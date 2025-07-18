package bookShop.service;

import bookShop.model.*;
import bookShop.repository.AppUserRepository;
import bookShop.repository.LoanRepository;
import bookShop.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserResponse response = UserResponse.from(user);
                    List<Loan> activeLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(user.getId());
                    response.setActiveLoans(activeLoans.stream()
                            .map(LoanResponse::from)
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public UserResponse getUserResponseById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        UserResponse response = UserResponse.from(user);
        List<Loan> activeLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(user.getId());
        response.setActiveLoans(activeLoans.stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList()));
        return response;
    }

    public UserResponse updateUser(Long id, RegisterRequest request, AppUserDetails userDetails) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        // Проверка прав: админ или владелец (по id)
        boolean isAdmin = userDetails.getRole() == Role.ADMIN;
        boolean isOwner = user.getId().equals(userDetails.getId());
        if (!isAdmin && !isOwner) {
            throw new ForbiddenActionException("Недостаточно прав для изменения пользователя");
        }
        // Username: если изменяется, то проверяем уникальность
        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
            }
            user.setUsername(request.getUsername());
        }
        // Пароль: всегда обновлять, если передан новый
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        // Роль: менять может только админ
        if (isAdmin && request.getRole() != null) {
            user.setRole(request.getRole());
        }
        AppUser saved = userRepository.save(user);
        return getUserResponseById(saved.getId());
    }

    public UserResponse createUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .loyaltyPoints(0)
                .loyaltyLevel(LoyaltyLevel.NOVICE)
                .build();
        AppUser saved = userRepository.save(user);
        return getUserResponseById(saved.getId());
    }
}