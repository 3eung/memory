package yunhan.supplement.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yunhan.supplement.Entity.Friendship;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.FriendshipRepository;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.mapper.FriendMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendshipService {
    @Autowired
    private FriendMapper friendMapper;

    private final FriendshipRepository friendshipRepository;
    private final UserRepository usersRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository usersRepository) {
        this.friendshipRepository = friendshipRepository;
        this.usersRepository = usersRepository;
    }

    // 친구 요청 보내기
    public void sendFriendRequest(int senderId, int receiverId) {
        UserEntity sender = usersRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
        UserEntity receiver = usersRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));
        // ✅ 자기 자신에게 친구 요청 방지
        if (senderId == receiverId) {
            throw new IllegalStateException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        if (friendshipRepository.findBySenderAndReceiver(sender, receiver).isPresent()) {
            throw new IllegalStateException("이미 친구 요청을 보냈거나 친구입니다.");
        }

        Friendship friendship = new Friendship(sender, receiver, true);
        friendshipRepository.save(friendship);
        Friendship friendship2 = new Friendship(receiver, sender, false);
        friendshipRepository.save(friendship2);
    }

    // 친구 요청 수락하기
    public void acceptFriendRequest(int senderId, int receiverId) {
        UserEntity sender = usersRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
        UserEntity receiver = usersRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));

        Friendship friendship = friendshipRepository.findBySenderAndReceiver(receiver,sender)
                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));

        friendship.setFriend(true);
        friendshipRepository.save(friendship);

    }

    // 친구 요청 거절하기 (또는 취소)
    public void rejectFriendRequest(int senderId, int receiverId) {
        UserEntity sender = usersRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
        UserEntity receiver = usersRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));

        Friendship friendship = friendshipRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));

        friendshipRepository.delete(friendship);

        Friendship friendship2 = friendshipRepository.findBySenderAndReceiver(receiver,sender)
                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));

        friendshipRepository.delete(friendship2);
    }
    // 친구 목록 조회
    public List<UserEntity> getFriends(int userId) {
        List<UserEntity> friends = friendMapper.findFriendsByUserId(userId);

        // ✅ 친구 목록이 없을 경우 빈 리스트 반환
        if (friends == null) {
            return Collections.emptyList();
        }

        return friends;
    }

    // ✅ 내가 팔로우한 사람(보낸 요청 목록)
    public List<UserEntity> getFollowingList(int userId) {
        UserEntity sender = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 userId"));

        // ✅ sender가 userId이고, isFriend=false인 요청을 조회
        return friendshipRepository.findBySenderAndIsFriendFalse(sender)
                .stream()
                .map(Friendship::getReceiver) // 수락 대기 중인 사람들만 반환
                .collect(Collectors.toList());
    }


    // ✅ 친구 삭제 기능 (Unfollow)
    public void removeFriend(int userId, int friendId) {
        UserEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 userId"));

        UserEntity friend = usersRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 friendId"));

        // 친구 관계가 존재하는지 확인
        Optional<Friendship> existingFriendship = friendshipRepository.findBySenderAndReceiver(user, friend);
        Optional<Friendship> reverseFriendship = friendshipRepository.findByReceiverAndSender(user, friend);

        if (existingFriendship.isPresent() || reverseFriendship.isPresent()) {
            // 양방향 친구 관계 삭제
            friendshipRepository.deleteFriendship(user, friend);
        } else {
            throw new IllegalStateException("삭제할 친구 관계가 존재하지 않습니다.");
        }
    }
//    // ✅ 친구 수 + 사용자 이름 반환
//    public Map<String, Object> getFriendInfo(int userId) {
//        int friendCount = friendshipRepository.countFriends(userId);
//        String userName = usersRepository.findUserNameById(userId);
//
//        // 결과를 Map으로 반환
//        Map<String, Object> result = new HashMap<>();
//        result.put("friendCount", friendCount);
//        result.put("userName", userName);
//        return result;
//    }
}


//package yunhan.supplement.Service;
//
//import org.springframework.stereotype.Service;
//import yunhan.supplement.Entity.Friendship;
//import yunhan.supplement.Entity.UserEntity;
//import yunhan.supplement.Repository.FriendshipRepository;
//import yunhan.supplement.Repository.UserRepository;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class FriendshipService {
//    private final FriendshipRepository friendshipRepository;
//    private final UserRepository usersRepository;
//
//    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository usersRepository) {
//        this.friendshipRepository = friendshipRepository;
//        this.usersRepository = usersRepository;
//    }
//
//    // 친구 요청 보내기
//    public void sendFriendRequest(Long senderId, Long receiverId) {
//        UserEntity sender = usersRepository.findById(senderId)
//                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
//        UserEntity receiver = usersRepository.findById(receiverId)
//                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));
//
//        if (friendshipRepository.findBySenderAndReceiver(sender, receiver).isPresent()) {
//            throw new IllegalStateException("이미 친구 요청을 보냈거나 친구입니다.");
//        }
//
//        Friendship friendship = new Friendship(sender, receiver, true);
//        friendshipRepository.save(friendship);
//        Friendship friendship2 = new Friendship(receiver, sender, false);
//        friendshipRepository.save(friendship2);
//    }
//
//    // 친구 요청 수락하기
//    public void acceptFriendRequest(Long senderId, Long receiverId) {
//        UserEntity sender = usersRepository.findById(senderId)
//                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
//        UserEntity receiver = usersRepository.findById(receiverId)
//                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));
//
//        Friendship friendship = friendshipRepository.findBySenderAndReceiver(sender, receiver)
//                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));
//
//        friendship.setFriend(true);
//        friendshipRepository.save(friendship);
//
//        // 반대 방향의 친구 관계도 추가
//        Friendship reverseFriendship = new Friendship(receiver, sender, true);
//        friendshipRepository.save(reverseFriendship);
//    }
//
//    // 친구 요청 거절하기 (또는 취소)
//    public void rejectFriendRequest(Long senderId, Long receiverId) {
//        UserEntity sender = usersRepository.findById(senderId)
//                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
//        UserEntity receiver = usersRepository.findById(receiverId)
//                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));
//
//        Friendship friendship = friendshipRepository.findBySenderAndReceiver(sender, receiver)
//                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));
//
//        friendshipRepository.delete(friendship);
//    }
//
//    // 친구 목록 조회
//    public List<UserEntity> getFriends(Long userId) {
//        UserEntity user = usersRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("잘못된 userId"));
//
//        List<UserEntity> friends = friendshipRepository.findBySenderAndIsFriendTrue(user)
//                .stream()
//                .map(Friendship::getReceiver)
//                .collect(Collectors.toList());
//
//        friends.addAll(friendshipRepository.findByReceiverAndIsFriendTrue(user)
//                .stream()
//                .map(Friendship::getSender)
//                .collect(Collectors.toList()));
//
//        return friends;
//    }
//}

