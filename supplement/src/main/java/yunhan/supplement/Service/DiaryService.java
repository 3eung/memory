package yunhan.supplement.Service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yunhan.supplement.Entity.Diary;
import yunhan.supplement.Entity.Emotionapi;
import yunhan.supplement.Repository.DiaryRepository;
import yunhan.supplement.Repository.EmotionapiRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DiaryService {

    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private EmotionapiRepository emotionapiRepository;

    public void saveDiary(int userId, String title ,String content, String imageUrl,String weather, String date) {
        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setTitle(title);
        diary.setContent(content);
        diary.setImageUrl(imageUrl);
        diary.setWeather(weather);
        diary.setDate(LocalDate.parse(date));
        diaryRepository.save(diary);
    }
    // ✅ 특정 userId에 해당하는 다이어리만 조회
    public List<Diary> getDiariesByUserId(int userId) {
        return diaryRepository.findByUserId(userId);
    }

    public Optional<Diary> getDiaryById(int diaryId) {  // ✅ 메서드 추가
        return diaryRepository.findById(diaryId);
    }


    // ✅ 다이어리 삭제 메서드
    @Transactional
    public boolean deleteDiary(int diaryId, int userId) {
        Optional<Diary> diaryOptional = diaryRepository.findById(diaryId);

        if (diaryOptional.isEmpty()) {
            return false; // 다이어리 없음
        }

        Diary diary = diaryOptional.get();
        if (diary.getUserId() != userId) {
            return false; // 권한 없음
        }

        diaryRepository.deleteById(diaryId);
        return true; // 삭제 성공
    }

    public void saveEmotionapi(int diaryId, String emotion) {
        Emotionapi emotionapi = new Emotionapi();
        emotionapi.setDiaryId(diaryId);
        emotionapi.setEmotion(emotion);

        emotionapiRepository.save(emotionapi);
    }


    public Optional<Emotionapi> getEmotionByDiaryId(int diaryId) {
        return emotionapiRepository.findByDiaryId(diaryId);
    }

}

//package yunhan.supplement.Service;
//
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import yunhan.supplement.Entity.Diary;
//import yunhan.supplement.Repository.DiaryRepository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class DiaryService {
//
//    @Autowired
//    private DiaryRepository diaryRepository;
//
//    public void saveDiary(int userId, String title ,String content, String imageUrl,String weather, String date) {
//        Diary diary = new Diary();
//        diary.setUserId(userId);
//        diary.setTitle(title);
//        diary.setContent(content);
//        diary.setImageUrl(imageUrl);
//        diary.setWeather(weather);
//        diary.setDate(LocalDate.parse(date));
//        diaryRepository.save(diary);
//    }
//    // ✅ 특정 userId에 해당하는 다이어리만 조회
//    public List<Diary> getDiariesByUserId(int userId) {
//        return diaryRepository.findByUserId(userId);
//    }
//
//    public Optional<Diary> getDiaryById(int diaryId) {  // ✅ 메서드 추가
//        return diaryRepository.findById(diaryId);
//    }
//
//
//    // ✅ 다이어리 삭제 메서드
//    @Transactional
//    public boolean deleteDiary(int diaryId, int userId) {
//        Optional<Diary> diaryOptional = diaryRepository.findById(diaryId);
//
//        if (diaryOptional.isEmpty()) {
//            return false; // 다이어리 없음
//        }
//
//        Diary diary = diaryOptional.get();
//        if (diary.getUserId() != userId) {
//            return false; // 권한 없음
//        }
//
//        diaryRepository.deleteById(diaryId);
//        return true; // 삭제 성공
//    }
//
//}





