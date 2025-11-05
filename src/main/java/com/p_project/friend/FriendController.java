package com.p_project.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
