package bookShop.service;

import bookShop.model.*;
import bookShop.repository.AppUserRepository;
import bookShop.repository.LoanRepository;
import bookShop.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import bookShop.model.response.UserResponse;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_ADMIN_COUNT = 3;

    private final AppUserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public List<UserResponse> getAllUsers() {
        List<AppUser> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new UserNotFoundException("Пользователи не найдены");
        }
        return users.stream()
                .map(this::toUserResponseWithActiveLoans)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        AppUser user = findUserByIdOrThrow(id);
        return toUserResponseWithActiveLoans(user);
    }

    public List<UserResponse> getUsersByUsername(String username) {
        username = (username != null) ? username.trim() : null;
        List<AppUser> users = userRepository.findByUsernameIgnoreCaseLike(username);
        if (users.isEmpty()) {
            throw new UserNotFoundException("Пользователи с таким username не найдены");
        }
        return users.stream().map(this::toUserResponseWithActiveLoans).collect(Collectors.toList());
    }

    public UserResponse getUserByUsername(String username) {
        username = (username != null) ? username.trim() : null;
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким username не найден"));
        return toUserResponseWithActiveLoans(user);
    }

    public void deleteUser(Long id) {
        log.warn("Попытка удаления админом пользователя [{}]", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException();
        }
        userRepository.deleteById(id);
        log.info("Пользователь [{}] удалён админом", id);
    }

    public UserResponse updateUser(Long id, RegisterRequest request, AppUserDetails userDetails) {
        log.info("Пользователь [{}] инициировал обновление пользователя [{}]", userDetails.getUsername(), id);
        trimRequestFields(request);
        AppUser user = findUserByIdOrThrow(id);
        checkUpdateUserRights(user, userDetails, request);
        updateUserFields(user, request, userDetails);
        AppUser saved = userRepository.save(user);
        log.info("Пользователь [{}] успешно обновлён", id);
        return toUserResponseWithActiveLoans(saved);
    }

    public UserResponse createUser(RegisterRequest request) {
        log.info("Попытка создать пользователя: username={}", request.getUsername());
        trimRequestFields(request);
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        if (request.getRole() == Role.ADMIN) {
            checkAdminLimit();
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .email(request.getEmail())
                .loyaltyPoints(0)
                .loyaltyLevel(LoyaltyLevel.NOVICE)
                .build();
        AppUser saved = userRepository.save(user);
        log.info("Пользователь создан: username={}, ID={}", saved.getUsername(), saved.getId());
        return toUserResponseWithActiveLoans(saved);
    }

    private AppUser findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    private UserResponse toUserResponseWithActiveLoans(AppUser user) {
        bookShop.model.response.UserResponse response = bookShop.model.response.UserResponse.from(user);
        List<Loan> activeLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(user.getId());
        response.setActiveLoans(
                activeLoans.stream()
                        .map(bookShop.model.response.LoanResponse::from)
                        .collect(Collectors.toList())
        );
        return response;
    }

    private void trimRequestFields(RegisterRequest request) {
        if (request != null) request.trimFields();
    }

    private void checkUpdateUserRights(AppUser user, AppUserDetails userDetails, RegisterRequest request) {
        boolean isAdmin = userDetails.getRole() == Role.ADMIN;
        boolean isOwner = user.getId().equals(userDetails.getId());
        if (!isAdmin && !isOwner) {
            throw new ForbiddenActionException("Недостаточно прав для изменения пользователя");
        }
        if (!user.getUsername().equals(request.getUsername())) {
            throw new ForbiddenActionException("Изменение username запрещено");
        }
        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            if (!isAdmin) {
                throw new ForbiddenActionException("Роль могут менять только пользователи с ролью ADMIN");
            }
        }
    }

    private void updateUserFields(AppUser user, RegisterRequest request, AppUserDetails userDetails) {
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            var violations = validator.validateProperty(request, "password");
            if (!violations.isEmpty()) {
                throw new ValidationException(violations.iterator().next().getMessage());
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        boolean isAdmin = userDetails.getRole() == Role.ADMIN;
        boolean isOwner = user.getId().equals(userDetails.getId());
        if (request.getPhone() != null) {
            if (isOwner || isAdmin) {
                user.setPhone(request.getPhone());
            }
        }
        if (request.getEmail() != null) {
            if (isOwner || isAdmin) {
                user.setEmail(request.getEmail());
            }
        }
        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            if (isAdmin) {
                if (user.getRole() != Role.ADMIN && request.getRole() == Role.ADMIN) {
                    checkAdminLimit();
                }
                user.setRole(request.getRole());
            }
        }
    }

    private void checkAdminLimit() {
        int adminCount = userRepository.countByRole(Role.ADMIN);
        if (adminCount >= MAX_ADMIN_COUNT) {
            throw new AdminLimitExceededException("В системе не может быть более " + MAX_ADMIN_COUNT + "-х админов");
        }
    }
}