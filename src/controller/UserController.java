package controller;

import model.EntityType;
import model.RoleName;
import model.User;

import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserController extends CRUDController<User>{
    List<String> userData;
    AuthController authController;

    public UserController(){
        super("data/user_details.txt", EntityType.USER);
        this.authController = new AuthController();
    }

    public List<String> getUserData() {
        new FileController("data/user_details.txt");
        userData = FileController.getFile();
        return userData;
    }

    public DefaultTableModel addUserData(String[] columnNames){
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column can be editable
            }
        };

        for(String line: getUserData()){
            String[] user = line.split(",");

            model.addRow(getUserDetails(user));
        }

        return model;
    }

    public Object[] getUserDetails(String[] user) {
        Object[] userDetails = new Object[7];
        userDetails[0] = user[0];  // ID
        userDetails[1] = user[1];  // Username
        userDetails[2] = user[3] + " " + user[4];  // Full Name (firstName + lastName)
        userDetails[3] = user[5];  // Email
        userDetails[4] = RoleName.getRoleName(user[0].substring(0, 2));  // Role

        String status = user[6].trim();
        userDetails[5] = status.substring(0,1).toUpperCase() + status.substring(1);

        userDetails[6] = "";
        return userDetails;
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

    public void createUser(String role, String username, String password, String firstName, String lastName, String email, Boolean isActive){
        try{
            String status = isActive ? "active" : "inactive";
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            Map<String, String> newId = FileController.getLastUserIdByRole();

            String userId = null;
            if(newId.containsKey(role)){
                String lastId = newId.get(role);
                int numericPart = Integer.parseInt(lastId.substring(2));
                userId = String.format("%s%03d", role, numericPart + 1);
            }

            User user = authController.userInstance(role, userId, username, password, firstName, lastName, email, status, createdAt, createdAt);

            add(user);
        }catch(IOException e){
            System.out.println("Error reading user data: " + e.getMessage());
        }
    }

    @Override
    public void update(User user){
        try{
            String data = user.toCSV();
            System.out.println(data);
            fileController.updateFile(data);
            System.out.println("User added successfully");
        } catch (Exception e) {
            System.out.println("Error adding user to file: " + e.getMessage());
        }
    }

    public void updateUser(String userID, String username, String password, String firstName, String lastName, String email, String status) throws IOException {
        String[] userData = fileController.getLine(0, userID);
        if (userData == null) {
            System.out.println("User not found");
            return;
        }

        update(authController.userInstance(userData[0].substring(0,2), userData[0], username, password, firstName, lastName, email, status, userData[7], new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
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
}