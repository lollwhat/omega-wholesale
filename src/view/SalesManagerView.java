package view;

import model.RoleName;

import javax.swing.*;

public class SalesManagerView extends JFrame {
    public SalesManagerView() {
        RoleName roleName = RoleName.SalesManager;
        setTitle(roleName.getRoleName("SM"));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
