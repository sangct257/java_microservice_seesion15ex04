package ra.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.demo.dto.request.RefreshTokenRequest;
import ra.demo.dto.request.UserLogin;
import ra.demo.dto.request.UserRegister;
import ra.demo.dto.response.ApiResponse;
import ra.demo.dto.response.JWTResponse;
import ra.demo.entity.User;
import ra.demo.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody UserRegister userRegister) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Đăng ký tài khoản " + userRegister.getUsername() + " thành công",
                userService.registerUser(userRegister),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JWTResponse>> login(@RequestBody UserLogin userLogin) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Đăng nhập thành công",
                userService.login(userLogin),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JWTResponse>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Đã cấp lại access token mới",
                userService.refreshToken(refreshTokenRequest.getRefreshToken()),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

}
