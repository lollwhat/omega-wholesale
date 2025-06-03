package controller;

import model.*;
import view.*;

import javax.swing.*;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class AuthController{

    private final SessionController sessionManager;

    public AuthController() {
        new FileController("data/user_details.txt");
        // Initialize the session manager if needed
        sessionManager = SessionController.getInstance();
    }

    public User userInstance(String roleCode, String userId, String username, String password, String firstName, String lastName, String email, String status, String createdAt, String updatedAt) {
        User user = null;

        switch (roleCode) {
            case "AM":
                user = new Admin(userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);
                break;
            case "SM":
                user = new SalesManager(userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);
                break;
            case "PM":
                user = new PurchaseManager(userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);
                break;
            case "IM":
                user = new InventoryManager(userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);
                break;
            case "FM":
                user = new FinanceManager(userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);
                break;
            default:
                break;
        }

        return user;
    }

    public User login(String username, String password){
        List<String> userData = FileController.getFile();
        for (String line : userData) {
            String[] details = line.split(",");
            if (details[1].trim().equals(username)) {
                String storedHashedPassword = details[2].trim();

                boolean matches = BCrypt.checkpw(password, storedHashedPassword);

                if(matches) {
                    String userId = details[0].trim();
                    String firstName = details[3].trim();
                    String lastName = details[4].trim();
                    String email = details[5].trim();
                    String status = details[6].trim();
                    String createdAt = details[7].trim();
                    String updatedAt = details[8].trim();
                    String roleCode = userId.substring(0, 2);

                    if (status.equals("active")) {
                        User user = userInstance(roleCode, userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);

                        // Set the current user in the session manager
                        if (user != null) {
                            sessionManager.setCurrentUser(user);
                            return user;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Your account is inactive. Please contact the administrator for more details.");
                    }
                }
            }
        }
        return null;
    }

    public void openDashboard(User user){
        String roleCode = user.getUserID().substring(0, 2);
        String[] menuItems;
        String[] quickOptions;
        switch (roleCode){
            case "AM" :
                menuItems = new String[]{"Dashboard", "User Management", "Item Management", "Supplier Management", "Inventory Management", "Stock Overview & Create Purchase Requisition", "Purchase Requisition Management", "PR Overview & Create Purchase Order", "Purchase Order Management", "Receive Purchase Order", "Daily Sales Entry"};
                quickOptions = new String[]{"User Management", "Item Management", "Supplier Management"};
                new AdminView((Admin) user, menuItems, quickOptions);
                break;
            case "SM":
                menuItems = new String[]{
                        "Dashboard",
                        "Item Management",
                        "Supplier Management",
                        "Stock Overview & Create Requisition",
                        "Purchase Requisition Management",
                        "List of Purchase Orders",
                        "Daily Sales Entry"
                };
                quickOptions = new String[]{"Item Management", "Supplier Management", "Daily Sales Entry"};
                new SalesManagerView((SalesManager) user, menuItems, quickOptions);
                break;
            case "PM":
                menuItems = new String[]{"Dashboard", "View Item", "View Supplier", "PR Overview & Create Purchase Order", "Purchase Order Management"};
                quickOptions = new String[]{"View Item", "View Supplier", "PR Overview & Create Purchase Order"};
                new PurchaseManagerView((PurchaseManager) user, menuItems, quickOptions);
                break;
            case "IM":
//                InventoryManagersView InventoryManager = new InventoryManagersView(user);
                menuItems = new String[]{"Dashboard", "View Item", "Inventory Management", "Receive Purchase Order"};
                quickOptions = new String[]{"View Item", "Inventory Management", "Receive Purchase Order"};
                new InventoryManagerView((InventoryManager) user, menuItems, quickOptions);
                break;
            case "FM":
                String[] financeMenu = {"View Requisitions", "View Purchase Orders", "Purchase Order Approval", "Purchase Order Management", "Approved Purchase Orders"};
                String[] financeOptions = {"View Requisitions", "View Purchase Orders", "Purchase Order Approval", "Purchase Order Management", "Approved Purchase Orders"};
                new FMView((FinanceManager) user, financeMenu, financeOptions);
                break;
            default:
                break;
        }
    }

    public void displayLoginMenu(){
        new LoginView(this);
    }

    public void logout() {
        sessionManager.logout();
        displayLoginMenu();
    }

}