package bookShop.model;

import lombok.Data;

@Data
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private int copiesAvailable;
    private double price;

    public static BookResponse from(Book book) {
        BookResponse dto = new BookResponse();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setCopiesAvailable(book.getCopiesAvailable());
        dto.setPrice(book.getPrice());
        return dto;
    }
}