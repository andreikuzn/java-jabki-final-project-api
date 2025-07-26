package bookShop.apiTests.common;

import bookShop.model.AppUser;
import bookShop.model.Role;
import bookShop.repository.AppUserRepository;
import java.util.List;

import static bookShop.model.LoyaltyLevel.NOVICE;
import static org.junit.jupiter.api.Assertions.*;

public class DbResponseAssert {

    public static void assertUserCorrectInDb(AppUserRepository repo,
                                             String expectedUsername,
                                             String expectedEmail,
                                             String expectedPhone,
                                             Role expectedRole) {
        AppUser dbUser = repo.findByUsername(expectedUsername).orElse(null);
        assertNotNull(dbUser, "Пользователь не найден в БД");
        assertEquals(expectedUsername, dbUser.getUsername());
        assertEquals(expectedEmail, dbUser.getEmail());
        assertEquals(expectedPhone, dbUser.getPhone());
        assertEquals(expectedRole, dbUser.getRole());
        assertEquals(0, dbUser.getLoyaltyPoints());
        assertEquals(NOVICE.getTitle(), dbUser.getLoyaltyLevel().getTitle());
        assertNotNull(dbUser.getId());
        assertNotNull(dbUser.getPassword(), "Пароль должен быть сохранён в БД");
    }

    public static void assertUserDeletedInDb(AppUserRepository repo, String userId) {
        AppUser deletedUser = repo.findById(Long.valueOf(userId)).orElse(null);
        assertNull(deletedUser, "Пользователь не был удалён из БД");
    }

    public static void assertUserNotExistsInDb(AppUserRepository repo, String username) {
        AppUser dbUser = repo.findByUsername(username).orElse(null);
        assertNull(dbUser, "Пользователь не должен был появиться в БД, но он найден!");
    }

    public static void assertUserCountByField(AppUserRepository repo, String field, String value, int expectedCount) {
        List<AppUser> users = repo.findAll().stream()
                .filter(user -> {
                    switch (field) {
                        case "email": return user.getEmail().equalsIgnoreCase(value);
                        case "phone": return user.getPhone().equals(value);
                        case "username": return user.getUsername().equals(value);
                        default: throw new IllegalArgumentException("Неизвестное поле: " + field);
                    }
                })
                .toList();
        org.junit.jupiter.api.Assertions.assertEquals(
                expectedCount, users.size(),
                "В БД должно быть " + expectedCount + " пользователь(я/ей) с " + field + " = " + value
        );
    }
}

