package bookShop.controller;

import bookShop.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import bookShop.model.UserResponse;
import bookShop.model.RegisterRequest;
import org.springframework.security.core.Authentication;
import bookShop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "Пользователи", description = "Управление пользователями (только для администратора)")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Получить список всех пользователей",
            description = "Доступно только для администратора"
    )

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Доступно только для администратора"
    )

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(
            summary = "Удалить пользователя по ID",
            description = "Доступно только для администратора"
    )

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(
            summary = "Создать пользователя",
            description = """
        Только для администратора. Создаёт нового пользователя с указанными параметрами (имя, пароль, роль).
        Может быть использовано для тестирования API и ручного создания пользователей.
        """
    )

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserResponse createUser(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @Operation(
            summary = "Обновить пользователя по ID",
            description = """
        Администратор может изменять имя, пароль и роль любого пользователя. 
        Обычный пользователь может изменять только свои имя и пароль.
        Роль пользователь менять не может.
        """
    )

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody RegisterRequest request, Authentication auth) {
        AppUser current = (AppUser) auth.getPrincipal();
        return userService.updateUser(id, request, current);
    }

    @Operation(
            summary = "Получить свои данные",
            description = "Текущий аутентифицированный пользователь"
    )

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public UserResponse getMe(Authentication auth) {
        AppUser current = (AppUser) auth.getPrincipal();
        return userService.getUserById(current.getId());
    }
}