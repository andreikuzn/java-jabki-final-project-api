package bookShop.model.response;

import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private int loyaltyPoints;
    private String loyaltyLevel;
    private String phone;
    private String email;
    private List<bookShop.model.response.LoanResponse> activeLoans;

    public static UserResponse from(bookShop.model.AppUser user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        dto.setLoyaltyPoints(user.getLoyaltyPoints());
        dto.setLoyaltyLevel(user.getLoyaltyLevel().getTitle());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setActiveLoans(
                user.getLoans() == null ? List.of() :
                        user.getLoans().stream()
                                .filter(l -> l.getReturnedDate() == null)
                                .map(bookShop.model.response.LoanResponse::from)
                                .collect(Collectors.toList())
        );
        return dto;
    }
}