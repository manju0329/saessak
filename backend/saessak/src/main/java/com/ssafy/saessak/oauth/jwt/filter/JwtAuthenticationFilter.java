package com.ssafy.saessak.oauth.jwt.filter;

import com.ssafy.saessak.exception.code.ExceptionCode;
import com.ssafy.saessak.oauth.authentication.UserAuthentication;
import com.ssafy.saessak.oauth.jwt.JwtTokenProvider;
import com.ssafy.saessak.oauth.jwt.JwtValidationType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.ssafy.saessak.oauth.jwt.JwtValidationType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TYPE = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String token = getJwtFromRequest(request);
            if (jwtTokenProvider.validateToken(token) == VALID_JWT_TOKEN) {
                Long userId = jwtTokenProvider.getUserFromJwt(token);
                // authentication 객체 생성 -> principal에 유저정보를 담는다.
                UserAuthentication authentication = new UserAuthentication(userId.toString(), null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception exception) {
            try {
                throw new Exception();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }


    private void sendError(ExceptionCode exceptionCode, HttpServletResponse response) throws IOException {
        response.setStatus(exceptionCode.getStatus());
        response.setHeader("Content-Type", "application/xml");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("utf-8");

        String code = exceptionCode.getCode();
        String message = exceptionCode.getMessage();
        String jsonErrorResponse = String.format("{\"code\": \"%s\", \"message\": \"%s\"}", code, message);
        response.getWriter().write(jsonErrorResponse);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
            return bearerToken.substring(BEARER_TYPE.length());
        }
        return null;
    }
}
