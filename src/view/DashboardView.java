package view;

import controller.*;
import model.*;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class DashboardView extends JFrame {
    protected JPanel sidebarPanel;
    protected JPanel mainPanel;
    protected User currentUser;

    // Common colors that can be used by subclasses
    protected final Color darkBlue = UITheme.DARK_BLUE;
    protected final Color mediumBlue = UITheme.MEDIUM_BLUE;
    protected final Color lightBlue = UITheme.LIGHT_BLUE;
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE;
    protected final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    protected final Color textWhite = UITheme.TEXT_WHITE;

    public DashboardView(User user, String[] menuItems, String[] quickOptions) {
        this.currentUser = user;

        setTitle("OWSB System");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(darkBlue);

        // Create sidebar and main panels
        createSidebar(user.getUsername(), user.getUserID(), menuItems);
        initializeDashboardPanel(quickOptions);

        // Add components to frame
        add(sidebarPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    private JButton createDashboardButtons(String text){
        JButton menuButton = new JButton(text);
        menuButton.setFont(new Font("Arial", Font.PLAIN, 14));
        menuButton.setForeground(textWhite);
        menuButton.setBackground(darkBlue);
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return menuButton;
    }

    protected void createSidebar(String username, String userID, String[] menuItems) {
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
        JButton logoutButton = createDashboardButtons("Logout");
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(250, 40));
        logoutButton.addActionListener(
                e -> {
                    AuthController authController = new AuthController();
                    authController.logout();
                    dispose();
                }
        );
        sidebarPanel.add(logoutButton);
    }

    protected JButton createMenuButton(String text) {
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

        button.addActionListener(e -> showContent(text));

        return button;
    }

    protected JPanel createHeaderPanel(String title){
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

    protected abstract JPanel createContentPanel(String contentType);

    protected JPanel createDashboardContentPanel(String[] quickOptions) {
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
        JLabel helloLabel = new JLabel("Hello, " + currentUser.getUsername() + "! You are logged in as " + RoleName.getRoleName(currentUser.getUserID().substring(0, 2)));
        helloLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        helloLabel.setForeground(textWhite);

        greetingPanel.add(helloLabel);

        JLabel instructionLabel = new JLabel("Please select an option from the menu to get started.");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setForeground(textWhite);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel quickAccessLabel = new JLabel("Quick Access");
        quickAccessLabel.setFont(new Font("Arial", Font.BOLD, 16));
        quickAccessLabel.setForeground(highlightBlue);
        quickAccessLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

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
            JButton quickButton = createDashboardButtons(option);
            quickButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            quickButton.setMaximumSize(new Dimension(300, 40));

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

            quickButton.addActionListener(e -> showContent(option));

            panel.add(quickButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    protected void initializeDashboardPanel(String[] quickOptions) {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dashboard header
        JPanel headerPanel = createHeaderPanel("Dashboard");

        // Dashboard content panel
        JPanel contentPanel = createDashboardContentPanel(quickOptions);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    protected void showContent(String contentType) {
        mainPanel.removeAll();

        JPanel contentPanel = createContentPanel(contentType);

        mainPanel.add(createHeaderPanel(contentType), BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
