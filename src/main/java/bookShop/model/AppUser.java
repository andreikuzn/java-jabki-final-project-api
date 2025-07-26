package bookShop.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 32)
    private String username;
    @Column(nullable = false, length = 255)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Builder.Default
    private int loyaltyPoints = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoyaltyLevel loyaltyLevel = LoyaltyLevel.NOVICE;
    @OneToMany(mappedBy = "appUser", fetch = FetchType.EAGER)
    private List<Loan> loans;
    @Column(nullable = false, length = 11)
    private String phone;
    @Column(nullable = false, length = 255)
    private String email;
}