package service.security.fillter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import service.security.config.RouteValidator;
import service.security.utils.JwtUtils;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private final RouteValidator routeValidator;
    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (routeValidator.isSecured.test(request)) {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || authHeader.isEmpty()) {
                log.error("Missing Authorization Header for secure path: {}", request.getURI().getPath());
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing authorization header");
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.error("Không đúng định dạng chuỗi jwt token: {}", authHeader);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid authorization header format");
            }

            String token = authHeader.substring(7);

            // Validate token
            if (!jwtUtils.validateToken(token)) {
                log.error("Invalid/Expired JWT token");
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }

            // Extract roles
            List<String> roles = jwtUtils.extractRoles(token);
            String username = jwtUtils.extractUsername(token);
            log.info("Request by user: {}, roles: {}", username, roles);

            // Apply authorization checks (Phân quyền)
            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            if (!routeValidator.isAuthorized(path, method, roles)) {
                log.warn("User {} unauthorized to call API {} {}", username, method, path);
                return onError(exchange, HttpStatus.FORBIDDEN, "Access denied: Unauthorized access");
            }

            // Forward user headers to downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Auth-User-Id", username)
                    .header("X-Auth-Roles", roles != null ? String.join(",", roles) : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    // Hàm trả về Response lỗi 401 Unauthorized
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String err) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
