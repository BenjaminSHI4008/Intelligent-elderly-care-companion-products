package com.xiaoban.server.security;

<<<<<<< HEAD
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoban.server.common.Result;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.UserMapper;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
<<<<<<< HEAD
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);
<<<<<<< HEAD
            User user = userMapper.selectById(userId);
            if (user == null) {
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        Result.error(ResultCode.ACCOUNT_NOT_FOUND));
                return;
            }

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
            String role = jwtUtil.getRole(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

<<<<<<< HEAD
    private void writeJsonError(HttpServletResponse response, int status, Result<Void> body)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
