package com.steam.modeni.config;

import com.steam.modeni.domain.entity.User;
import com.steam.modeni.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // JWT 토큰 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
            }
        }

        // 토큰이 유효하고 현재 SecurityContext에 인증 정보가 없는 경우
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 토큰 유효성 검증
                if (jwtUtil.validateToken(token)) {
                    // 사용자 정보 조회
                    User user = userService.findByUserId(username);
                    
                    // UserDetails 구현체 생성
                    UserDetails userDetails = createUserDetails(user);
                    
                    // Authentication 객체 생성
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("JWT 인증 성공: {}", username);
                }
            } catch (Exception e) {
                log.warn("JWT 인증 처리 실패: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails createUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
            user.getUserId(),
            "", // 패스워드는 JWT 인증에서 사용하지 않음
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
