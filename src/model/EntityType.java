package model;

public enum EntityType {
    ITEM("item"),
    SUPPLIER("supplier"),
    STOCK("stock");

    private final String displayName;

    EntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
