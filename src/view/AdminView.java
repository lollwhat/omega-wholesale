package view;

import controller.*;
import model.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
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
    UserController userController = new UserController();

    public AdminView(User user) {
        setTitle("OWSB System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(darkBlue);

        // Create sidebar and main panels
        createSidebar(user.getUsername(), user.getUserID());
        createMainPanel();

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

        button.addActionListener(e -> showDashboardContent(text));

        return button;
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dashboard header
        JPanel headerPanel = createHeaderPanel("Dashboard");

        // Default content panel
        JPanel contentPanel = createDefaultContentPanel();

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

    private JPanel createDefaultContentPanel() {
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
        JLabel helloLabel = new JLabel("Hello, Admin! You are logged in as Administrator");
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

            quickButton.addActionListener(e -> showDashboardContent(option));

            panel.add(quickButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    private JPanel createUserManagementPanel(){
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(mediumBlue);
        titlePanel.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.add(addUserButton);

        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);

        String[] columnNames = {"User ID", "Username", "Full Name", "Email", "Role", "Status", "Actions"};

        tablePanel.add(createTable(columnNames), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JScrollPane createTable(String[] columnNames){
        JTable table = new JTable(userController.addUserData(columnNames));
        table.setBackground(darkBlue);
        table.setForeground(textWhite);
        table.setGridColor(new Color(50, 60, 80));
        table.setRowHeight(75);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(150, 165, 235));
        table.getTableHeader().setForeground(textWhite);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setSelectionBackground(new Color(60, 70, 90));

        ActionButtonPanel actionPanel = new ActionButtonPanel(table);
        int actionsColumn = columnNames.length - 1;
        table.getColumnModel().getColumn(actionsColumn).setPreferredWidth(180);
        table.getColumnModel().getColumn(actionsColumn).setCellRenderer(actionPanel);
        table.getColumnModel().getColumn(actionsColumn).setCellEditor(actionPanel);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(darkBlue);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        return scrollPane;
    }

    // Combined renderer and editor for action buttons
    class ActionButtonPanel extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor {

        private final JPanel panel;
        private final JButton editButton;
        private final JButton deleteButton;
        private final JButton statusButton;
        private final JTable table;
        private int row;

        public ActionButtonPanel(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5));
            panel.setBackground(darkBlue);

            // Create buttons
            // Implement edit functionality by calling userController.editUser()
            editButton = createActionButton("Edit", e -> {
                String userId = (String) table.getValueAt(row, 0);
                JOptionPane.showMessageDialog(panel, "Edit user with ID: " + userId);
                // Implement edit functionality by calling userController.editUser()
            });

            deleteButton = createActionButton("Delete", e -> {
                String userId = (String) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete this user?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Implement delete functionality by calling userController.deleteUser()
                    JOptionPane.showMessageDialog(panel, "User with ID " + userId + " deleted");
                }
            });

            statusButton = createActionButton("Status", e -> {
                String userId = (String) table.getValueAt(row, 0);
                // Implement status change by calling userController.switchUserStatus()
                JOptionPane.showMessageDialog(panel, "Status changed for user with ID: " + userId);
            });

            // Add buttons to panel
            panel.add(editButton);
            panel.add(deleteButton);
            panel.add(statusButton);
        }

        private JButton createActionButton(String text, ActionListener listener) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setForeground(textWhite);
            button.setBackground(lightBlue);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setMargin(new Insets(2, 4, 2, 4));
            button.addActionListener(listener);
            return button;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private void showDashboardContent(String contentType) {
        mainPanel.removeAll();

        JPanel contentPanel;
        if (contentType.equals("User Management")) {
            contentPanel = createUserManagementPanel();
        } else {
            contentPanel = createDefaultContentPanel();
        }

        mainPanel.add(createHeaderPanel(contentType), BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}