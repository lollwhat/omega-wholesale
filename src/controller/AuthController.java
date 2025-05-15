package controller;

import model.*;
import view.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AuthController extends CRUDController<User>{
    public AuthController() {
        super("data/user_details.txt", EntityType.USER);
    }

    private SessionController sessionManager = SessionController.getInstance();

    private User userInstance(String roleCode, String userId, String username, String password, String firstName, String lastName, String email, String status, String createdAt, String updatedAt) {
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
            case "IM":
                user = new InventoryManager(userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);
            case "FM":
                user = new FinanceManager(userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);
            default:
                break;
        }

        return user;
    }

    public User login(String username, String password){
        List<String> userData = FileController.getFile();
        for (String line : userData) {
            String[] details = line.split(",");
            if (details[1].trim().equals(username) && details[2].trim().equals(password)) {
                String userId = details[0].trim();
                String firstName = details[3].trim();
                String lastName = details[4].trim();
                String email = details[5].trim();
                String status = details[6].trim();
                String createdAt = details[7].trim();
                String updatedAt = details[8].trim();
                String roleCode = userId.substring(0, 2);

                User user = userInstance(roleCode, userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);

                // Set the current user in the session manager
                if(user != null){
                    sessionManager.setCurrentUser(user);
                    return user;
                }
            }
        }
        return null;
    }

    public void openDashboard(User user){
        String roleCode = user.getUserID().substring(0, 2);
        switch (roleCode){
            case "AM" :
                String[] menuItems = {"User Management", "Item Management", "Supplier Management", "Daily Sales Entry", "Create Purchase Requisition", "View Requisitions", "Create Purchase Order", "View Purchase Orders"};
                String[] quickOptions = {"User Management", "Item Management", "Supplier Management"};
                new AdminView((Admin) user, menuItems, quickOptions);
                break;
            case "SM":
                SalesManagerView SalesManager = new SalesManagerView();
            case "PM":
                break;
            case "IM":
                InventoryManagerView InventoryManager = new InventoryManagerView(user);
                break;
            case "FM":
                FinanceManagerView FinanceManager = new FinanceManagerView(user);
                break;
            default:
                break;
        }
    }

    @Override
    public void add(User user){
        try{
            String data = user.toCSV();
            FileController.appendFile(data);
            System.out.println("User added successfully");
        } catch (Exception e) {
            System.out.println("Error adding user to file: " + e.getMessage());
        }
    }

    public void createUser(String role, String username, String password, String firstName, String lastName, String email){
        try{
            String status = "active";
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String updatedAt = createdAt;

            Map<String, String> newId = FileController.getLastUserIdByRole();

            String userId = null;
            if(newId.containsKey(role)){
                String lastId = newId.get(role);
                int numericPart = Integer.parseInt(lastId.substring(2));
                userId = String.format("%s%03d", role, numericPart + 1);
            }

            User user = userInstance(role, userId, username, password, firstName, lastName, email, status, createdAt, updatedAt);

            add(user);
        }catch(IOException e){
            System.out.println("Error reading user data: " + e.getMessage());
        }
    }

    @Override
    public void update(User user){
        try{
            String data = user.toCSV();
            fileController.updateFile(data);
            System.out.println("User added successfully");
        } catch (Exception e) {
            System.out.println("Error adding user to file: " + e.getMessage());
        }
    }

    public void updateUser(String userID, String username, String password, String firstName, String lastName, String email) throws IOException {
        String[] userData = fileController.getLine(0, userID);
        if (userData == null) {
            System.out.println("User not found");
            return;
        }

        User user = userInstance(userData[0].substring(0,2), userData[1], username, password, firstName, lastName, email, userData[6], userData[7], new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    public void deleteUser(String userID){
        delete(userID);
    }

    public void switchUserStatus(String userID) throws IOException {
        String[] userData = fileController.getLine(0, userID);
        String status = userData[6].trim();
        String newStatus = status.equals("active") ? "inactive" : "active";
        userData[6] = newStatus;

        String updatedLine = String.join(",", userData);
        fileController.updateFile(updatedLine);
    }

    public void displayLoginMenu(){
        LoginView loginView = new LoginView(this);
    }

    public void logout() {
        sessionManager.logout();
        displayLoginMenu();
    }

}