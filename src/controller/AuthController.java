package controller;

import model.*;
import view.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class AuthController {
    private static final String user_details = "data/user_details.txt";

    public User login(String username, String password){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(user_details));
            String line = "";
            while((line = reader.readLine()) != null){
                String[] details = line.split(",");
                if(details[1].trim().equals(username) && details[2].trim().equals(password)){
                    String userId = details[0].trim();
                    String firstName = details[3].trim();
                    String lastName = details[4].trim();
                    String createdAt = details[5].trim();
                    String updatedAt = details[6].trim();
                    String roleCode = userId.substring(0, 2);

                    User user;
                    switch (roleCode) {
                        case "AM":
                            user = new Admin(userId, username, password, firstName, lastName, createdAt, updatedAt);
                            reader.close();
                            return user;
                        case "SM":
                            user = new SalesManager(userId, username, password, firstName, lastName, createdAt, updatedAt);
                            reader.close();
                            return user;
                        case "PM":
                            break;
                        case "IM":
                            break;
                        case "FM":
                            break;
                        default:
                            break;
                    }
                }
            }
        }catch(IOException e){
            System.out.println("Error reading user data: " + e.getMessage());
        }
        return null;
    }

    public void openDashboard(User user){
        String roleCode = user.getUserID().substring(0, 2);
        switch (roleCode){
            case "AM" :
//              AdminDashboardView();
                break;
            case "SM":
                SalesManagerView SalesManager = new SalesManagerView();
            case "PM":
                break;
            case "IM":
                break;
            case "FM":
                break;
            default:
                break;
        }
    }

    public User createUser(String role, String username, String password, String firstName, String lastName){
        try{
            FileWriter fileWriter = new FileWriter(user_details, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String updatedAt = createdAt;

            GeneratorController idGenerator = new GeneratorController(user_details);
            Map<String, String> newId = idGenerator.getLastUserIdByRole();

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

            bufferedWriter.write(userData);
            bufferedWriter.newLine();
            bufferedWriter.close();

            return user;
        }catch(IOException e){
            System.out.println("Error reading user data: " + e.getMessage());
            return null;
        }
    }

    public void displayLoginMenu(){
        LoginView loginView = new LoginView(this);

    }
}