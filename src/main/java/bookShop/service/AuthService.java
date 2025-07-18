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
import bookShop.exception.*;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public void register(RegisterRequest request) {
        log.info("Регистрация пользователя: {}", request.getUsername());
        request.trimFields();
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        if (request.getRole() == Role.ADMIN) {
            int adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount >= 3) {
                throw new AdminLimitExceededException("В системе не может быть более 3-х админов");
            }
        }
        var violations = validator.validateProperty(request, "password");
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.iterator().next().getMessage());
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .loyaltyPoints(0)
                .loyaltyLevel(LoyaltyLevel.NOVICE)
                .build();
        userRepository.save(user);
        log.info("Пользователь [{}] успешно зарегистрирован", request.getUsername());
    }

    public AuthResponse login(AuthRequest request) {
        log.info("Попытка входа пользователя: {}", request.getUsername());
        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Неверный логин или пароль");
        }

        String token = jwtUtil.generateToken(new AppUserDetails(user));
        log.info("Пользователь [{}] успешно вошёл в систему", request.getUsername());
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