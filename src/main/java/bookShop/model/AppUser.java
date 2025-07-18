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
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private int loyaltyPoints = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyLevel loyaltyLevel = LoyaltyLevel.NOVICE;
    @OneToMany(mappedBy = "appUser", fetch = FetchType.EAGER)
    private List<Loan> loans;
}