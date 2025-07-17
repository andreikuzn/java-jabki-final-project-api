package bookShop.model;

import lombok.Getter;

@Getter
public enum LoyaltyLevel {
    NOVICE("Новичок", 1, 100, 5),
    STORY_SEEKER("Искатель историй", 3, 120, 10),
    PAGE_CONQUEROR("Покоритель страниц", 5, 150, 15),
    TOME_MASTER("Повелитель томов", 7, 200, 25),
    LIBRARY_MAGISTER("Магистр библиотеки", 10, 250, 30);

    private final String title;
    private final int maxBooks;
    private final double maxBookPrice;
    private final int maxDays;

    LoyaltyLevel(String title, int maxBooks, double maxBookPrice, int maxDays) {
        this.title = title;
        this.maxBooks = maxBooks;
        this.maxBookPrice = maxBookPrice;
        this.maxDays = maxDays;
    }

    public static LoyaltyLevel fromPoints(int points) {
        if (points >= 30) return LIBRARY_MAGISTER;
        if (points >= 20) return TOME_MASTER;
        if (points >= 10) return PAGE_CONQUEROR;
        if (points >= 5)  return STORY_SEEKER;
        return NOVICE;
    }
}