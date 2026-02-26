package Girolook.com.GiroLook.models.enums;

public enum Category {
    DRESS("Vestido"),
    PANTS("Calça"),
    SHIRT("Camisa"),
    SHORTS("Short"),
    FOOTWEAR("Calçado"),
    SOCKS("Meia"),
    UNDERWEAR("Cueca"),
    PANTIES("Calcinha"),
    BRA("Sutiã"),
    ACCESSORIES("Acessórios"),
    COATS("Casacos");

    private final String description;

    Category(String description) {
        this.description = description;
    }
}
