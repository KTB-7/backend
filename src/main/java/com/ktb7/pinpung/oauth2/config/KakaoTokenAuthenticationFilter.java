package com.ktb7.pinpung.oauth2.config;

import com.ktb7.pinpung.entity.User;
import com.ktb7.pinpung.exception.common.CustomException;
import com.ktb7.pinpung.exception.common.ErrorCode;
import com.ktb7.pinpung.oauth2.dto.KakaoTokenInfoResponseDto;
import com.ktb7.pinpung.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class KakaoTokenAuthenticationFilter extends OncePerRequestFilter {

    private final String kakaoUserInfoUrl = "https://kapi.kakao.com/v1/user/access_token_info";
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        log.info("Received URI: " + uri); // 초기 요청 경로 확인
        // 헤더에서 Authorization 값 추출
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // "Bearer " 이후의 값 추출
            log.info("Received Token: {}", token);
        } else {
            log.warn("Authorization header is missing or does not start with 'Bearer '");
        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            log.info("{}", token);

            try {
                // socialId로 userId 찾기
                Long socialId = validateTokenAndExtractUserId(token);
                log.info("Validate user id {}", socialId);
                User user = userRepository.findBySocialId(socialId)
                        .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

                Long userId = user.getUserId();
                if (userId != null) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (CustomException e) {
                log.error("Authentication failed for token: {}, error: {}", token, e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 로그인 없이 접근 가능한 기타 URL 경로 설정
        return
                requestURI.startsWith("/login") ||
                requestURI.startsWith("/login/oauth2/code/kakao") ||
                requestURI.startsWith("/favicon.ico") ||
                requestURI.startsWith("/logout-success") ||
                requestURI.startsWith("/cdn-cgi/**") ||
                requestURI.startsWith("/api/test");
    }


    private Long validateTokenAndExtractUserId(String token) {
        WebClient webClient = WebClient.builder()
                .baseUrl(kakaoUserInfoUrl)
                .build();

        KakaoTokenInfoResponseDto kakaoTokenInfoResponseDto = webClient
                .get()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        Mono.error(new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN_OR_SOCIAL_ID, ErrorCode.INVALID_TOKEN_OR_SOCIAL_ID.getMsg()))
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        Mono.error(new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMsg()))
                )
                .bodyToMono(KakaoTokenInfoResponseDto.class)
                .block();

        if (kakaoTokenInfoResponseDto == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN_OR_SOCIAL_ID, ErrorCode.INVALID_TOKEN_OR_SOCIAL_ID.getMsg());
        }
        log.info("{}", kakaoTokenInfoResponseDto.getExpires_in());
        return kakaoTokenInfoResponseDto.getId();
    }
}