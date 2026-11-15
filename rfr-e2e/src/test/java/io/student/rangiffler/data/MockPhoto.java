package io.student.rangiffler.data;

public enum MockPhoto {
    AMSTERDAM("Netherlands", "Амстердам!", true, 0),
    PARIS("France", "Париж!", true, 2),
    PETERSBURG("Russian Federation", "Петербург!", true, 0),
    NEW_YORK("United States", "Нью-Йорк!", true, 1),
    PRAGUE("Czech Republic", "Старая Прага завораживает!", false, 0),
    MUNICH("Germany", "Мюнхен - мой любимый город!", false, 0),
    BANGKOK("Thailand", "Обожаю Таиланд!", false, 1);

    private final String country;
    private final String description;
    private final boolean own;
    private final int likes;

    MockPhoto(String country, String description, boolean own, int likes) {
        this.country = country;
        this.description = description;
        this.own = own;
        this.likes = likes;
    }

    public String country() {
        return country;
    }

    public String description() {
        return description;
    }

    public boolean own() {
        return own;
    }

    public int likes() {
        return likes;
    }
}
