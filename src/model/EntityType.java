package model;

public enum EntityType {
    ITEM("item"),
    SUPPLIER("supplier"),
    DAILY_SALES_ENTRY("daily_sales_entry"),
    PURCHASE_REQUISITION("purchase_requisition");

    private final String displayName;

    EntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
