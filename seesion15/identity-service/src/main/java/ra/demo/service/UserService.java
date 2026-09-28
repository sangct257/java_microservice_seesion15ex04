package ra.demo.service;

import ra.demo.dto.request.UserLogin;
import ra.demo.dto.request.UserRegister;
import ra.demo.dto.response.JWTResponse;
import ra.demo.entity.User;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User save(User user);
    User update(Long userId,User user);
    void delete(Long userId);
    JWTResponse login(UserLogin userLogin);
    JWTResponse refreshToken(String refreshToken);
    User registerUser(UserRegister userRegister);
}
