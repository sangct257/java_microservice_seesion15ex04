package ra.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ra.demo.dto.request.UserLogin;
import ra.demo.dto.request.UserRegister;
import ra.demo.dto.response.JWTResponse;
import ra.demo.entity.RefreshToken;
import ra.demo.entity.User;
import ra.demo.repository.RefreshTokenRepository;
import ra.demo.repository.UserRepository;
import ra.demo.security.jwt.JwtProvider;
import ra.demo.security.principal.CustomerUserDetailService;
import ra.demo.security.principal.CustomerUserDetails;
import ra.demo.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomerUserDetailService customerUserDetailService;

    @Value("${jwt-refresh-expiration}")
    private long jwtRefreshExpiration;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Không tồn tại user " + id));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(Long userId, User user) {
        userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Không tồn tại user " + userId));
        return userRepository.save(user);
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public JWTResponse login(UserLogin userLogin) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.getUsername(),userLogin.getPassword()));

            CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();

            String accessToken = jwtProvider.generateToken(customerUserDetails);

            RefreshToken refreshToken = RefreshToken.builder()
                    .username(customerUserDetails.getUsername())
                    .expirationDate(Instant.now().plusMillis(jwtRefreshExpiration))
                    .refreshToken(UUID.randomUUID().toString())
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(refreshToken);

            return JWTResponse.builder()
                    .username(customerUserDetails.getUsername())
                    .enabled(customerUserDetails.getEnabled())
                    .authorities(customerUserDetails.getAuthorities())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getRefreshToken())
                    .build();
        } catch (Exception e) {
            log.error("Username hoặc password không đúng ", e.getMessage());

            throw new UsernameNotFoundException("Username hoặc password không đúng "+ e.getMessage());
        }
    }

    @Override
    public JWTResponse refreshToken(String refreshToken) {
        RefreshToken objRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchElementException("Refresh Token không tồn tại"));
        if (objRefreshToken.getRevoked()){
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }

        if (objRefreshToken.getExpirationDate().isBefore(Instant.now())){
            throw new RuntimeException("Refresh token đã hết hạn");
        }

        CustomerUserDetails customerUserDetails = (CustomerUserDetails) customerUserDetailService.loadUserByUsername(objRefreshToken.getUsername());

        String accessToken = jwtProvider.generateToken(customerUserDetails);

        return JWTResponse.builder()
                .username(customerUserDetails.getUsername())
                .enabled(customerUserDetails.getEnabled())
                .authorities(customerUserDetails.getAuthorities())
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
    }

    @Override
    public User registerUser(UserRegister userRegister) {
        User user = User.builder()
                .username(userRegister.getUsername())
                .password(passwordEncoder.encode(userRegister.getPassword()))
                .enabled(true)
                .build();
        return userRepository.save(user);
    }
}
