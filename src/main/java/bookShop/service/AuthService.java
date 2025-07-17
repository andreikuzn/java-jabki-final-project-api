package bookShop.service;

import bookShop.model.RegisterRequest;
import bookShop.model.AuthRequest;
import bookShop.model.AppUser;
import bookShop.model.Role;
import bookShop.model.AppUserDetails;
import bookShop.model.AuthResponse;
import bookShop.repository.AppUserRepository;
import  bookShop.model.AuthFullResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;

    public AuthFullResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            throw new IllegalArgumentException("Админ уже существует");
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        userRepository.save(user);

        AppUserDetails userDetails = new AppUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return new AuthFullResponse(token, user.getId(), user.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Введен не верный пароль");
        }
        String token = jwtService.generateToken(new AppUserDetails(user));
        return new AuthResponse(token, user.getId(), user.getRole().name());
    }

    public boolean adminExists() {
        return userRepository.existsByRole(Role.ADMIN);
    }
}