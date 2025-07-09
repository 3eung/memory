package yunhan.supplement.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import yunhan.supplement.DTO.TimeCapsuleDTO;
import yunhan.supplement.DTO.TimeCapsuleSummaryDTO;
import yunhan.supplement.Entity.TimeCapsule;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.TimeCapsuleRepository;
import yunhan.supplement.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TimeCapsuleService {
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;

    public TimeCapsuleService(TimeCapsuleRepository timeCapsuleRepository, UserRepository userRepository) {
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public int saveTimeCapsule(int creatorId, String title, String content, String imageUrl, String openDate, List<Integer> userIds) {
        TimeCapsule timeCapsule = new TimeCapsule();
        timeCapsule.setTitle(title);
        timeCapsule.setContent(content);
        timeCapsule.setImagePath(imageUrl);
        timeCapsule.setOpenDate(LocalDateTime.parse(openDate));
        timeCapsule.setIsOpened(false);

        // ✅ 중복 방지를 위해 Set 사용
        Set<UserEntity> users = new HashSet<>();

        // ✅ 타임캡슐을 만든 사용자 추가
        Optional<UserEntity> creatorOptional = userRepository.findById(creatorId);
        creatorOptional.ifPresent(users::add);

        // ✅ 공유 대상 사용자 추가
        for (int userId : userIds) {
            userRepository.findById(userId).ifPresent(users::add);
        }

        timeCapsule.setUsers(new ArrayList<>(users));

        // ✅ 타임캡슐 저장 후 ID 반환
        TimeCapsule savedCapsule = timeCapsuleRepository.save(timeCapsule);
        return savedCapsule.getTimecapsuleId(); // ✅ 저장된 타임캡슐의 ID 반환
    }


    // ✅ 특정 사용자의 타임캡슐 조회
    public List<TimeCapsuleSummaryDTO> getUserTimeCapsules(int userId) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        UserEntity user = userOptional.get();
        List<TimeCapsule> timeCapsules = timeCapsuleRepository.findByUsersContains(user);

        return timeCapsules.stream()
                .map(tc -> new TimeCapsuleSummaryDTO(tc.getTimecapsuleId(), tc.getTitle(), tc.getOpenDate(), tc.getIsOpened()))
                .collect(Collectors.toList());
    }
    public Map<String, Object> getTimeCapsuleDetail(int timecapsuleId) {
        Optional<TimeCapsule> timeCapsuleOptional = timeCapsuleRepository.findById(timecapsuleId);
        if (timeCapsuleOptional.isEmpty()) {
            throw new RuntimeException("Time capsule not found");
        }

        TimeCapsule timeCapsule = timeCapsuleOptional.get();

        // ✅ 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();

        // ✅ `openDate`가 현재 시간보다 과거라면 `isOpened = true`로 업데이트
        if (!timeCapsule.getIsOpened() && timeCapsule.getOpenDate().isBefore(now)) {
            timeCapsule.setIsOpened(true);
            timeCapsuleRepository.save(timeCapsule);
        }

        // ✅ 아직 열리지 않은 경우 메시지만 반환
        if (!timeCapsule.getIsOpened()) {
            return Map.of("message", "아직 기한이 되지 않았습니다.");
        }

        // ✅ 타임캡슐을 공유하는 사용자 정보 가져오기
        List<UserEntity> users = timeCapsule.getUsers();
        List<Integer> userIds = users.stream().map(UserEntity::getId).toList();
        List<String> usernames = users.stream().map(UserEntity::getUsername).toList();
        List<String> names = users.stream().map(UserEntity::getName).toList();

        // ✅ HashMap을 사용하여 `null` 값 방어 처리
        Map<String, Object> response = new HashMap<>();
        response.put("timecapsuleId", timeCapsule.getTimecapsuleId());
        response.put("title", timeCapsule.getTitle());
        response.put("content", timeCapsule.getContent() != null ? timeCapsule.getContent() : ""); // 🚀 null 값 방어
        response.put("imagePath", timeCapsule.getImagePath() != null ? timeCapsule.getImagePath() : ""); // 🚀 null 값 방어
        response.put("openDate", timeCapsule.getOpenDate());
        response.put("isOpened", timeCapsule.getIsOpened());
        //response.put("userIds", userIds);      // ✅ 공유된 친구 ID 리스트 추가
        response.put("usernames", usernames);  // ✅ 공유된 친구 이름 리스트 추가
        response.put("names", names);
        return response;
    }
    @Transactional
    public void deleteTimeCapsule(int timecapsuleId) {
        Optional<TimeCapsule> timeCapsuleOptional = timeCapsuleRepository.findById(timecapsuleId);
        if (timeCapsuleOptional.isEmpty()) {
            throw new RuntimeException("Time capsule not found");
        }

        // ✅ 타임캡슐 삭제
        timeCapsuleRepository.deleteById(timecapsuleId);
    }
}



