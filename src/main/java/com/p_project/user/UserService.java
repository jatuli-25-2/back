package com.p_project.user;

import com.p_project.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service //스프링이 관리해주는 객체
@RequiredArgsConstructor //controller 와 같이 final 멤버변수 생성자 만드는 역할
public class UserService {

    private final UserRepository userRepository; //디펜던시 추가
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public void save(UserDTO userDTO){
        //repository 의 save 메서드 호출
        System.out.println("\n\n\n\nuserDTO in userService : " + userDTO);
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        System.out.println("\n\n\n\nUserEntity in userService" + userEntity);
        userRepository.save(userEntity);
    }

    public void resetPassword(PasswordResetDTO dto) {
        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        // 새 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPwd(encodedPassword);

        userRepository.save(user);
    }

    public void clearSecurityContext(){
        SecurityContextHolder.clearContext();
    }

    public void clearAccessToken(HttpServletResponse response){
        Cookie accessCookie = new Cookie("Authorization", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);
    }

    public void clearRefreshToken(HttpServletResponse response){
        Cookie refreshCookie = new Cookie("RefreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
    }

    public Map<String, String> responseMessage(String message){
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }


    public String logoutUser(HttpServletRequest request, HttpServletResponse response) {
        String userEmail = null;

        try {
            String accessToken = jwtUtil.extractAccessToken(request);

            if (accessToken != null && !jwtUtil.isExpired(accessToken)) {
                userEmail = jwtUtil.getUsername(accessToken);
            }

        } catch (Exception e) {
            //jwt 파싱중오류처리
            log.warn("JWT 토큰 파싱 중 오류 발생: {}", e.getMessage());
        }

        //토큰 삭제 로직
        clearSecurityContext();
        clearAccessToken(response);
        clearRefreshToken(response);

        if (userEmail != null) {
            log.info("{} 님이 로그아웃했습니다.", userEmail);
            return userEmail + " 님이 로그아웃했습니다.";
        } else {
            log.info("로그아웃되었습니다. (사용자 정보 없음)");
            return "로그아웃되었습니다.";
        }
    }


}
