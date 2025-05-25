package model;

public enum RoleName {
    Administrator,
    SalesManager,
    PurchaseManager,
    InventoryManager,
    FinanceManager;


    public static String getRoleName(String roleCode) {
        switch (roleCode){
            case "AM" :
                return Administrator.name();
            case "SM":
                return SalesManager.name();
            case "PM":
                return PurchaseManager.name();
            case "IM":
                return InventoryManager.name();
            case "FM":
                return FinanceManager.name();
            default:
                return "Role not found";
        }
    }
}
