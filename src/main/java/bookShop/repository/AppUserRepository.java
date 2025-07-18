package bookShop.repository;

import bookShop.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import bookShop.model.Role;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    int countByRole(Role role);
    boolean existsByUsername(String username);
}
