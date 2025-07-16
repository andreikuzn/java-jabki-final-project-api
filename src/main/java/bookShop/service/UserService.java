package bookShop.service;

import bookShop.model.Role;
import bookShop.model.AppUser;
import bookShop.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService {

    @Autowired
    private AppUserRepository userRepo;
    @Autowired
    private PasswordEncoder encoder;

    public AppUser registerUser(String username, String password) {
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("User already exists");
        }
        AppUser appUser = AppUser.builder()
                .username(username)
                .password(encoder.encode(password))
                .roles(Collections.singleton(Role.USER))
                .build();
        return userRepo.save(appUser);
    }

    public AppUser getUserByUsername(String username) {
        return userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }
}