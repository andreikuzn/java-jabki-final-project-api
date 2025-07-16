package bookShop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import bookShop.model.AppUser;
import bookShop.model.Role;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByRole(Role role);
    Optional<AppUser> findByUsername(String username);
}

