package controller;

import model.RoleName;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class UserController{
    String filepath;
    List<String> userData;

    public UserController(String filepath){
        this.filepath = filepath;
    }

    public List<String> getUserData() {
        new FileController(filepath);
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
}