package com.p_project.friend;

import com.p_project.user.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friend")
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/add")
    public ResponseEntity<Void> addFriend(@RequestBody FriendDTO friendDTO){
        log.info("in FriendController: addFriend");

        friendService.addFriend(friendDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/acept")
    public ResponseEntity<Void> aceptFriendRequest(@RequestBody FriendDTO friendDTO){
        log.info("in FriendController: addFriend");

        friendService.addFriend(friendDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<UserDTO>> listFriendRequest(@PathVariable Long userId){
        log.info("in FriendController: aceptFriendRequest");

        List<UserDTO> users = friendService.getMutualFriends(userId);
        return ResponseEntity.ok(users);
    }

}
