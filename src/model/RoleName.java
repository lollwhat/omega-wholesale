package model;

import view.SalesManagerView;

public enum RoleName {
    Admin,
    SalesManager,
    PurchaseManager,
    InventoryManager,
    FinanceManager;


    public String getRoleName(String roleCode) {
        switch (roleCode){
            case "AM" :
                return Admin.name();
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
