package view;

import controller.AuthController;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private AuthController authController;

    public LoginView(AuthController authController) {
        this.authController = authController;

        // Frame setup
        setTitle("OWSB Login");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Dark background
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(20, 25, 45));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Logo
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ImageIcon originalIcon = new ImageIcon("assets/omega-wholesale-white.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);
        logoLabel.setIcon(resizedIcon);

        // Title
        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(128, 140, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Login to your account");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(160, 160, 160));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JPanel usernamePanel = createInputPanel("Username", usernameField = new JTextField());

        // Password field
        JPanel passwordPanel = createInputPanel("Password", passwordField = new JPasswordField());

        // Login button
        JButton loginButton = new JButton("Sign In");
        loginButton.setBackground(new Color(104, 112, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(150, 40));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Rounded button (optional)
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login logic
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                User user = authController.login(username, password);
                if (user == null) {
                    errorLabel.setText("Invalid username or password!");
                } else {
                    dispose();
                    authController.openDashboard(user);
                }
            }
        });

        // Add components
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(errorLabel);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createInputPanel(String labelText, JTextField inputField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(20, 25, 45));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Set a fixed width for both label and input field
        int fieldWidth = 300;
        int fieldHeight = 35;

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // changed from LEFT_ALIGNMENT
        label.setMaximumSize(new Dimension(fieldWidth, 20));
        label.setPreferredSize(new Dimension(fieldWidth, 20));

        inputField.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        inputField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        inputField.setBackground(new Color(30, 35, 55));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 60, 80)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(inputField);

        return panel;
    }
}
