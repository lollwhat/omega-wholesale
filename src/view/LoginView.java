package view;

import controller.AuthController;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JFrame{
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private AuthController authController;

    public LoginView(AuthController authController) {
        this.authController = authController;

        // Set up the frame
        setTitle("OWSB Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the main panel with some padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Add title
        JLabel titleLabel = new JLabel("OWSB Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add subtitle
        JLabel subtitleLabel = new JLabel("Purchase Order Management System");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(Color.DARK_GRAY);

        // Username field
        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        usernamePanel.add(usernameLabel, BorderLayout.NORTH);
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        // Password field
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordLabel, BorderLayout.NORTH);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(30, 144, 255)); // Dodger Blue
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(200, 40));

        // Error message
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add action to login button
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                User user = authController.login(username, password);
                if (user == null) {
                    errorLabel.setText("Invalid username or password!");
                } else {
                    dispose(); // Close login window
                    authController.openDashboard(user);
                }
            }
        });

        // Add components to panel with spacing
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(errorLabel);

        // Add panel to frame
        add(mainPanel);
        setVisible(true);
    }
}
