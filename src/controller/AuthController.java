package controller;

import model.*;
import view.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AuthController {
    FileController user_details = new FileController("data/user_details.txt");
    private SessionController sessionManager = SessionController.getInstance();

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
                new AdminView(user);
                break;
            case "SM":
                SalesManagerView SalesManager = new SalesManagerView();
            case "PM":
                break;
            case "IM":
                InventoryManagerView InventoryManager = new InventoryManagerView(user);
                break;
            case "FM":
                break;
            default:
                break;
        }
    }

    public User createUser(String role, String username, String password, String firstName, String lastName){
        try{
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String updatedAt = createdAt;

            Map<String, String> newId = FileController.getLastUserIdByRole();

            String userId = null;
            if(newId.containsKey(role)){
                String lastId = newId.get(role);
                int numericPart = Integer.parseInt(lastId.substring(2));
                userId = String.format("%s%03d", role, numericPart + 1);
            }

            User user = null;
//            switch(role){
//                case "AM":
//                    user = new Admin(userId, username, password, firstName, lastName, createdAt, updatedAt);
//                    break;
//                case "SM":
//                    user = new SalesManager(userId, username, password, firstName, lastName, createdAt, updatedAt);
//                    break;
//                case "PM":
//                    user = new PurchaseManager(userId, username, password, firstName, lastName, createdAt, updatedAt);
//                    break;
//                case "IM":
//                    user = new InventoryManager(userId, username, password, firstName, lastName, createdAt, updatedAt);
//                    break;
//                case "FM":
//                    user = new FinanceManager(userId, username, password, firstName, lastName, createdAt, updatedAt);
//                    break;
//                default:
//                    throw new IllegalArgumentException("Invalid role: " + role);
//            }

            String userData = String.format("%s,%s,%s,%s,%s,%s,%s",
                    userId, username, password, firstName, lastName, createdAt, updatedAt);

            FileController.appendFile(userData);

            return user;
        }catch(IOException e){
            System.out.println("Error reading user data: " + e.getMessage());
            return null;
        }
    }

    public void displayLoginMenu(){
        LoginView loginView = new LoginView(this);
    }

    public void logout() {
        sessionManager.logout();
        displayLoginMenu();
    }

}