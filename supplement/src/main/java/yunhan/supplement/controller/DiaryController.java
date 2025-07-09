package yunhan.supplement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yunhan.supplement.DTO.DiaryDTO;
import yunhan.supplement.Entity.Emotionapi;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.Service.DiaryService;
import yunhan.supplement.Service.FirebaseStorageService;
import yunhan.supplement.Entity.Diary;
import yunhan.supplement.Service.GoogleTranslateService;
import yunhan.supplement.Service.TwinwordService;
import yunhan.supplement.util.EmotionParser;
import yunhan.supplement.util.TranslationParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor // ✅ 권장되는 방식으로 의존성 주입
public class DiaryController {

    private final FirebaseStorageService firebaseStorageService;
    private final DiaryService diaryService;
    private final UserRepository userRepository; // ✅ 인스턴스 필드로 선언
    private final TwinwordService twinwordService;
    private final GoogleTranslateService translateService;

    // ✅ 이미지 업로드 API
    @PostMapping(value = "/diary", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> uploadAndCreateDiary(
            Authentication authentication,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "imageUrl", required = false) MultipartFile image,
            @RequestParam("weather") String weather,
            @RequestParam("date") String date) {

        System.out.println("🔍 [DEBUG] 인증된 사용자: " + (authentication != null ? authentication.getName() : "인증 실패"));

        if (authentication == null) {
            return ResponseEntity.status(403).body(Map.of("message", "JWT 인증 필요"));
        }

        String username = authentication.getName();
        System.out.println("✅ 인증된 사용자명: " + username);

        // ✅ username을 이용해 userId 조회
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        UserEntity user = userOptional.get();
        int userId = user.getId();
        System.out.println("✅ 조회된 userId: " + userId);

        // ✅ 이미지 업로드 (이미지가 존재할 경우에만)
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = firebaseStorageService.uploadImage(image);
                System.out.println("✅ 업로드된 이미지 URL: " + imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(500).body(Map.of("message", "Image upload failed: " + e.getMessage()));
            }
        }

        // ✅ 일기 저장
        diaryService.saveDiary(userId, title, content, imageUrl, weather,date);

        // ✅ 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Diary created successfully");

        if (imageUrl != null) {
            response.put("imageUrl", imageUrl);  // ✅ 이미지 업로드 시 imageUrl 포함
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<DiaryDTO>> getMyDiaries(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(403).body(null);
        }

        String username = authentication.getName();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        UserEntity user = userOptional.get();
        int userId = user.getId();

        List<Diary> myDiaries = diaryService.getDiariesByUserId(userId);

        // ✅ DiaryDto → DiaryDTO (클래스명 통일)
        List<DiaryDTO> diaryDTOs = myDiaries.stream()
                .map(diary -> new DiaryDTO(diary.getDiaryId(), diary.getTitle(), diary.getDate()))
                .toList();

        return ResponseEntity.ok(diaryDTOs);
    }

    @GetMapping("/diary/{diaryId}")
    public ResponseEntity<Diary> getDiaryById(@PathVariable int diaryId) {
        Optional<Diary> diaryOptional = diaryService.getDiaryById(diaryId);

        if (diaryOptional.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(diaryOptional.get());
    }

    @GetMapping("/diary/{diaryId}/emotion")
    public ResponseEntity<Map<String, Object>> emotion(@PathVariable int diaryId) {
        Optional<Diary> diaryOptional = diaryService.getDiaryById(diaryId);

        if (diaryOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "다이어리를 찾을 수 없습니다."));
        }

        // ✅ 1. 이미 감정 결과가 있는 경우, DB에서 바로 꺼내서 반환
        Optional<Emotionapi> existing = diaryService.getEmotionByDiaryId(diaryId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "diaryId", diaryId,
                    "emotion", existing.get().getEmotion()
            ));
        }

        // ✅ 2. 없으면 API 호출 → 분석 → 저장
        String content = diaryOptional.get().getContent();
        String translated = translateService.translateText(content, "en");
        String emotionapi = twinwordService.analyzeEmotion(translated).join();
        List<String> emotionlist = EmotionParser.extractKeywords(emotionapi);

        if (emotionlist.isEmpty()) {
            return ResponseEntity.status(204).body(Map.of("message", "감정 분석 결과가 없습니다."));
        }

        String emotion = emotionlist.get(0);
        String translatedEmotion = translateService.translateText(emotion, "ko");
        String finalEmotion = TranslationParser.extractTranslatedText(translatedEmotion);

        diaryService.saveEmotionapi(diaryId, finalEmotion);

        return ResponseEntity.ok(Map.of(
                "diaryId", diaryId,
                "emotion", finalEmotion
        ));
    }

//    @GetMapping("/diary/{diaryId}/emotion")
//    public ResponseEntity<Map<String, Object>> emotion(@PathVariable int diaryId) {
//        Optional<Diary> diaryOptional = diaryService.getDiaryById(diaryId);
//
//        if (diaryOptional.isEmpty()) {
//            return ResponseEntity.status(404).body(Map.of("message", "다이어리를 찾을 수 없습니다."));
//        }
//
//        String content = diaryOptional.get().getContent();
//        String translated = translateService.translateText(content, "en");
//        String emotionapi = twinwordService.analyzeEmotion(translated).join();
//        List<String> emotionlist = EmotionParser.extractKeywords(emotionapi);
//
//        if (emotionlist.isEmpty()) {
//            return ResponseEntity.status(204).body(Map.of("message", "감정 분석 결과가 없습니다."));
//        }
//
//        String emotion = emotionlist.get(0);
//        String translatedEmotion = translateService.translateText(emotion, "ko");
//        String finalEmotion = TranslationParser.extractTranslatedText(translatedEmotion);
//
//        diaryService.saveEmotionapi(diaryId, finalEmotion);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("diaryId", diaryId);
//        response.put("emotion", finalEmotion);
//
//        return ResponseEntity.ok(response);
//    }



    @DeleteMapping("/diary/{diaryId}")
    public ResponseEntity<Map<String, String>> deleteDiary(
            Authentication authentication,
            @PathVariable int diaryId) {

        if (authentication == null) {
            return ResponseEntity.status(403).body(Map.of("message", "JWT 인증 필요"));
        }

        String username = authentication.getName();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        UserEntity user = userOptional.get();
        int userId = user.getId();

        boolean isDeleted = diaryService.deleteDiary(diaryId, userId);

        if (!isDeleted) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제할 권한이 없거나 다이어리가 존재하지 않습니다."));
        }

        return ResponseEntity.ok(Map.of("message", "다이어리 삭제 성공"));
    }


}







