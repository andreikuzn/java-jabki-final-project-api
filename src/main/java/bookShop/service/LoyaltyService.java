package bookShop.service;

import bookShop.model.LoyaltyLevel;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoyaltyService {
    public LoyaltyLevel calculateLevel(int points) {
        log.debug("Рассчитан уровень лояльности для {} баллов", points);
        return LoyaltyLevel.fromPoints(points);
    }
}