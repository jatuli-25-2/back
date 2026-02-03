package com.p_project.friend;

import com.p_project.calendar.CalendarDTO;
import com.p_project.calendar.CalendarService;
import com.p_project.calendar.DailyWritingItemDTO;
import com.p_project.profile.ProfileService;
import com.p_project.user.UserDTO;
import com.p_project.user.UserEntity;
import com.p_project.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserService userService;
    private final CalendarService calendarService;
    private final ProfileService profileService;

    public void addFriend(FriendDTO friendDTO){

        log.info("in FriendService: addFriend");
        Long friendId = Long.valueOf(userService.findByNickname(friendDTO.getFriendNickName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId());

        friendDTO.setToUserId(friendId);
        FriendEntity friendEntity = friendDTO.toEntity();
        friendRepository.save(friendEntity);
    }

    public List<UserDTO> getMutualFriends(Long userId) {
        List<UserEntity> users = friendRepository.findMutualFriends(userId);

        return users.stream()
                .map(user -> {
                    UserDTO dto = UserDTO.fromEntity(user);
                    profileService.getProfile(user.getId())
                            .map(p -> p.getImageUrl())
                            .ifPresent(dto::setProfileImageUrl);
                    return dto;
                })
                .toList();
    }

    public List<UserDTO> getPendingRequests(Long userId) {
        List<UserEntity> users = friendRepository.findPendingRequestSenders(userId);

        return users.stream()
                .map(user -> {
                    UserDTO dto = UserDTO.fromEntity(user);
                    profileService.getProfile(user.getId())
                            .map(p -> p.getImageUrl())
                            .ifPresent(dto::setProfileImageUrl);
                    return dto;
                })
                .toList();
    }

    @Transactional
    public void acceptFriend(Long fromUserId, Long toUserId) {
        if (friendRepository.existsFriendship(fromUserId, toUserId) < 1) {
            friendRepository.acceptFriendRequest(fromUserId, toUserId);
        }
    }

    @Transactional // 하나의 트랜잭션으로 묶어서 실행
    public FriendSimpleDTO sendFriendRequest(Long fromUserId, String email) {
        friendRepository.sendFriendRequest(fromUserId, email);
        Long toUserId = userService.findUserIdByEmail(email);

        return new FriendSimpleDTO(toUserId, fromUserId);
    }

    @Transactional
    public void deleteFriendRequest(Long fromUserId, Long toUserId) {
        friendRepository.deleteFriendRequest(fromUserId, toUserId);
    }

    public CalendarDTO getFriendCalendarSummary(Long userId, Long friendId, LocalDate date) {
        return calendarService.getFriendCalendarSummary(userId, friendId, date);
    }

    /** 서로 친구인지 (수락된 상태) */
    public boolean areMutualFriends(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) return true;
        Integer a = friendRepository.existsFriendship(userId, friendId);
        Integer b = friendRepository.existsFriendship(friendId, userId);
        return (a != null && a >= 1) && (b != null && b >= 1);
    }

    /** 친구의 해당 날짜 글 목록 (친구만 조회 가능) */
    public List<DailyWritingItemDTO> getFriendDailyWritings(Long userId, Long friendId, LocalDate date) {
        if (!areMutualFriends(userId, friendId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Not friends or not allowed");
        }
        return calendarService.getDailyWritings(friendId, date);
    }

}
