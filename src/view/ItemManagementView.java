package view;

import controller.*;
import util.table.GenericModelHelper;
import util.table.mappers.ItemRowMapper;
import view.forms.AddItemForm;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ItemManagementView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;

    // Instance variables for table components and controllers
    private JTable table;
    private DefaultTableModel itemTableModel;
    private ItemController itemController;
    private ItemRowMapper itemRowMapper;
    private final String[] itemColumnNames = { // Made this an instance variable
            "Item Entry ID", "Item Code", "Name", "Unit", "Price", "Supplier ID",
            "Created At", "Created By", "Updated At", "Updated By", "Actions"
    };

    // Constructor (if ItemManagementView is the main frame)
    public ItemManagementView() {
        setTitle("Item Management Application"); // Example title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Example close operation
        // Initialize controllers and mappers here
        itemController = new ItemController();
        itemRowMapper = new ItemRowMapper();

        // Add the main panel created by createItemManagementPanel
        add(createItemManagementPanel());

        pack(); // Adjust frame size to components
        setLocationRelativeTo(null); // Center on screen
        // setVisible(true); // You might set this from where ItemManagementView is created
    }


    public JPanel createItemManagementPanel(){
        // Initialize controllers and mappers if not done in constructor
        if (itemController == null) {
            itemController = new ItemController();
        }
        if (itemRowMapper == null) {
            itemRowMapper = new ItemRowMapper();
        }

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with title and Add New Item button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Item Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);

        JButton addItemButton = new JButton("+ New Item");
        addItemButton.setFont(new Font("Arial", Font.BOLD, 14));
        addItemButton.setForeground(textWhite);
        addItemButton.setBackground(highlightBlue);
        addItemButton.setFocusPainted(false);
        addItemButton.setBorderPainted(false);
        addItemButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addItemButton.addActionListener(_ -> showAddItemForm());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(mediumBlue);
        titlePanel.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.add(addItemButton);

        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(buttonPanel, BorderLayout.CENTER);

        // Table Panel - createTable will now use instance variables
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createTable(), BorderLayout.CENTER); // Removed itemColumnNames argument

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Initial data load
        refreshTable();

        return mainPanel;
    }

    private void showAddItemForm() {
        AddItemForm addItemForm = new AddItemForm(this); // 'this' is the ItemManagementView instance
        addItemForm.setVisible(true);
        // refreshTable() will be called from AddItemForm after successful save
    }

    private void showEditItemForm(String itemId) {
        AddItemForm addItemForm = new AddItemForm(this, itemId); // 'this' is the ItemManagementView instance
        addItemForm.setVisible(true);
        // refreshTable() will be called from AddItemForm after successful save
    }

    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            if (itemTableModel == null || itemController == null || itemRowMapper == null) {
                System.err.println("ItemManagementView.refreshTable(): Key components not initialized.");
                // Attempt to initialize if they are null and this is the first sensible time
                if (itemController == null) itemController = new ItemController();
                if (itemRowMapper == null) itemRowMapper = new ItemRowMapper();
                if (itemTableModel == null && table != null) { // If table exists, model should too
                    if (table.getModel() instanceof DefaultTableModel) {
                        itemTableModel = (DefaultTableModel) table.getModel();
                    } else {
                        System.err.println("Table model is not a DefaultTableModel. Cannot refresh.");
                        return;
                    }
                } else if (itemTableModel == null) { // If table and model are null, this is problematic
                    System.err.println("ItemTableModel is null and cannot be derived. Refresh aborted.");
                    return;
                }
            }

            // 1. Clear existing table data from the model
            itemTableModel.setRowCount(0);

            // 2. Get fresh data from the controller
            List<String> rawItemData = itemController.getAll();

            // 3. Populate the table model
            if (rawItemData != null && !rawItemData.isEmpty()) {
                for (String line : rawItemData) {
                    if (line == null || line.trim().isEmpty()) continue;

                    String[] fields = line.split(",");
                    Object[] rowData = itemRowMapper.mapFieldsToRow(fields, itemTableModel.getColumnCount());

                    if (rowData != null) {
                        itemTableModel.addRow(rowData);
                    }
                }
            } else {
                System.out.println("No items to display or error fetching items.");
            }
            // DefaultTableModel methods like setRowCount() and addRow() automatically fire table change events.
        });
    }

    private JScrollPane createTable(){ // Removed itemColumnNames argument
        // Use instance variables for controller, mapper, and column names
        List<String> rawItemData = itemController.getAll(); // Initial fetch

        // Initialize the instance variable itemTableModel
        this.itemTableModel = GenericModelHelper.createGenericTableModel(
                rawItemData,
                this.itemColumnNames, // Use instance variable
                this.itemRowMapper,   // Use instance variable
                GenericModelHelper.LAST_COLUMN_EDITABLE
        );

        // Initialize the instance variable table
        this.table = new JTable(this.itemTableModel);
        table.setBackground(darkBlue);
        table.setForeground(textWhite);
        table.setGridColor(new Color(50, 60, 80));
        table.setRowHeight(45);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(150, 165, 235));
        table.getTableHeader().setForeground(textWhite);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setSelectionBackground(new Color(60, 70, 90));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Important for horizontal scrolling

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Item Entry ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Item Code
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Name (wider)
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Unit
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Price
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Supplier ID
        table.getColumnModel().getColumn(6).setPreferredWidth(160); // Created At
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Created By
        table.getColumnModel().getColumn(8).setPreferredWidth(160); // Updated At
        table.getColumnModel().getColumn(9).setPreferredWidth(100); // Updated By


        ActionButtonPanel actionPanel = new ActionButtonPanel(this.table); // Pass the instance table
        int actionsColumn = this.itemColumnNames.length - 1;
        table.getColumnModel().getColumn(actionsColumn).setPreferredWidth(180); // Actions
        table.getColumnModel().getColumn(actionsColumn).setCellRenderer(actionPanel);
        table.getColumnModel().getColumn(actionsColumn).setCellEditor(actionPanel);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // For single click, check if it's not on the actions column before showing details
                if (e.getClickCount() == 1) { // Changed to single click for detail view
                    int viewRow = table.rowAtPoint(e.getPoint());
                    int viewColumn = table.columnAtPoint(e.getPoint());

                    if (viewRow >= 0 && viewColumn >= 0) {
                        if (viewColumn != actionsColumn) { // Check if not actions column
                            int modelRow = table.convertRowIndexToModel(viewRow);
                            String itemId = (String) table.getModel().getValueAt(modelRow, 0);
                            if (itemId != null && !itemId.trim().isEmpty()) {
                                showItemDetailsPopup(itemId, itemColumnNames);
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.table);
        scrollPane.getViewport().setBackground(darkBlue);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        return scrollPane;
    }

    // Inner class for Action Buttons Panel
    class ActionButtonPanel extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor {

        private final JPanel panel;
        private final JButton editButton;
        private final JButton deleteButton; // Made final
        private final JTable containingTable; // Renamed for clarity
        private int currentRow; // Renamed for clarity

        public ActionButtonPanel(JTable table) {
            this.containingTable = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5)); // Reduced spacing
            panel.setOpaque(true); // Ensure background color is painted

            editButton = createActionButton("Edit", e -> {
                // `fireEditingStopped` is important for the table to commit any pending edits
                // and for the editor to be properly dismissed before opening a dialog.
                fireEditingStopped();
                String itemId = (String) containingTable.getValueAt(currentRow, 0);
                showEditItemForm(itemId);
            });

            deleteButton = createActionButton("Delete", e -> {
                fireEditingStopped();
                String itemId = (String) containingTable.getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(panel.getParent(), // Dialog parent should be a top-level window
                        "Are you sure you want to delete item ID: " + itemId + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // Use the ItemManagementView's instance of ItemController
                        ItemManagementView.this.itemController.delete(itemId);

                        // Crucially, refresh the main table from the source of truth
                        ItemManagementView.this.refreshTable();

                        JOptionPane.showMessageDialog(panel.getParent(),
                                "Item with ID " + itemId + " deleted successfully.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel.getParent(),
                                "Error deleting item: " + ex.getMessage(),
                                "Delete Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        private JButton createActionButton(String text, ActionListener listener) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setForeground(textWhite);
            button.setBackground(lightBlue); // Consistent button color
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setMargin(new Insets(2, 5, 2, 5)); // Slightly more padding
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (listener != null) {
                button.addActionListener(listener);
            }
            return button;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            // Set background based on selection
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                // Use the row's actual background color, or the table's default if not specified
                panel.setBackground(row % 2 == 0 ? darkBlue : new Color(40, 50, 70)); // Example alternating row color
            }
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.currentRow = row; // Store the row being edited
            panel.setBackground(table.getSelectionBackground()); // Editor usually matches selection
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return ""; // Value doesn't matter for buttons
        }

        // It's good practice to override this for cell editors
        @Override
        public boolean stopCellEditing() {
            // fireEditingStopped(); // Already called in button actions
            return super.stopCellEditing();
        }
    }

    private void showItemDetailsPopup(String itemId, String[] allColumnNames) {
        // Ensure itemController is initialized
        if (this.itemController == null) this.itemController = new ItemController();

        String itemDataString = this.itemController.getOneWithId(itemId);

        if (itemDataString == null || itemDataString.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not retrieve details for item ID: " + itemId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] itemDetails = itemDataString.split(",");

        JDialog detailDialog = new JDialog(this, "Item Details: " + itemId, true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.getContentPane().setBackground(mediumBlue);

        JPanel detailsContentPanel = new JPanel();
        int displayableDetailsCount = 0;
        for (String colName : allColumnNames) {
            if (!colName.equals("Actions")) {
                displayableDetailsCount++;
            }
        }

        detailsContentPanel.setLayout(new GridLayout(displayableDetailsCount, 2, 8, 8));
        detailsContentPanel.setBackground(mediumBlue);
        detailsContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int i = 0; i < allColumnNames.length; i++) {
            if (allColumnNames[i].equals("Actions")) continue;
            if (i < itemDetails.length) {
                JLabel labelName = new JLabel(allColumnNames[i] + ":");
                labelName.setForeground(textWhite);
                labelName.setFont(new Font("Arial", Font.BOLD, 13));
                detailsContentPanel.add(labelName);

                if (allColumnNames[i].equals("Created By")) {
                    JLabel labelValue = new JLabel(itemDetails[8].trim());
                    labelValue.setForeground(verylightBlue);
                    labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
                    detailsContentPanel.add(labelValue);
                } else if (allColumnNames[i].equals("Updated At")) {
                    JLabel labelValue = new JLabel(itemDetails[7].trim());
                    labelValue.setForeground(verylightBlue);
                    labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
                    detailsContentPanel.add(labelValue);
                } else if (allColumnNames[i].equals("Updated By")) {
                    JLabel labelValue = new JLabel(itemDetails[9].trim());
                    labelValue.setForeground(verylightBlue);
                    labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
                    detailsContentPanel.add(labelValue);
                } else {
                    JLabel labelValue = new JLabel(itemDetails[i].trim());
                    labelValue.setForeground(verylightBlue);
                    labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
                    detailsContentPanel.add(labelValue);
                }
            }
        }

        JScrollPane detailScrollPane = new JScrollPane(detailsContentPanel);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(mediumBlue);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setForeground(textWhite);
        closeButton.setBackground(lightBlue);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(_ -> detailDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        buttonPanel.add(closeButton);

        detailDialog.add(detailScrollPane, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);

        detailDialog.setMinimumSize(new Dimension(450, 350)); // Slightly increased height
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setVisible(true);
    }

    // Main method for standalone testing (optional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ItemManagementView view = new ItemManagementView();
            view.setVisible(true);
        });
    }
}