//
//package yunhan.supplement.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import yunhan.supplement.DTO.DiaryDTO;
//import yunhan.supplement.Entity.UserEntity;
//import yunhan.supplement.Repository.UserRepository;
//import yunhan.supplement.Service.DiaryService;
//import yunhan.supplement.Service.FirebaseStorageService;
//import yunhan.supplement.Entity.Diary;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/diary")
//@RequiredArgsConstructor // ✅ 권장되는 방식으로 의존성 주입
//public class DiaryController {
//
//    private final FirebaseStorageService firebaseStorageService;
//    private final DiaryService diaryService;
//    private final UserRepository userRepository; // ✅ 인스턴스 필드로 선언
//
//    // ✅ 이미지 업로드 API
//    @PostMapping(value = "/diary", consumes = {"multipart/form-data"})
//    public ResponseEntity<Map<String, Object>> uploadAndCreateDiary(
//            Authentication authentication,
//            @RequestParam("title") String title,
//            @RequestParam("content") String content,
//            @RequestPart(value = "imageUrl", required = false) MultipartFile image,
//            @RequestParam("weather") String weather,
//            @RequestParam("date") String date) {
//
//        System.out.println("🔍 [DEBUG] 인증된 사용자: " + (authentication != null ? authentication.getName() : "인증 실패"));
//
//        if (authentication == null) {
//            return ResponseEntity.status(403).body(Map.of("message", "JWT 인증 필요"));
//        }
//
//        String username = authentication.getName();
//        System.out.println("✅ 인증된 사용자명: " + username);
//
//        // ✅ username을 이용해 userId 조회
//        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
//        if (userOptional.isEmpty()) {
//            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
//        }
//
//        UserEntity user = userOptional.get();
//        int userId = user.getId();
//        System.out.println("✅ 조회된 userId: " + userId);
//
//        // ✅ 이미지 업로드 (이미지가 존재할 경우에만)
//        String imageUrl = null;
//        if (image != null && !image.isEmpty()) {
//            try {
//                imageUrl = firebaseStorageService.uploadImage(image);
//                System.out.println("✅ 업로드된 이미지 URL: " + imageUrl);
//            } catch (IOException e) {
//                return ResponseEntity.status(500).body(Map.of("message", "Image upload failed: " + e.getMessage()));
//            }
//        }
//
//        // ✅ 일기 저장
//        diaryService.saveDiary(userId, title, content, imageUrl, weather,date);
//
//        // ✅ 응답 데이터 생성
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Diary created successfully");
//
//        if (imageUrl != null) {
//            response.put("imageUrl", imageUrl);  // ✅ 이미지 업로드 시 imageUrl 포함
//        }
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/my")
//    public ResponseEntity<List<DiaryDTO>> getMyDiaries(Authentication authentication) {
//        if (authentication == null) {
//            return ResponseEntity.status(403).body(null);
//        }
//
//        String username = authentication.getName();
//        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
//        if (userOptional.isEmpty()) {
//            return ResponseEntity.status(404).body(null);
//        }
//
//        UserEntity user = userOptional.get();
//        int userId = user.getId();
//
//        List<Diary> myDiaries = diaryService.getDiariesByUserId(userId);
//
//        // ✅ DiaryDto → DiaryDTO (클래스명 통일)
//        List<DiaryDTO> diaryDTOs = myDiaries.stream()
//                .map(diary -> new DiaryDTO(diary.getDiaryId(), diary.getTitle(), diary.getDate()))
//                .toList();
//
//        return ResponseEntity.ok(diaryDTOs);
//    }
//
//    @GetMapping("/diary/{diaryId}")
//    public ResponseEntity<Diary> getDiaryById(@PathVariable int diaryId) {
//        Optional<Diary> diaryOptional = diaryService.getDiaryById(diaryId);
//
//        if (diaryOptional.isEmpty()) {
//            return ResponseEntity.status(404).body(null);
//        }
//
//        return ResponseEntity.ok(diaryOptional.get());
//    }
//
//    @DeleteMapping("/diary/{diaryId}")
//    public ResponseEntity<Map<String, String>> deleteDiary(
//            Authentication authentication,
//            @PathVariable int diaryId) {
//
//        if (authentication == null) {
//            return ResponseEntity.status(403).body(Map.of("message", "JWT 인증 필요"));
//        }
//
//        String username = authentication.getName();
//        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
//        if (userOptional.isEmpty()) {
//            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
//        }
//
//        UserEntity user = userOptional.get();
//        int userId = user.getId();
//
//        boolean isDeleted = diaryService.deleteDiary(diaryId, userId);
//
//        if (!isDeleted) {
//            return ResponseEntity.status(403).body(Map.of("message", "삭제할 권한이 없거나 다이어리가 존재하지 않습니다."));
//        }
//
//        return ResponseEntity.ok(Map.of("message", "다이어리 삭제 성공"));
//    }
//
//
//}
//



