package yunhan.supplement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunhan.supplement.DTO.FriendRequestDTO;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Service.FriendshipService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // 친구 요청 보내기 (POST, JSON Body)
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> sendFriendRequest(@RequestBody FriendRequestDTO requestDTO) {
        friendshipService.sendFriendRequest(requestDTO.getSenderId(), requestDTO.getReceiverId());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "친구 요청을 보냈습니다."
        ));
    }

    // 친구 요청 수락하기 (POST, JSON Body)
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptFriendRequest(@RequestBody FriendRequestDTO requestDTO) {
        friendshipService.acceptFriendRequest(requestDTO.getSenderId(), requestDTO.getReceiverId());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "친구 요청을 수락했습니다."
        ));
    }

    // 친구 요청 거절하기 (DELETE, JSON Body)
    @DeleteMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectFriendRequest(@RequestBody FriendRequestDTO requestDTO) {
        friendshipService.rejectFriendRequest(requestDTO.getSenderId(), requestDTO.getReceiverId());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "친구 요청을 거절했습니다."
        ));
    }

    // 친구 목록 조회 (GET, @RequestParam)

    @GetMapping("/friends")
    public ResponseEntity<Map<String, Object>> getFriends(@RequestParam int userId) {
        List<UserEntity> friends = friendshipService.getFriends(userId);

        // 안전한 방식으로 Map 변환
        List<Map<String, Object>> friendList = friends.stream()
                .map(friend -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", friend.getId());
                    map.put("username", friend.getUsername());
                    map.put("name", friend.getName());
                    return map;
                })
                .collect(Collectors.toList());

        // 리스트를 역순으로 변경
        Collections.reverse(friendList);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "friends", friendList
        ));
    }


    @GetMapping("/following")
    public ResponseEntity<Map<String, Object>> getFollowingList(@RequestParam int userId) {
        List<UserEntity> following = friendshipService.getFollowingList(userId);

        // JSON 응답 형태로 변환
        List<Map<String, Object>> followingList = following.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("name", user.getName());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "following", followingList
        ));
    }



    // ✅ 친구 삭제 API (DELETE)
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFriend(@RequestBody FriendRequestDTO requestDTO) {
        try {
            friendshipService.removeFriend(requestDTO.getSenderId(), requestDTO.getReceiverId());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "친구가 삭제되었습니다."
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }


//    // ✅ 친구 수 및 사용자 이름 반환 API
//    @GetMapping("/friend-info")
//    public ResponseEntity<Map<String, Object>> getFriendInfo(@RequestParam int userId) {
//        Map<String, Object> friendInfo = friendshipService.getFriendInfo(userId);
//
//        return ResponseEntity.ok(Map.of(
//                "status", "success",
//                "friendCount", friendInfo.get("friendCount"),
//                "userName", friendInfo.get("userName")
//        ));
//    }
}
