package bookShop.model;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    @NotBlank(message = "Название не должно быть пустым")
    private String title;
    @NotBlank(message = "Автор не должен быть пустым")
    private String author;
    @NotNull(message = "Цена обязательна")
    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private Integer price;
    @NotNull(message = "Количество экземпляров обязательно")
    @PositiveOrZero(message = "Количество экземпляров не может быть отрицательным")
    private Integer copiesAvailable;

    public void trimFields() {
        if (title != null) title = title.trim();
        if (author != null) author = author.trim();
    }
}