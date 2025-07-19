package bookShop.service;

import bookShop.model.*;
import bookShop.repository.AppUserRepository;
import bookShop.repository.LoanRepository;
import bookShop.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public List<UserResponse> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> {
                    UserResponse response = UserResponse.from(user);
                    List<Loan> activeLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(user.getId());
                    response.setActiveLoans(activeLoans.stream()
                            .map(LoanResponse::from)
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());
        if (users.isEmpty()) {
            throw new UserNotFoundException("Пользователи не найдены");
        }
        return users;
    }

    public UserResponse getUserResponseById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        UserResponse response = UserResponse.from(user);
        List<Loan> activeLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(user.getId());
        response.setActiveLoans(activeLoans.stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList()));
        return response;
    }

    public UserResponse getUserResponseByUsername(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким username не найден"));
        UserResponse response = UserResponse.from(user);
        List<Loan> activeLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(user.getId());
        response.setActiveLoans(activeLoans.stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList()));
        return response;
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
        request.trimFields();
        AppUser user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        boolean isAdmin = userDetails.getRole() == Role.ADMIN;
        boolean isOwner = user.getId().equals(userDetails.getId());
        if (!isAdmin && !isOwner) {
            throw new ForbiddenActionException("Недостаточно прав для изменения пользователя");
        }

        if (!user.getUsername().equals(request.getUsername())) {
            throw new ForbiddenActionException("Изменение username запрещено");
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            var violations = validator.validateProperty(request, "password");
            if (!violations.isEmpty()) {
                throw new ValidationException(violations.iterator().next().getMessage());
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
        }

        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            if (!isAdmin) {
                throw new ForbiddenActionException("Роль могут менять только пользователи с ролью ADMIN");
            }
            Role oldRole = user.getRole();
            Role newRole = request.getRole();
            if (oldRole != Role.ADMIN && newRole == Role.ADMIN) {
                int adminCount = userRepository.countByRole(Role.ADMIN);
                if (adminCount >= 3) {
                    throw new AdminLimitExceededException("В системе не может быть более 3-х админов");
                }
            }
            user.setRole(newRole);
        }

        AppUser saved = userRepository.save(user);
        log.info("Пользователь [{}] успешно обновлён", id);
        return getUserResponseById(saved.getId());
    }

    public UserResponse createUser(RegisterRequest request) {
        log.info("Попытка создать пользователя: username={}", request.getUsername());
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
        return getUserResponseById(saved.getId());
    }
}