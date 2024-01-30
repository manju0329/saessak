package com.ssafy.saessak.oauth.service;

import com.ssafy.saessak.oauth.client.KakaoApiClient;
import com.ssafy.saessak.oauth.client.KakaoAuthApiClient;
import com.ssafy.saessak.oauth.dto.kakao.KakaoAccessTokenResponse;
import com.ssafy.saessak.oauth.dto.kakao.KakaoUserResponse;
import com.ssafy.saessak.oauth.exception.BadRequestException;
import com.ssafy.saessak.oauth.exception.ErrorMessage;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoSocialService {

    private static final String AUTH_CODE = "authorization_code";

    private final KakaoApiClient kakaoApiClient;
    private final KakaoAuthApiClient kakaoAuthApiClient;
    private final ParentService parentService;
    private final TeacherService teacherService;

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.client.secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect.url}")
    private String KAKAO_REDIRECT_URL;

    private final static String KAKAO_AUTH_URI = "https://kauth.kakao.com";
    private final static String KAKAO_API_URI = "https://kapi.kakao.com";

    public String getKakaoLogin() {
        return KAKAO_AUTH_URI+"/oauth/authorize"
                +"?client_id="+KAKAO_CLIENT_ID
                +"&redirect_uri="+KAKAO_REDIRECT_URL
                +"&response_type=code";
    }

    @Transactional
    public KakaoUserResponse login(String authorizationCode) {
        String accessToken = "";
        try {
            // 인가 코드로 Access Token + Refresh Token 받아오기
            accessToken = getOAuth2Authentication(authorizationCode);
        } catch (FeignException e) {
            throw new BadRequestException(ErrorMessage.AUTHENTICATION_CODE_EXPIRED);
        }
        // Access Token으로 유저 정보 불러오기
        return getUserInfo(accessToken);
    }


    public String getOAuth2Authentication (final String authorizationCode) {
        CompletableFuture<KakaoAccessTokenResponse> future = CompletableFuture.supplyAsync(
                () -> kakaoAuthApiClient.getOAuth2AccessToken(
                        AUTH_CODE,
                        KAKAO_CLIENT_ID,
                        KAKAO_CLIENT_SECRET,
                        KAKAO_REDIRECT_URL,
                        authorizationCode
                ));
        KakaoAccessTokenResponse tokenResponse = future.join();
        return tokenResponse.accessToken();
    }

    private KakaoUserResponse getUserInfo (final String accessToken ) {
        return kakaoApiClient.getUserInformation("Bearer " + accessToken);
    }

}
