package bookShop.repository;

import bookShop.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    int countByRole(bookShop.model.Role role);
    boolean existsByUsername(String username);
}
