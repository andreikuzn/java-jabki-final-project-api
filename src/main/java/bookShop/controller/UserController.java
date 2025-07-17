package bookShop.controller;

import bookShop.model.RegisterRequest;
import bookShop.model.UserResponse;
import bookShop.service.UserService;
import bookShop.model.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Пользователи", description = "Управление пользователями")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Список всех пользователей", description = "Только для администратора")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Профиль текущего пользователя", description = "Информация о текущем пользователе")
    @GetMapping("/me")
    public UserResponse getMe(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        return userService.getUserResponseById(userId);
    }

    @Operation(summary = "Обновить пользователя", description = "Обновить данные пользователя (имя, пароль). Роль может менять только админ")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody RegisterRequest request, Authentication auth) {
        return userService.updateUser(id, request, auth);
    }

    @Operation(summary = "Создать пользователя", description = "Создать нового пользователя (только для администратора)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserResponse createUser(@RequestBody RegisterRequest request) {
        return userService.createUser(request);
    }
}