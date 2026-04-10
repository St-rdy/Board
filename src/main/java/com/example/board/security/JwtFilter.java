package com.example.board.security;

import com.example.board.dto.JwtUserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try{
            String token = resolveToken(request);

            if (token != null && jwtUtil.validateToken(token)) {

                JwtUserInfo jwtUserInfo = jwtUtil.getUserIdFromToken(token);

                // 첫 번째 파라미터(Principal)에 유저정보 입력 (권한 Role이 필요 없다면 뒤는 null)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(jwtUserInfo, null, null);

                // 컨트롤러에서 @AuthenticationPrincipal로 꺼내 쓸 수 있게 시큐리티 보관소에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                //삭제시점 : 어떠한 형태든 클라이언트에 응답이 가면(요청 1회)
            }

            // 필터 통과시키기
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }

    // HTTP 요청의 'Authorization' 헤더에서 'Bearer ' 글자를 떼어내는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
