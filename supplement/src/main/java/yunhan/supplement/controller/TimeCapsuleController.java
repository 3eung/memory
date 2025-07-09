package yunhan.supplement.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yunhan.supplement.DTO.TimeCapsuleDTO;
import yunhan.supplement.DTO.TimeCapsuleSummaryDTO;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.Service.FirebaseStorageService;
import yunhan.supplement.Service.TimeCapsuleService;
import yunhan.supplement.Service.FirebaseStorageService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/timecapsules")
public class TimeCapsuleController {
    @Autowired
    private final FirebaseStorageService firebaseStorageService;
    private final TimeCapsuleService timeCapsuleService;
    private final UserRepository userRepository;


    public TimeCapsuleController(TimeCapsuleService timeCapsuleService, UserRepository userRepository,FirebaseStorageService firebaseStorageService) {
        this.timeCapsuleService = timeCapsuleService;
        this.userRepository = userRepository;
        this.firebaseStorageService =firebaseStorageService;

    }

    // ✅ 타임캡슐 생성 (이미지 포함)
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createTimeCapsule(
            Authentication authentication,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam("openDate") String openDate,
            @RequestParam("userIds") List<Integer> userIds) {

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

        // ✅ 이미지 업로드 처리
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = firebaseStorageService.uploadImage(image);
            } catch (IOException e) {
                return ResponseEntity.status(500).body(Map.of("message", "Image upload failed: " + e.getMessage()));
            }
        }

        // ✅ 타임캡슐 저장 후 ID 반환
        int timecapsuleId = timeCapsuleService.saveTimeCapsule(userId, title, content, imageUrl, openDate, userIds);

        // ✅ 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Time capsule created successfully");
        response.put("timecapsuleId", timecapsuleId); // ✅ 타임캡슐 ID 추가

        if (imageUrl != null) {
            response.put("imageUrl", imageUrl);
        }

        return ResponseEntity.ok(response);
    }


    // ✅ 특정 사용자의 타임캡슐 조회
// ✅ 특정 사용자의 타임캡슐 조회 API
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserTimeCapsules(@PathVariable int userId) {
        try {
            List<TimeCapsuleSummaryDTO> timeCapsules = timeCapsuleService.getUserTimeCapsules(userId);
            return ResponseEntity.ok(timeCapsules);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
    // ✅ 특정 타임캡슐 상세 조회 API
// ✅ 특정 타임캡슐 상세 조회 API
    @GetMapping("/detail/{timecapsuleId}")
    public ResponseEntity<Map<String, Object>> getTimeCapsuleDetail(@PathVariable int timecapsuleId) {
        Map<String, Object> response = timeCapsuleService.getTimeCapsuleDetail(timecapsuleId);
        return ResponseEntity.ok(response);
    }
    // ✅ 타임캡슐 삭제 API
    @DeleteMapping("/{timecapsuleId}")
    public ResponseEntity<Map<String, Object>> deleteTimeCapsule(@PathVariable int timecapsuleId) {
        try {
            timeCapsuleService.deleteTimeCapsule(timecapsuleId);
            return ResponseEntity.ok(Map.of("message", "Time capsule deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

}




//@RestController
//@RequestMapping("/api/timecapsules")
//public class TimeCapsuleController {
//    @Autowired
//    private final FirebaseStorageService firebaseStorageService;
//    private final TimeCapsuleService timeCapsuleService;
//    private final UserRepository userRepository;
//
//
//    public TimeCapsuleController(TimeCapsuleService timeCapsuleService, UserRepository userRepository,FirebaseStorageService firebaseStorageService) {
//        this.timeCapsuleService = timeCapsuleService;
//        this.userRepository = userRepository;
//        this.firebaseStorageService =firebaseStorageService;
//
//    }
//
//    // ✅ 타임캡슐 생성 (이미지 포함)
//    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String, Object>> createTimeCapsule(
//            Authentication authentication,
//            @RequestParam("title") String title,
//            @RequestParam("content") String content,
//            @RequestPart(value = "image", required = false) MultipartFile image,
//            @RequestParam("openDate") String openDate,
//            @RequestParam("userIds") String userIdsString) { // 🚨 문자열로 받아서 변환
//
//        // ✅ 문자열을 List<Integer>로 변환
//        List<Integer> userIds = Arrays.stream(userIdsString.split(","))
//                .map(Integer::parseInt)
//                .collect(Collectors.toList());
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
//
//        // ✅ 타임캡슐 저장
//        timeCapsuleService.saveTimeCapsule(userId, title, content, imageUrl, openDate, userIds);
//
//// ✅ 응답 생성 (imageUrl이 null이면 포함하지 않음)
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Time capsule created successfully");
//
//        if (imageUrl != null) {
//            response.put("imageUrl", imageUrl);
//        }
//
//        return ResponseEntity.ok(response);
//    }
//
//    // ✅ 특정 사용자의 타임캡슐 조회
//    @GetMapping("/{userId}")
//    public ResponseEntity<List<TimeCapsuleDTO>> getUserTimeCapsules(@PathVariable int userId) {
//        List<TimeCapsuleDTO> timeCapsules = timeCapsuleService.getUserTimeCapsules(userId);
//        return ResponseEntity.ok(timeCapsules);
//    }
//}
