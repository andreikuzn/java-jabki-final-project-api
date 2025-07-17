package bookShop.service;

import bookShop.model.RegisterRequest;
import bookShop.model.AppUser;
import bookShop.model.Role;
import bookShop.repository.AppUserRepository;
import bookShop.model.AppUserDetails;
import bookShop.model.LoyaltyLevel;
import bookShop.exception.AdminLimitExceededException;
import bookShop.exception.UserNotFoundException;
import bookShop.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import bookShop.model.AuthResponse;
import bookShop.model.AuthRequest;
import bookShop.exception.InvalidCredentialsException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) >= 3) {
            throw new AdminLimitExceededException("Нельзя регистрировать больше 3-х администраторов");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .loyaltyPoints(0)
                .loyaltyLevel(LoyaltyLevel.NOVICE)
                .build();
        userRepository.save(user);
    }

    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Неверный логин или пароль");
        }

        String token = jwtUtil.generateToken(new AppUserDetails(user));
        return new AuthResponse(token, user.getId(), user.getRole().name());
    }

    public AppUserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(AppUserDetails::new)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    public boolean adminExists() {
        return userRepository.countByRole(Role.ADMIN) >= 3;
    }
}