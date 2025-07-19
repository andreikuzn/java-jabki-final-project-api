package bookShop.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String title;
    @Column(nullable = false, length = 32)
    private String author;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private int copiesAvailable;
}