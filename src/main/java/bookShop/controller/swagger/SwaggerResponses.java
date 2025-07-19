package bookShop.controller.swagger;

public class SwaggerResponses {

    public static final String STATUS_200_LIST = """
{
  "error": null,
  "message": null,
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
""";

    public static final String STATUS_200_MSG_USER = """
{
  "error": null,
  "message": "Пользователь успешно обновлён",
  "status": 200,
  "timestamp": "2025-07-21T14:10:00.000",
  "data": { ... }
}
""";

    public static final String STATUS_401 = """
{
  "error": "USER_NOT_AUTHENTICATED",
  "message": "Пользователь не авторизован в системе",
  "status": 401,
  "timestamp": "2025-07-21T14:10:00.000",
  "data": null
}
""";

    public static final String STATUS_403 = """
{
  "error": "FORBIDDEN",
  "message": "Действие запрещено: недостаточно прав",
  "status": 403,
  "timestamp": "2025-07-21T14:10:00.000",
  "data": null
}
""";

    public static final String STATUS_404_USER = """
{
  "error": "USER_NOT_FOUND",
  "message": "Пользователь не найден",
  "status": 404,
  "timestamp": "2025-07-21T14:10:00.000",
  "data": null
}
""";

    public static final String STATUS_400_VALIDATION = """
{
  "error": "VALIDATION_ERROR",
  "message": "Некорректные данные",
  "status": 400,
  "timestamp": "2025-07-21T14:10:00.000",
  "data": null
}
""";

    public static final String STATUS_500 = """
{
  "error": "INTERNAL_ERROR",
  "message": "Произошла внутренняя ошибка сервера",
  "status": 500,
  "timestamp": "2025-07-21T14:10:00.000",
  "data": null
}
""";

    public static final String STATUS_200_MSG_LOAN_ISSUE = """
{
  "error": null,
  "message": "Книга успешно выдана",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [ { ... } ]
}
""";

    public static final String STATUS_200_MSG_LOAN_RETURN = """
{
  "error": null,
  "message": "Книга успешно возвращена",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [ { ... } ]
}
""";

    public static final String STATUS_404_BOOK = """
{
  "error": "BOOK_NOT_FOUND",
  "message": "Книга не найдена",
  "status": 404,
  "timestamp": "2025-07-21T14:00:00.000",
  "data": null
}
""";

    public static final String STATUS_404_LOAN = """
{
  "error": "LOAN_NOT_FOUND",
  "message": "Выдача книги не найдена",
  "status": 404,
  "timestamp": "2025-07-21T14:00:00.000",
  "data": null
}
""";

    public static final String STATUS_400_LIMIT_EXCEEDED = """
{
  "error": "BOOK_LOAN_LIMIT_EXCEEDED",
  "message": "Превышен лимит книг для вашего уровня лояльности",
  "status": 400,
  "timestamp": "2025-07-21T14:00:00.000",
  "data": null
}
""";

    public static final String STATUS_400_BOOK_UNAVAILABLE = """
{
  "error": "BOOK_UNAVAILABLE",
  "message": "Нет доступных экземпляров книги",
  "status": 400,
  "timestamp": "2025-07-21T14:00:00.000",
  "data": null
}
""";

    public static final String STATUS_400_ALREADY_RETURNED = """
{
  "error": "LOAN_ALREADY_RETURNED",
  "message": "Книга уже возвращена",
  "status": 400,
  "timestamp": "2025-07-21T14:00:00.000",
  "data": null
}
""";

    public static final String STATUS_200_MSG_BOOK_ADD = """
{
  "error": null,
  "message": "Книга успешно добавлена",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [ { ... } ]
}
""";

    public static final String STATUS_400_ALREADY_EXISTS = """
{
  "error": "BOOK_ALREADY_EXISTS",
  "message": "Книга с таким названием и автором уже существует",
  "status": 400,
  "timestamp": "2025-07-21T14:00:00.000",
  "data": null
}
""";

    public static final String STATUS_200_MSG_BOOK_UPDATED = """
{
  "error": null,
  "message": "Книга успешно обновлена",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [ { ... } ]
}
""";

    public static final String STATUS_200_MSG_BOOK_DELETED = """
{
  "error": null,
  "message": "Книга удалена",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [ { ... } ]
}
""";
}