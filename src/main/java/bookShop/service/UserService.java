package bookShop.service;

import bookShop.model.Role;
import bookShop.model.AppUser;
import bookShop.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import bookShop.model.RegisterRequest;
import bookShop.model.UserResponse;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return appUserRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public void deleteUser(Long id) {
        appUserRepository.deleteById(id);
    }

    public UserResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN && appUserRepository.existsByRole(Role.ADMIN)) {
            throw new IllegalArgumentException("Админ уже существует");
        }
        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == null ? Role.USER : request.getRole());
        return UserResponse.from(appUserRepository.save(user));
    }

    public UserResponse updateUser(Long id, RegisterRequest request, AppUser currentUser) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (currentUser.getRole() != Role.ADMIN && !user.getId().equals(currentUser.getId())) {
            throw new SecurityException("Доступ запрещен");
        }
        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (currentUser.getRole() == Role.ADMIN && request.getRole() != null) {
            if (request.getRole() == Role.ADMIN && appUserRepository.existsByRole(Role.ADMIN) && user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Админ уже существует");
            }
            user.setRole(request.getRole());
        }
        return UserResponse.from(appUserRepository.save(user));
    }
}