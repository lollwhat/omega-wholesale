package controller;

import model.RoleName;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class UserController{
    FileController fileController = new FileController("data/user_details.txt");
    List<String> userData = FileController.getFile();

    public DefaultTableModel addUserData(String[] columnNames){
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        for(String line: userData){
            String[] user = line.split(",");
            Object[] rowData = new Object[7];

            rowData[0] = user[0];  // ID
            rowData[1] = user[1];  // Username
            rowData[2] = user[3] + " " + user[4];  // Full Name (firstName + lastName)
            rowData[3] = user[5];  // Email
            rowData[4] = RoleName.getRoleName(user[0].substring(0, 2));  // Role
            String status = user[6].trim();
            if(user.length > 8) {
                rowData[5] = status.substring(0,1).toUpperCase() + status.substring(1); // Status
            }

            if(user.length <= 8){
                rowData[6] = "";
            }

            model.addRow(rowData);
        }

        return model;
    }
}