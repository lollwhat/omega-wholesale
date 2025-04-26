package view;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminView extends JFrame {
    private JPanel sidebarPanel;
    private JPanel mainPanel;
    private Color darkBlue = new Color(21, 31, 46);
    private Color mediumBlue = new Color(30, 41, 59);
    private Color lightBlue = new Color(96, 103, 205);
    private Color verylightBlue = new Color(165, 180, 252);
    private Color highlightBlue = new Color(78, 91, 249);
    private Color textWhite = new Color(255, 255, 255);

    public AdminView(User user) {
        setTitle("OWSB System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(darkBlue);

        // Create sidebar and main panels
        createSidebar(user.getUsername(), user.getUserID());
        createMainPanel(user.getUsername(), user.getUserID());

        // Add components to frame
        add(sidebarPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    private void createSidebar(String username, String userID) {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(darkBlue);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebarPanel.setPreferredSize(new Dimension(250, getHeight()));

        // Header
        JLabel systemLabel = new JLabel("OWSB System");
        systemLabel.setFont(new Font("Arial", Font.BOLD, 20));
        systemLabel.setForeground(highlightBlue);
        systemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("User: " + username);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.LIGHT_GRAY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("Role: " + RoleName.getRoleName(userID.substring(0, 2)));
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        roleLabel.setForeground(Color.LIGHT_GRAY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Menu items
        String[] menuItems = {
                "User Management",
                "Item Management",
                "Supplier Management",
                "Daily Sales Entry",
                "Create Purchase Requisition",
                "View Requisitions",
                "Generate Purchase Order",
                "View Purchase Orders"
        };

        // Add components to sidebar
        sidebarPanel.add(systemLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebarPanel.add(userLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebarPanel.add(roleLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Add menu items
        for (String menuItem : menuItems) {
            JButton menuButton = createMenuButton(menuItem);
            sidebarPanel.add(menuButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Add logout button at bottom with spacing
        sidebarPanel.add(Box.createVerticalGlue());
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setForeground(textWhite);
        logoutButton.setBackground(lightBlue);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(250, 40));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Logout clicked"));
        sidebarPanel.add(logoutButton);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(textWhite);
        button.setBackground(darkBlue);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 35));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(verylightBlue);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(darkBlue);
            }
        });

        button.addActionListener(e -> {
            showDashboardContent(text);
        });

        return button;
    }

    private void createMainPanel(String username, String userID) {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dashboard header
        JPanel headerPanel = createHeaderPanel("Dashboard");

        // Default content panel
        JPanel contentPanel = createDefaultContentPanel(username, userID);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(String title){
        // Dashboard header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(textWhite);

        // Date label on the right
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        JLabel dateLabel = new JLabel(currentDate.format(formatter));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(textWhite);

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createDefaultContentPanel(String username, String userID) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(mediumBlue);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to Omega Wholesale Business System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(textWhite);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel greetingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        greetingPanel.setBackground(mediumBlue);
        JLabel helloLabel = new JLabel("Hello, " + username + "! You are logged in as ");
        helloLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        helloLabel.setForeground(textWhite);

        JLabel adminLabel = new JLabel(RoleName.getRoleName(userID.substring(0, 2)));
        adminLabel.setFont(new Font("Arial", Font.BOLD, 14));
        adminLabel.setForeground(highlightBlue);

        greetingPanel.add(helloLabel);
        greetingPanel.add(adminLabel);

        JLabel instructionLabel = new JLabel("Please select an option from the menu to get started.");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setForeground(textWhite);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel quickAccessLabel = new JLabel("Quick Access");
        quickAccessLabel.setFont(new Font("Arial", Font.BOLD, 16));
        quickAccessLabel.setForeground(highlightBlue);
        quickAccessLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Quick Access buttons
        String[] quickOptions = {
                "User Management",
                "Item Management",
                "Supplier Management"
        };

        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(greetingPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(instructionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(quickAccessLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add quick access buttons
        for (String option : quickOptions) {
            JButton quickButton = new JButton(option);
            quickButton.setFont(new Font("Arial", Font.PLAIN, 14));
            quickButton.setForeground(textWhite);
            quickButton.setBackground(darkBlue);
            quickButton.setFocusPainted(false);
            quickButton.setBorderPainted(false);
            quickButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            quickButton.setMaximumSize(new Dimension(300, 40));
            quickButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            quickButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    quickButton.setBackground(verylightBlue);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    quickButton.setBackground(darkBlue);
                }
            });

            quickButton.addActionListener(e -> showDashboardContent(option));

            panel.add(quickButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    private JPanel createUserManagementPanel(){
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(mediumBlue);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with title and Add New User button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);

        JButton addUserButton = new JButton("Add New User");
        addUserButton.setFont(new Font("Arial", Font.BOLD, 14));
        addUserButton.setForeground(textWhite);
        addUserButton.setBackground(highlightBlue);
        addUserButton.setFocusPainted(false);
        addUserButton.setBorderPainted(false);
        addUserButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addUserButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add New User clicked"));

        headerPanel.add(titleLabel);
        headerPanel.add(addUserButton, BorderLayout.EAST);

        return panel;
    }

    private void showDashboardContent(String contentType) {
        // This method would be expanded to show different content based on menu selection
        JOptionPane.showMessageDialog(this, contentType + " option selected");
    }
}