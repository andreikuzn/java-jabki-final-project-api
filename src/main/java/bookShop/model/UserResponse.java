package bookShop.model;

import lombok.*;

import lombok.Data;
import bookShop.model.AppUser;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String role;

    public static UserResponse from(AppUser user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        return dto;
    }
}