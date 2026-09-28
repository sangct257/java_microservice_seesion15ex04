package ra.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class JWTResponse {
    private String username;
    private Boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
}
