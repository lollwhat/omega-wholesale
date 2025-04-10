package controller;

import model.*;
import GeneratorController;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuthController {
    private static final String user_details = "";

    public User login(String username, String password){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(user_details));
            String line = "";
            while(line = reader.readLine() != null){
                String[] details = line.split(",");
                if(details[1].equals(username) && details[2].equals(password)){
                    switch (details[0].substring(0, 2){
                        case "AM" :
                            break;
                        case "SM":
                            break;
                        case "PM":
                            break;
                        case "IM":
                            break;
                        case "FM":
                            break;
                        case default:
                            break;
                    }
                }
            }
            reader.close();
        }catch(IOException e){
            System.out.println("Error reading user data: " + e.getMessage());
        }

        return null;
    }

    public User createUser(String role, String username, String password, String firstName, String lastName){
        try{
            FileWriter fileWriter = new FileWriter(user_details, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String updatedAt = createdAt;

            GeneratorController idGenerator = new GeneratorController(user_details);
            HashMap<String, String> newId = idGenerator.getLastUserIdByRole();

            String userId;
            if(newId.containsKey(role)){
                String lastId = newId.get(role);
                int numericPart = Integer.parseInt(lastId.substring(2));
                userId = String.format("%s%03d", role, numericPart + 1);
            }

//            User user;
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
}