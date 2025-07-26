package bookShop.apiTests.common;

import lombok.Data;
import java.util.List;

@Data
public class UserData {
    private int id;
    private String username;
    private String role;
    private String phone;
    private String email;
    private int loyaltyPoints;
    private String loyaltyLevel;
    private List<Object> activeLoans;
}