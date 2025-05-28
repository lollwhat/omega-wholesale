package view;

import controller.*;
import model.RoleName;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UserManagementView {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;
    private final UserController userController = new UserController();
    FileController fileController = new FileController("data/user_details.txt");
    private JTable userTable;

    public JPanel createUserManagementPanel(){
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

        String[] columnNames = {"ID", "Username", "Full Name", "Email", "Role", "Status", "Actions"};

        JScrollPane scrollPane = createTable(columnNames);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        addUserButton.addActionListener(
                _ -> {
                    JDialog dialog = userCRUDForm("Add New User", false, null);
                    dialog.setVisible(true);
                }
        );

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JScrollPane createTable(String[] columnNames){
        userTable = new JTable(userController.addUserData(columnNames));
        userTable.setBackground(darkBlue);
        userTable.setForeground(textWhite);
        userTable.setGridColor(new Color(50, 60, 80));
        userTable.setRowHeight(75);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        userTable.getTableHeader().setBackground(new Color(150, 165, 235));
        userTable.getTableHeader().setForeground(textWhite);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        userTable.setSelectionBackground(new Color(60, 70, 90));

        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(175);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(50);

        ActionButtonPanel actionPanel = new ActionButtonPanel(userTable);
        int actionsColumn = columnNames.length - 1;
        userTable.getColumnModel().getColumn(actionsColumn).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(actionsColumn).setCellRenderer(actionPanel);
        userTable.getColumnModel().getColumn(actionsColumn).setCellEditor(actionPanel);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().setBackground(darkBlue);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        return scrollPane;
    }

    // Combined renderer and editor for action buttons
    class ActionButtonPanel extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor {

        private final JPanel panel;
        private int row;
        AuthController authController = new AuthController();

        public ActionButtonPanel(JTable table) {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5));
            panel.setBackground(darkBlue);

            JButton editButton = createActionButton("Edit", _ -> {
                String userId = (String) table.getValueAt(row, 0);

                table.getCellEditor().stopCellEditing();

                String[] userData;
                try {
                    userData = fileController.getLine(0, userId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                JDialog dialog = userCRUDForm("Edit User", true, userData);
                dialog.setVisible(true);
                updateTable();
            });

            // Implement delete functionality by calling userController.deleteUser()
            JButton deleteButton = createActionButton("Delete", _ -> {
                String userId = (String) table.getValueAt(row, 0);
                table.getCellEditor().stopCellEditing();
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete this user?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Implement delete functionality by calling userController.deleteUser()
                    authController.deleteUser(userId);
                    updateTable();
                }
            });

            // Implement status change by calling userController.switchUserStatus()
            JButton statusButton = createActionButton("Status", _ -> {
                String userId = (String) table.getValueAt(row, 0);
                // Implement status change by calling userController.switchUserStatus()
                try {
                    table.getCellEditor().stopCellEditing();

                    authController.switchUserStatus(userId);
                    updateTable();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
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

    private JDialog userCRUDForm(String title, Boolean isEditing, String[] userData) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        // Configure the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(darkBlue);

        // Form panel with proper GridBagLayout implementation
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(darkBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Username field
        JLabel usernameLabel = createLabel("Username:");
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = createTextField();
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(usernameField, gbc);

        // Full Name field
        JLabel fullNameLabel = createLabel("Full Name:");
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(fullNameLabel, gbc);

        JTextField fullNameField = createTextField();
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(fullNameField, gbc);

        // Email field
        JLabel emailLabel = createLabel("Email:");
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(emailLabel, gbc);

        JTextField emailField = createTextField();
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(emailField, gbc);

        // Role dropdown
        JLabel roleLabel = createLabel("Role:");
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(roleLabel, gbc);

        String[] roles = {"Select Role", "Administrator", "Sales Manager", "Purchase Manager", "Inventory Manager", "Finance Manager"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        roleComboBox.setBackground(mediumBlue);
        roleComboBox.setForeground(textWhite);
        roleComboBox.setBorder(BorderFactory.createEmptyBorder());
        roleComboBox.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(roleComboBox, gbc);

        // Password field
        JLabel passwordLabel = createLabel("Password:");
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBackground(mediumBlue);
        passwordField.setForeground(textWhite);
        passwordField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        passwordField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setCaretColor(textWhite);
        if(isEditing) {
            // Add password hint label below the password field
            JLabel passwordHintLabel = new JLabel("Leave blank to keep current password");
            passwordHintLabel.setForeground(new Color(180, 180, 180)); // Light gray color
            passwordHintLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            gbc.insets = new Insets(0, 0, 5, 0);
            formPanel.add(passwordField, gbc);

            gbc.insets = new Insets(0, 0, 15, 0);
            formPanel.add(passwordHintLabel, gbc);
        } else {
            gbc.insets = new Insets(0, 0, 15, 0);
            formPanel.add(passwordField, gbc);
        }

        JCheckBox activeCheckbox = new JCheckBox("Active");
        activeCheckbox.setForeground(textWhite);
        activeCheckbox.setBackground(darkBlue);
        activeCheckbox.setSelected(true);
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(activeCheckbox, gbc);

        // Action button
        JButton actionButton = new JButton(isEditing ? "Update User" : "Add User");
        actionButton.setBackground(highlightBlue);
        actionButton.setForeground(textWhite);
        actionButton.setFocusPainted(false);
        actionButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        System.out.println("User Data: " + (userData != null ? String.join(", ", userData) : "No data"));
        if(isEditing && userData != null){
            usernameField.setText(userData[1]);
            fullNameField.setText(userData[3].trim() + " " + userData[4].trim());
            emailField.setText(userData[5]);
            // Get role code from user ID
            String roleCode = userData[0].substring(0, 2);

            // Map role code to combo box selection
            switch (roleCode) {
                case "AM": roleComboBox.setSelectedItem("Administrator"); break;
                case "SM": roleComboBox.setSelectedItem("Sales Manager"); break;
                case "PM": roleComboBox.setSelectedItem("Purchase Manager"); break;
                case "IM": roleComboBox.setSelectedItem("Inventory Manager"); break;
                case "FM": roleComboBox.setSelectedItem("Finance Manager"); break;
                default: roleComboBox.setSelectedItem("Select Role");
            }
            activeCheckbox.setSelected(userData[6].trim().equalsIgnoreCase("active"));
        }

        actionButton.addActionListener(_ ->{
            String username = usernameField.getText();
            String[] fullName = fullNameField.getText().trim().split("\\s+");
            String firstName = fullName[0];
            String lastName = "";
            if(fullName.length > 1){
                lastName = String.join(" ", Arrays.copyOfRange(fullName, 1, fullName.length));
            }

            String email = emailField.getText();
            String role = (String) roleComboBox.getSelectedItem();
            String password = new String(passwordField.getPassword());
            if(password.isEmpty()) {
                password = userData[2];
            }
            boolean isActive = activeCheckbox.isSelected();
            String status = isActive ? "active" : "inactive";

            if (username.isEmpty() || fullName == null || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String roleCode;
            switch (role) {
                case "Administrator": roleCode = "AM"; break;
                case "Sales Manager": roleCode = "SM"; break;
                case "Purchase Manager": roleCode = "PM"; break;
                case "Inventory Manager": roleCode = "IM"; break;
                case "Finance Manager": roleCode = "FM"; break;
                default:
                    JOptionPane.showMessageDialog(dialog, "Please select a valid role", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            AuthController authController = new AuthController();
            if (isEditing) {
                try {
                    authController.updateUser(userData[0], username, password, firstName, lastName, email, status);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(dialog, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                authController.createUser(roleCode, username, password, firstName, lastName, email, isActive);
                JOptionPane.showMessageDialog(dialog, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dialog.dispose();
            updateTable();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(darkBlue);
        buttonPanel.add(actionButton);

        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        // Add components to the main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Add the main panel to the dialog
        dialog.add(mainPanel);

        return dialog;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(textWhite);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(mediumBlue);
        textField.setForeground(textWhite);
        textField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textField.setCaretColor(textWhite);
        textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return textField;
    }

    private void updateTable() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        removeAllRows(model);
        List<String> userData = fileController.getFile();
        for (String line : userData) {
            String[] user = line.split(",");
            Object[] rowData = new Object[7]; // Create a properly formatted row array

            rowData[0] = user[0];  // ID
            rowData[1] = user[1];  // Username
            rowData[2] = user[3] + " " + user[4];  // Full Name (firstName + lastName)
            rowData[3] = user[5];  // Email
            rowData[4] = RoleName.getRoleName(user[0].substring(0, 2));  // Role

            // Format status to proper case
            String status = user[6].trim();
            rowData[5] = status.substring(0,1).toUpperCase() + status.substring(1);

            rowData[6] = "";  // Empty string for actions column

            model.addRow(rowData);
        }
    }

    private void removeAllRows(DefaultTableModel model) {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }
}
