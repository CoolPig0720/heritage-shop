package com.heritage.security;

import com.heritage.security.CustomUserDetails;
import com.heritage.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        log.info("请求路径: {}", request.getRequestURI());
        log.info("Authorization Header: {}", authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
            log.info("提取的用户名: {}", username);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                CustomUserDetails userDetails = (CustomUserDetails) this.userDetailsService.loadUserByUsername(username);
                log.info("用户详情: {}", userDetails);
                log.info("用户权限: {}", userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.info("Token 验证成功，已设置认证信息");
                } else {
                    log.warn("Token 验证失败");
                }
            } catch (Exception e) {
                log.error("Token 验证异常", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
