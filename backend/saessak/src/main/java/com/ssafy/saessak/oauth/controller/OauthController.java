package com.ssafy.saessak.oauth.controller;

import com.ssafy.saessak.oauth.dto.LoginSuccessResponseDto;
import com.ssafy.saessak.oauth.service.KakaoSocialService;
import com.ssafy.saessak.oauth.service.ParentService;
import com.ssafy.saessak.oauth.service.TeacherService;
import com.ssafy.saessak.oauth.token.service.RefreshTokenService;
import com.ssafy.saessak.result.ResultCode;
import com.ssafy.saessak.result.ResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.security.Principal;

@CrossOrigin
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/oauth")
public class OauthController {

    private final KakaoSocialService kakaoSocialService;
    private final ParentService parentService;
    private final TeacherService teacherService;
    private final RefreshTokenService refreshTokenService;


    @Operation(summary = "카카오 로그인 창 접근")
    @GetMapping("/kakao/login")
    public String getKakaologin() {
        return kakaoSocialService.getKakaoLogin();
    }

    @Operation(summary = "사용자 검증 (로그인 or 회원가입)")
    @GetMapping("/kakao/token/{code}")
    public ResponseEntity<ResultResponse> login(@PathVariable("code") String code) {
        LoginSuccessResponseDto loginSuccessResponseDto = kakaoSocialService.login(code);
        if(loginSuccessResponseDto.isTeacher()) {
            return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS, teacherService.login(loginSuccessResponseDto)));
        } else {
            return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS, parentService.login(loginSuccessResponseDto)));
        }
    }

//    @GetMapping("/token-refresh")
//    public ResponseEntity<ResultResponse> refreshToken(@RequestParam final String refreshToken) {
//        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS, parentService.refreshToken(refreshToken)));
//    }
//
//    @DeleteMapping("/delete")
//    public ResponseEntity<ResultResponse> deleteUser(final Principal principal) {
//        refreshTokenService.deleteRefreshToken(Long.valueOf(principal.getName()));
//        parentService.deleteUser(Long.valueOf(principal.getName()));
//        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS));
//    }
//
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ResultResponse> logout(final Principal principal) {
        refreshTokenService.deleteRefreshToken(Long.valueOf(principal.getName()));
        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS));
    }

}
