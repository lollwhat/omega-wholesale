package model;

public class Stock {
    private String id;
    private String itemCode;
    private String name;
    private int currentStock;
    private int minStock;
    private int maxStock;
    private String status;
    private String lastUpdateDate;

    public Stock(String id, String itemCode, String name, int currentStock, int minStock, int maxStock, String status,
                 String lastUpdateDate) {
        this.id = id;
        this.name = name;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.maxStock = maxStock;
        this.status = status;
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getId() {return id;};
    public void setId(String id) {this.id = id;};

    public String getItemCode() {return itemCode;};
    public void setItemCode(String itemCode) {this.itemCode = itemCode;};

    public String getName() {return name;};
    public void setName(String name) {this.name = name;};

    public int getCurrentStock() {return currentStock;};
    public void setCurrentStock(int currentStock) {this.currentStock = currentStock;}

    public int getMinStock() {return minStock;};
    public void setMinStock(int minStock) {this.minStock = minStock;}

    public int getMaxStock() {return maxStock;};
    public void setMaxStock(int maxStock) {this.maxStock = maxStock;}

    public String getStatus() {return status;};
    public void setStatus(String status) {this.status = status;}

    public String getLastUpdateDate() {return lastUpdateDate;};
    public void setLastUpdateDate(String lastUpdateDate) {this.lastUpdateDate = lastUpdateDate;}

    @Override
    public String toString() {
        return String.join(",",
                id,
                itemCode,
                name,
                String.valueOf(currentStock),
                String.valueOf(minStock),
                String.valueOf(maxStock),
                status,
                lastUpdateDate
                );
    }
}
