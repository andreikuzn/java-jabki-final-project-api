package bookShop.model;

import lombok.*;
import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    @NotBlank(message = "Название книги не должно быть пустым")
    @Size(max = 128, message = "Название книги не должно превышать 128 символов")
    private String title;
    @NotBlank(message = "Автор не должен быть пустым")
    @Size(max = 64, message = "Автор не должен превышать 64 символа")
    private String author;
    @DecimalMin(value = "0.0", inclusive = false, message = "Цена книги должна быть больше 0")
    private double price;
    @Min(value = 0, message = "Количество экземпляров не может быть отрицательным")
    private int copiesAvailable;
}