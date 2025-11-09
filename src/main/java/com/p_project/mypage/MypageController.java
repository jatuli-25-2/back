package com.p_project.mypage;

import com.p_project.profile.ProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myapge")
public class MypageController {

    private final MypageService mypageService;

    @PostMapping(path = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDTO> updateProfile(
            @RequestParam("userId") Long userId,
            @RequestPart("file") MultipartFile file) {

        ProfileDTO result = mypageService.updateProfile(userId, file);
        return ResponseEntity.ok(result);
    }

}
