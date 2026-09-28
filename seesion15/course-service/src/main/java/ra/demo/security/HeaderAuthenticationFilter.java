package ra.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Đọc Header X-User-Role do Gateway gửi tới
        String userRole = request.getHeader("X-Auth-Role");

        if (StringUtils.hasText(userRole)) {
            // 2. Chuyển Role thành GrantedAuthority (Ví dụ: STUDENT hoặc INSTRUCTOR)
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(userRole)
            );

            // 3. Tạo Authentication object (lấy tạm Principal là "GatewayUser")
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("GatewayUser", null, authorities);

            // 4. Thiết lập Authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
