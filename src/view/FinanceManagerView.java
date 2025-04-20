package view;

import model.RoleName;

import java.awt.*;
import javax.swing.*;

public class FinanceManagerView extends JFrame {
    private Panel panel;

    public FinanceManagerView() {
//        RoleName roleName = RoleName.FinanceManager;
        setTitle("Finance Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,500);
        setLocationRelativeTo(null);
        setVisible(true);
        setLayout(new BorderLayout());

        JPanel Navigation = new JPanel();
        Navigation.setBackground(new Color(10,10,40));
        Navigation.setPreferredSize(new Dimension(250, getHeight()));
        Navigation.setLayout(new BoxLayout(Navigation, BoxLayout.Y_AXIS));
        Navigation.setBorder(BorderFactory.createLineBorder(Color.black));
        Navigation.setVisible(true);
        add(Navigation, BorderLayout.WEST);
    }


}