package view;

import controller.SupplierController;
import util.table.GenericModelHelper;
import util.table.mappers.SupplierRowMapper;
import view.forms.AddSupplierForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SupplierManagementView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;

    // Instance variables for table components and controllers
    private JTable table;
    private DefaultTableModel supplierTableModel;
    private SupplierController supplierController;
    private SupplierRowMapper supplierRowMapper;
    private final String[] supplierColumnNames = { // Made this an instance variable
        "Supplier ID", "Company", "Person-In-Charged (PIC)", "Description", "Address",
        "Contact Number", "Email", "Status", "Created At", "Created By", "Updated At", "Updated By", "Actions"
    };

    // Constructor (if SupplierManagementView is the main frame)
    public SupplierManagementView() {
        setTitle("Supplier Management"); // Example title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Example close operation
        // Initialize controllers and mappers here
        supplierController = new SupplierController();
        supplierRowMapper = new SupplierRowMapper();

        // Add the main panel created by createSupplierManagementPanel
        add(createSupplierManagementPanel());

        pack(); // Adjust frame size to components
        setLocationRelativeTo(null); // Center on screen
    }


    public JPanel createSupplierManagementPanel(){
        // Initialize controllers and mappers if not done in constructor
        if (supplierController == null) {
            supplierController = new SupplierController();
        }
        if (supplierRowMapper == null) {
            supplierRowMapper = new SupplierRowMapper();
        }

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with title and Add New Supplier button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Supplier Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);

        JButton addSupplierButton = new JButton("+ New Supplier");
        addSupplierButton.setFont(new Font("Arial", Font.BOLD, 14));
        addSupplierButton.setForeground(textWhite);
        addSupplierButton.setBackground(highlightBlue);
        addSupplierButton.setFocusPainted(false);
        addSupplierButton.setBorderPainted(false);
        addSupplierButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addSupplierButton.addActionListener(_ -> showAddSupplierForm());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(mediumBlue);
        titlePanel.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.add(addSupplierButton);

        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(buttonPanel, BorderLayout.CENTER);

        // Table Panel - createTable will now use instance variables
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createTable(), BorderLayout.CENTER); // Removed supplierColumnNames argument

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Initial data load
        refreshTable();

        return mainPanel;
    }

    private void showAddSupplierForm() {
        AddSupplierForm addSupplierForm = new AddSupplierForm(this); // 'this' is the SupplierManagementView instance
        addSupplierForm.setVisible(true);
    }

    private void showEditSupplierForm(String supplierId) {
        AddSupplierForm addSupplierForm = new AddSupplierForm(this, supplierId); // 'this' is the SupplierManagementView instance
        addSupplierForm.setVisible(true);
    }

    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            if (supplierTableModel == null || supplierController == null || supplierRowMapper == null) {
                System.err.println("SupplierManagementView.refreshTable(): Key components not initialized.");
                // Attempt to initialize if they are null and this is the first sensible time
                if (supplierController == null) supplierController = new SupplierController();
                if (supplierRowMapper == null) supplierRowMapper = new SupplierRowMapper();
                if (supplierTableModel == null && table != null) {
                    if (table.getModel() instanceof DefaultTableModel) {
                        supplierTableModel = (DefaultTableModel) table.getModel();
                    } else {
                        System.err.println("Table model is not a DefaultTableModel. Cannot refresh.");
                        return;
                    }
                } else if (supplierTableModel == null) { // If table and model are null, this is problematic
                    System.err.println("SupplierTableModel is null and cannot be derived. Refresh aborted.");
                    return;
                }
            }

            // 1. Clear existing table data from the model
            supplierTableModel.setRowCount(0);

            // 2. Get fresh data from the controller
            List<String> rawSupplierData = supplierController.getAll();

            // 3. Populate the table model
            if (rawSupplierData != null && !rawSupplierData.isEmpty()) {
                for (String line : rawSupplierData) {
                    if (line == null || line.trim().isEmpty()) continue;

                    String[] fields = line.split(",");
                    Object[] rowData = supplierRowMapper.mapFieldsToRow(fields, supplierTableModel.getColumnCount());

                    if (rowData != null) {
                        supplierTableModel.addRow(rowData);
                    }
                }
            } else {
                System.out.println("No suppliers to display or error fetching suppliers.");
            }
            // DefaultTableModel methods like setRowCount() and addRow() automatically fire table change events.
        });
    }

    private JScrollPane createTable(){ // Removed supplierColumnNames argument
        // Use instance variables for controller, mapper, and column names
        List<String> rawSupplierData = supplierController.getAll(); // Initial fetch

        // Initialize the instance variable supplierTableModel
        this.supplierTableModel = GenericModelHelper.createGenericTableModel(
                rawSupplierData,
                this.supplierColumnNames, // Use instance variable
                this.supplierRowMapper,   // Use instance variable
                GenericModelHelper.LAST_COLUMN_EDITABLE
        );

        // Initialize the instance variable table
        this.table = new JTable(this.supplierTableModel);
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
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Supplier Id
        table.getColumnModel().getColumn(1).setPreferredWidth(180); // Company
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // PIC
        table.getColumnModel().getColumn(3).setPreferredWidth(160);  // Description
        table.getColumnModel().getColumn(4).setPreferredWidth(160); // Address
        table.getColumnModel().getColumn(5).setPreferredWidth(140); // Phone
        table.getColumnModel().getColumn(6).setPreferredWidth(160); // Email
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(160); // Created By
        table.getColumnModel().getColumn(9).setPreferredWidth(100); // Updated At
        table.getColumnModel().getColumn(10).setPreferredWidth(160); // Updated By
        table.getColumnModel().getColumn(11).setPreferredWidth(100); // Updated By


        ActionButtonPanel actionPanel = new ActionButtonPanel(this.table); // Pass the instance table
        int actionsColumn = this.supplierColumnNames.length - 1;
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
                            String supplierId = (String) table.getModel().getValueAt(modelRow, 0);
                            if (supplierId != null && !supplierId.trim().isEmpty()) {
                                showSupplierDetailsPopup(supplierId, supplierColumnNames);
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
                String supplierId = (String) containingTable.getValueAt(currentRow, 0);
                showEditSupplierForm(supplierId);
            });

            deleteButton = createActionButton("Delete", e -> {
                fireEditingStopped();
                String supplierId = (String) containingTable.getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(panel.getParent(), // Dialog parent should be a top-level window
                        "Are you sure you want to delete supplier ID: " + supplierId + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // Use the SupplierManagementView's instance of SupplierController
                        SupplierManagementView.this.supplierController.delete(supplierId);

                        // Crucially, refresh the main table from the source of truth
                        SupplierManagementView.this.refreshTable();

                        JOptionPane.showMessageDialog(panel.getParent(),
                                "Supplier with ID " + supplierId + " deleted successfully.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel.getParent(),
                                "Error deleting supplier: " + ex.getMessage(),
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

    private void showSupplierDetailsPopup(String supplierId, String[] allColumnNames) {
        // Ensure supplierController is initialized
        if (this.supplierController == null) this.supplierController = new SupplierController();

        String supplierDataString = this.supplierController.getOneWithId(supplierId);

        if (supplierDataString == null || supplierDataString.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not retrieve details for supplier ID: " + supplierId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] supplierDetails = supplierDataString.split(",");

        JDialog detailDialog = new JDialog(this, "Supplier Details: " + supplierId, true);
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
            if (i < supplierDetails.length) {
                JLabel labelName = new JLabel(allColumnNames[i] + ":");
                labelName.setForeground(textWhite);
                labelName.setFont(new Font("Arial", Font.BOLD, 13));
                detailsContentPanel.add(labelName);

                if (allColumnNames[i].equals("Created By")) {
                    JLabel labelValue = new JLabel(supplierDetails[10].trim());
                    labelValue.setForeground(verylightBlue);
                    labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
                    detailsContentPanel.add(labelValue);
                } else if (allColumnNames[i].equals("Updated At")) {
                    JLabel labelValue = new JLabel(supplierDetails[9].trim());
                    labelValue.setForeground(verylightBlue);
                    labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
                    detailsContentPanel.add(labelValue);
                } else if (allColumnNames[i].equals("Updated By")) {
                    JLabel labelValue = new JLabel(supplierDetails[11].trim());
                    labelValue.setForeground(verylightBlue);
                    labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
                    detailsContentPanel.add(labelValue);
                } else {
                    JLabel labelValue = new JLabel(supplierDetails[i].trim());
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
}