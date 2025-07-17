package bookShop.service;

import bookShop.model.LoyaltyLevel;
import org.springframework.stereotype.Service;

@Service
public class LoyaltyService {
    public LoyaltyLevel calculateLevel(int points) {
        return LoyaltyLevel.fromPoints(points);
    }
}