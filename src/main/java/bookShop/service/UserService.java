package bookShop.service;

import bookShop.model.RegisterRequest;
import bookShop.model.AppUserDetails;
import bookShop.model.UserResponse;
import bookShop.model.AppUser;
import bookShop.model.Role;
import bookShop.model.LoyaltyLevel;
import bookShop.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import bookShop.repository.LoanRepository;
import bookShop.exception.UserNotFoundException;
import bookShop.exception.UserAlreadyExistsException;
import bookShop.exception.AdminLimitExceededException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoyaltyService loyaltyService;

    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse getMe(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        AppUser user = userDetails.getUser();
        return UserResponse.from(user);
    }

    public UserResponse updateUser(Long id, RegisterRequest request, Authentication auth) {
        AppUser current = (AppUser) auth.getPrincipal();
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        if (current.getRole() == Role.ADMIN && request.getRole() != null) {
            if (request.getRole() == Role.ADMIN && appUserRepository.countByRole(Role.ADMIN) >= 3) {
                throw new AdminLimitExceededException("Нельзя иметь больше 3-х администраторов");
            }
            user.setRole(request.getRole());
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setLoyaltyLevel(loyaltyService.calculateLevel(user.getLoyaltyPoints()));

        return UserResponse.from(appUserRepository.save(user));
    }

    public UserResponse createUser(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        if (request.getRole() == Role.ADMIN && appUserRepository.countByRole(Role.ADMIN) >= 3) {
            throw new AdminLimitExceededException("Нельзя регистрировать больше 3-х администраторов");
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .loyaltyPoints(0)
                .loyaltyLevel(LoyaltyLevel.NOVICE)
                .build();
        return UserResponse.from(appUserRepository.save(user));
    }

    public UserResponse getUserResponseById(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        // Подгрузим все активные выдачи пользователя
        List<bookShop.model.Loan> activeLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(userId);

        // Соберём DTO
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setLoyaltyPoints(user.getLoyaltyPoints());
        response.setLoyaltyLevel(user.getLoyaltyLevel().getTitle());
        response.setActiveLoans(activeLoans.stream().map(bookShop.model.LoanResponse::from).collect(Collectors.toList()));
        return response;
    }
}