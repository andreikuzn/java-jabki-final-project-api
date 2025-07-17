package bookShop.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private AppUser appUser;
    @ManyToOne(optional = false)
    private Book book;
    @Column(nullable = false)
    private LocalDate loanDate;
    @Column(nullable = false)
    private LocalDate dueDate;
    private LocalDate returnedDate;
}
