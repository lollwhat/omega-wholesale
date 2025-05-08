package view;

import controller.UserController;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;

public class UserManagementView {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;
    private final UserController userController = new UserController();

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
        addUserButton.addActionListener(e -> JOptionPane.showMessageDialog(this.createUserManagementPanel(), "Add New User clicked"));

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

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(175);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);

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
}
