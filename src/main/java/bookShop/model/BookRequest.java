package bookShop.model;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    @NotBlank(message = "Название не должно быть пустым")
    @Size(min = 2, max = 32, message = "Название книги должно быть от 2 до 32 символов")
    private String title;
    @NotBlank(message = "Автор не должен быть пустым")
    @Size(min = 2, max = 32, message = "Имя автора должно быть от 2 до 32 символов")
    private String author;
    @NotNull(message = "Цена обязательна")
    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private Double price;
    @NotNull(message = "Количество экземпляров обязательно")
    @PositiveOrZero(message = "Количество экземпляров не может быть отрицательным")
    private Integer copiesAvailable;

    public void trimFields() {
        if (title != null) title = title.trim();
        if (author != null) author = author.trim();
    }
}