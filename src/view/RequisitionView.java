package view;

import model.User;

import java.awt.*;
import javax.swing.*;

public class RequisitionView extends JPanel {
    private User currentUser;
    private String[] financeOptions = {"View Requisitions", "View Purchase Orders", "Financial Management"};

    private Color background = new Color(21, 31, 46);
    private Color panelColour = new Color(30, 41, 59);
    private Color lightBlue = new Color(96, 103, 205);
    private Color verylightBlue = new Color(165, 180, 252);
    private Color labelColour = new Color(78, 91, 249);
    private Color textColour = new Color(255, 255, 255);

    public JPanel requisitionView() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(panelColour);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        JLabel requisitionTitle = new JLabel("View Purchase Requisition");
        requisitionTitle.setForeground(labelColour);
        requisitionTitle.setFont(new Font("SanSerif", Font.PLAIN, 20));

        JPanel tablePanel = new JPanel(new BorderLayout());

        String[] columnNames = {"PR ID", "Date", "Required Date", "Item", "Quantity", "Supplier", "Status", "Created By"};


        add(mainPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(headerPanel, BorderLayout.CENTER);
        headerPanel.add(requisitionTitle);

        return mainPanel;
    }


    private JScrollPane createTable(String[] columnNames) {
        JPanel tableWrapper = new JPanel();
        tableWrapper.setLayout(new BorderLayout());
        tableWrapper.setBackground(panelColour);
        tableWrapper.setPreferredSize(new Dimension(900, 500));
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));


        JTable requisitionTable = new JTable(columnNames);
        requisitionTable.setBackground(panelColour);
        requisitionTable.setForeground(textColour);
        requisitionTable.setRowHeight(30);
        requisitionTable.setFont(new Font("SanSerif", Font.PLAIN, 14));

        return tableWrapper;
    }
}
