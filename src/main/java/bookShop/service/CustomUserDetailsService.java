package bookShop.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import bookShop.repository.AppUserRepository;
import bookShop.model.AppUserDetails;
import bookShop.model.AppUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AppUserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Попытка входа с несуществующим пользователем: [{}]", username);
                    return new UsernameNotFoundException("Пользователь не найден");
                });
        log.info("Пользователь [{}] успешно найден для авторизации", username);
        return new AppUserDetails(user);
    }
}