//package yunhan.supplement.Service;
//
//import jakarta.transaction.Transactional;
//import org.springframework.stereotype.Service;
//import yunhan.supplement.DTO.TimeCapsuleDTO;
//import yunhan.supplement.DTO.TimeCapsuleSummaryDTO;
//import yunhan.supplement.Entity.TimeCapsule;
//import yunhan.supplement.Entity.UserEntity;
//import yunhan.supplement.Repository.TimeCapsuleRepository;
//import yunhan.supplement.Repository.UserRepository;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//
//@Service
//public class TimeCapsuleService {
//    private final TimeCapsuleRepository timeCapsuleRepository;
//    private final UserRepository userRepository;
//
//    public TimeCapsuleService(TimeCapsuleRepository timeCapsuleRepository, UserRepository userRepository) {
//        this.timeCapsuleRepository = timeCapsuleRepository;
//        this.userRepository = userRepository;
//    }
//
//    @Transactional
//    public int saveTimeCapsule(int creatorId, String title, String content, String imageUrl, String openDate, List<Integer> userIds) {
//        TimeCapsule timeCapsule = new TimeCapsule();
//        timeCapsule.setTitle(title);
//        timeCapsule.setContent(content);
//        timeCapsule.setImagePath(imageUrl);
//        timeCapsule.setOpenDate(LocalDateTime.parse(openDate));
//        timeCapsule.setIsOpened(false);
//
//        // ✅ 중복 방지를 위해 Set 사용
//        Set<UserEntity> users = new HashSet<>();
//
//        // ✅ 타임캡슐을 만든 사용자 추가
//        Optional<UserEntity> creatorOptional = userRepository.findById(creatorId);
//        creatorOptional.ifPresent(users::add);
//
//        // ✅ 공유 대상 사용자 추가
//        for (int userId : userIds) {
//            userRepository.findById(userId).ifPresent(users::add);
//        }
//
//        timeCapsule.setUsers(new ArrayList<>(users));
//
//        // ✅ 타임캡슐 저장 후 ID 반환
//        TimeCapsule savedCapsule = timeCapsuleRepository.save(timeCapsule);
//        return savedCapsule.getTimecapsuleId(); // ✅ 저장된 타임캡슐의 ID 반환
//    }
//
//
//    // ✅ 특정 사용자의 타임캡슐 조회
//    public List<TimeCapsuleSummaryDTO> getUserTimeCapsules(int userId) {
//        Optional<UserEntity> userOptional = userRepository.findById(userId);
//        if (userOptional.isEmpty()) {
//            throw new RuntimeException("User not found");
//        }
//
//        UserEntity user = userOptional.get();
//        List<TimeCapsule> timeCapsules = timeCapsuleRepository.findByUsersContains(user);
//
//        return timeCapsules.stream()
//                .map(tc -> new TimeCapsuleSummaryDTO(tc.getTimecapsuleId(), tc.getTitle(), tc.getOpenDate(), tc.getIsOpened()))
//                .collect(Collectors.toList());
//    }
//    public Map<String, Object> getTimeCapsuleDetail(int timecapsuleId) {
//        Optional<TimeCapsule> timeCapsuleOptional = timeCapsuleRepository.findById(timecapsuleId);
//        if (timeCapsuleOptional.isEmpty()) {
//            throw new RuntimeException("Time capsule not found");
//        }
//
//        TimeCapsule timeCapsule = timeCapsuleOptional.get();
//
//        // ✅ 현재 시간 가져오기
//        LocalDateTime now = LocalDateTime.now();
//
//        // ✅ `openDate`가 현재 시간보다 과거라면 `isOpened = true`로 업데이트
//        if (!timeCapsule.getIsOpened() && timeCapsule.getOpenDate().isBefore(now)) {
//            timeCapsule.setIsOpened(true);
//            timeCapsuleRepository.save(timeCapsule);
//        }
//
//        // ✅ 아직 열리지 않은 경우 메시지만 반환
//        if (!timeCapsule.getIsOpened()) {
//            return Map.of("message", "아직 기한이 되지 않았습니다.");
//        }
//
//        // ✅ 타임캡슐을 공유하는 사용자 정보 가져오기
//        List<UserEntity> users = timeCapsule.getUsers();
//        List<Integer> userIds = users.stream().map(UserEntity::getId).toList();
//        List<String> usernames = users.stream().map(UserEntity::getUsername).toList();
//
//        // ✅ HashMap을 사용하여 `null` 값 방어 처리
//        Map<String, Object> response = new HashMap<>();
//        response.put("timecapsuleId", timeCapsule.getTimecapsuleId());
//        response.put("title", timeCapsule.getTitle());
//        response.put("content", timeCapsule.getContent() != null ? timeCapsule.getContent() : ""); // 🚀 null 값 방어
//        response.put("imagePath", timeCapsule.getImagePath() != null ? timeCapsule.getImagePath() : ""); // 🚀 null 값 방어
//        response.put("openDate", timeCapsule.getOpenDate());
//        response.put("isOpened", timeCapsule.getIsOpened());
//        //response.put("userIds", userIds);      // ✅ 공유된 친구 ID 리스트 추가
//        response.put("usernames", usernames);  // ✅ 공유된 친구 이름 리스트 추가
//
//        return response;
//    }
//    @Transactional
//    public void deleteTimeCapsule(int timecapsuleId) {
//        Optional<TimeCapsule> timeCapsuleOptional = timeCapsuleRepository.findById(timecapsuleId);
//        if (timeCapsuleOptional.isEmpty()) {
//            throw new RuntimeException("Time capsule not found");
//        }
//
//        // ✅ 타임캡슐 삭제
//        timeCapsuleRepository.deleteById(timecapsuleId);
//    }
//}
