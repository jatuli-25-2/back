package com.p_project.mypage;

import com.p_project.profile.ProfileDTO;
import com.p_project.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MypageService {

    private final ProfileService profileService;

    public ProfileDTO updateProfile(Long userId, MultipartFile file) {

        String imageURL = profileService.uploadProfile(userId, file);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(userId);
        profileDTO.setProfileURL(imageURL);

        return profileDTO;
    }



}
