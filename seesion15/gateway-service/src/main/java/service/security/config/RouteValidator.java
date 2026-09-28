package service.security.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

	public static final List<String> openApiEndpoints = List.of(
			"/auth-service/api/auth/login",
			"/auth-service/api/auth/register",
			"/auth-service/api/auth/refresh-token",
			"/eureka");

	public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
			.stream()
			.noneMatch(uri -> request.getURI().getPath().contains(uri));

	public boolean isAuthorized(String path, String method, List<String> roles) {
		for (RouteRule rule : AUTHORIZATION_RULES) {
			if (rule.matches(path, method)) {
				if (!rule.isAuthorized(roles)) {
					return false;
				}
			}
		}
		return true;
	}

	private static final List<RouteRule> AUTHORIZATION_RULES = List.of(
			// Toàn bộ phương thức truy cập vào /api/v1/categories/** đều yêu cầu ROLE_ADMIN
			new RouteRule("/product-service/api/coures/**", List.of("POST","PUT","PATH","DELETE"), List.of("ROLE_ADMIN")),
			new RouteRule("/product-service/api/coures/**", List.of("GET"), List.of())

	);
}
