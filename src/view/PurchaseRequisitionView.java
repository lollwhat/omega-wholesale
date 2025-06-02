package view;

import controller.PurchaseRequisitionController;
// SupplierController is no longer needed for the PR header display
// import controller.SupplierController;
import controller.FileController; // For static FileController path setting workaround
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;
import util.table.GenericModelHelper;
import util.table.mappers.PurchaseRequisitionRowMapper; // Ensure this is the updated mapper
import view.forms.AddPurchaseRequisitionForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors; // For joining supplier IDs in popup

public class PurchaseRequisitionView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;

    private JTable prTable;
    private DefaultTableModel prTableModel;
    private PurchaseRequisitionController prController;
    private PurchaseRequisitionRowMapper prRowMapper;

    // Updated column names: "Supplier" column removed
    private final String[] prColumnNames = {
            "PR ID", "Notes", "Status",
            "Created At", "Created By", "Updated At", "Updated By", "Actions"
    };

    public PurchaseRequisitionView() {
        setTitle("Purchase Requisition Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose, not exit

        prController = new PurchaseRequisitionController();
        prRowMapper = new PurchaseRequisitionRowMapper(); // Updated mapper doesn't need SupplierController
        // supplierController = new SupplierController(); // Not needed for this view's main table

        setLayout(new BorderLayout());
        add(createPurchaseRequisitionManagementPanel(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 600));
        pack();
        setLocationRelativeTo(null);
    }

    public JPanel createPurchaseRequisitionManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Purchase Requisitions"); // Updated title
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);


        JButton addPRButton = createActionButton("+ New Purchase Requisition", _ -> showAddPurchaseRequisitionForm(null));
        addPRButton.setFont(new Font("Arial", Font.BOLD, 14));
        addPRButton.setBackground(highlightBlue);

        // Center the title label more effectively
        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleContainer.setBackground(mediumBlue);
        titleContainer.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.add(addPRButton);

        headerPanel.add(titleContainer, BorderLayout.NORTH); // Title at top
        headerPanel.add(buttonPanel, BorderLayout.CENTER);  // Button below title

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createPRTable(), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        refreshTable();
        return mainPanel;
    }

    private JFrame getParentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    private void showAddPurchaseRequisitionForm(String prIdToEdit) {
        JFrame parentFrame = getParentFrame();
        AddPurchaseRequisitionForm addPRForm;
        if (prIdToEdit == null) {
            // When creating new, pass null or an empty list for items if that constructor is used.
            // The constructor AddPurchaseRequisitionForm(JFrame parent) is fine.
            addPRForm = new AddPurchaseRequisitionForm(parentFrame);
        } else {
            addPRForm = new AddPurchaseRequisitionForm(parentFrame, prIdToEdit);
        }
        addPRForm.setVisible(true);
        // Consider adding a WindowListener to AddPurchaseRequisitionForm
        // to call refreshTable() when the form is closed.
    }

    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            if (prTableModel == null) {
                if (prTable != null && prTable.getModel() instanceof DefaultTableModel) {
                    prTableModel = (DefaultTableModel) prTable.getModel();
                } else {
                    System.err.println("PurchaseRequisitionView.refreshTable(): prTableModel is null and table not ready.");
                    return;
                }
            }
            if (prController == null) prController = new PurchaseRequisitionController();
            if (prRowMapper == null) prRowMapper = new PurchaseRequisitionRowMapper();


            prTableModel.setRowCount(0);
            // PurchaseRequisitionController.getAllPurchaseRequisitionHeaders()
            // internally calls super.getAll() which ensures correct FileController path.
            List<PurchaseRequisition> prHeaders = prController.getAllPurchaseRequisitionHeaders();

            if (prHeaders != null && !prHeaders.isEmpty()) {
                for (PurchaseRequisition prHeaderObj : prHeaders) {
                    if (prHeaderObj == null) continue;
                    // prHeaderObj.toCSV() now returns 7 fields
                    String headerCsvLine = prHeaderObj.toCSV();
                    String[] fields = headerCsvLine.split(",", -1); // Use -1 to keep trailing empty fields if any

                    // prRowMapper.mapFieldsToRow expects 7 fields and maps to prColumnNames (8 columns with Actions)
                    Object[] rowData = prRowMapper.mapFieldsToRow(fields, prTableModel.getColumnCount());
                    if (rowData != null) {
                        prTableModel.addRow(rowData);
                    }
                }
            } else {
                System.out.println("No Purchase Requisitions to display.");
            }
        });
    }

    private JScrollPane createPRTable() {
        // prRowMapper should be initialized in constructor
        if (prRowMapper == null) prRowMapper = new PurchaseRequisitionRowMapper();

        List<String> initialEmptyData = new ArrayList<>(); // Start with an empty table
        this.prTableModel = GenericModelHelper.createGenericTableModel(
                initialEmptyData,       // Pass empty list for initial setup
                this.prColumnNames,     // Now 8 columns ("Supplier" removed)
                this.prRowMapper,       // Updated mapper
                (row, col, totalCols) -> col == totalCols - 1 // Actions column editable
        );

        this.prTable = new JTable(this.prTableModel);
        // Style table (same as before)
        prTable.setBackground(darkBlue);
        prTable.setForeground(textWhite);
        prTable.setGridColor(new Color(50, 60, 80));
        prTable.setRowHeight(45);
        prTable.setFont(new Font("Arial", Font.PLAIN, 14));
        prTable.getTableHeader().setBackground(new Color(150, 165, 235));
        prTable.getTableHeader().setForeground(textWhite);
        prTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        prTable.setSelectionBackground(new Color(60, 70, 90));
        prTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        prTable.setAutoCreateRowSorter(true); // Enable sorting

        // Adjust column widths based on the new prColumnNames
        int colIdx = 0;
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(80);  // PR ID
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(250); // Notes (wider)
        // Supplier column removed
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(100); // Status
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(160); // Created At
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(100); // Created By
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(160); // Updated At
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(100); // Updated By
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(180); // Actions

        PRActionButtonPanel actionPanel = new PRActionButtonPanel(this.prTable);
        int actionsColumnModelIndex = prTable.getColumn("Actions").getModelIndex(); // Get by name
        prTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellRenderer(actionPanel);
        prTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellEditor(actionPanel);

        prTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Single click for details
                    int viewRow = prTable.rowAtPoint(e.getPoint());
                    int viewColumn = prTable.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewColumn >= 0 && viewColumn != actionsColumnModelIndex) {
                        int modelRow = prTable.convertRowIndexToModel(viewRow);
                        String prId = (String) prTable.getModel().getValueAt(modelRow, 0);
                        if (prId != null && !prId.trim().isEmpty()) {
                            showPurchaseRequisitionDetailsPopup(prId);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.prTable);
        scrollPane.getViewport().setBackground(darkBlue);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        // ... (styling as before) ...
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(textWhite);
        button.setBackground(lightBlue);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(2, 8, 2, 8)); // Adjusted margin
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (listener != null) {
            button.addActionListener(listener);
        }
        return button;
    }

    // PRActionButtonPanel inner class (Edit, Delete buttons) remains largely the same
    // but ensure it uses getParentFrame() for dialogs.
    class PRActionButtonPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        // ... (Implementation as provided by user, ensure getParentFrame() for JOptionPanes) ...
        private final JPanel panel;
        private final JTable containingTable;
        private int currentRow;
        private JButton editButton; // Made instance fields for consistent styling
        private JButton deleteButton;

        public PRActionButtonPanel(JTable table) {
            this.containingTable = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2)); // Adjusted padding
            panel.setOpaque(true);

            editButton = createActionButton("Edit", _ -> {
                fireEditingStopped();
                String prId = (String) containingTable.getValueAt(currentRow, 0);
                showAddPurchaseRequisitionForm(prId);
            });

            deleteButton = createActionButton("Delete", _ -> {
                fireEditingStopped();
                String prId = (String) containingTable.getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(getParentFrame(),
                        "Are you sure you want to delete Purchase Requisition ID: " + prId + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // Ensure prController sets its path before delete if it uses static FileController methods
                        new FileController(PurchaseRequisitionView.this.prController.getPrHeaderFilePath()); // Path for PR header
                        // And PR_ITEMS_FILE_PATH for items if delete logic there uses static FC
                        PurchaseRequisitionView.this.prController.delete(prId);
                        PurchaseRequisitionView.this.refreshTable();
                        JOptionPane.showMessageDialog(getParentFrame(),
                                "PR ID " + prId + " deleted successfully.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(getParentFrame(),
                                "Error deleting PR: " + ex.getMessage(),
                                "Delete Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            });
            panel.add(editButton);
            panel.add(deleteButton);
        }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            this.currentRow = r; // Important for context
            panel.setBackground(isSel ? tbl.getSelectionBackground() : (r % 2 == 0 ? darkBlue : new Color(40, 50, 70)));
            return panel;
        }
        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object val, boolean isSel, int r, int c) {
            this.currentRow = r;
            panel.setBackground(tbl.getSelectionBackground());
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public boolean stopCellEditing() { return super.stopCellEditing(); }
    }


    private void showPurchaseRequisitionDetailsPopup(String prId) {
        JFrame parentFrame = getParentFrame();
        // Ensure prController path setting for its operations
        new FileController(this.prController.getPrHeaderFilePath()); // For PR header
        PurchaseRequisition pr = prController.getFullPurchaseRequisitionById(prId); // getFull... will handle item file path

        if (pr == null) {
            JOptionPane.showMessageDialog(parentFrame, "Could not retrieve details for PR ID: " + prId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog detailDialog = new JDialog(parentFrame, "Purchase Requisition Details: " + prId, true);
        // ... (dialog setup as before) ...
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.getContentPane().setBackground(mediumBlue);

        JPanel mainDetailPanel = new JPanel();
        mainDetailPanel.setLayout(new BoxLayout(mainDetailPanel, BoxLayout.Y_AXIS));
        mainDetailPanel.setBackground(mediumBlue);
        mainDetailPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel headerDetailsPanel = new JPanel(new GridLayout(0, 2, 8, 8)); // Auto rows
        // ... (headerDetailsPanel setup as before) ...
        headerDetailsPanel.setBackground(mediumBlue);
        headerDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Header Information",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));


        addDetailRow(headerDetailsPanel, "PR ID:", pr.getPrId());
        addDetailRow(headerDetailsPanel, "Notes:", pr.getNotes());
        if (prRowMapper == null) prRowMapper = new PurchaseRequisitionRowMapper(); // Ensure mapper is not null
        addDetailRow(headerDetailsPanel, "Status:", prRowMapper.getStatusString(pr.getStatus()));
        addDetailRow(headerDetailsPanel, "Created At:", pr.getCreatedAt());
        addDetailRow(headerDetailsPanel, "Created By:", pr.getCreatedBy());
        addDetailRow(headerDetailsPanel, "Updated At:", pr.getUpdatedAt());
        addDetailRow(headerDetailsPanel, "Updated By:", pr.getUpdatedBy());

        mainDetailPanel.add(headerDetailsPanel);
        mainDetailPanel.add(Box.createRigidArea(new Dimension(0,15)));

        JPanel itemsPanel = new JPanel(new BorderLayout());
        // ... (itemsPanel setup as before) ...
        itemsPanel.setBackground(mediumBlue);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Requested Items",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));


        // Updated item table columns for the popup
        String[] itemPopupTableColumns = {"Item ID", "Item Code", "Item Name", "Qty", "Unit Price", "Total Price", "Suggested Suppliers"};
        DefaultTableModel itemsDetailTableModel = new DefaultTableModel(itemPopupTableColumns, 0);

        if (pr.getItems() != null) {
            for (PurchaseRequisitionItem item : pr.getItems()) {
                if (item == null) continue;
                double displayPrice = item.getPrice() / 100.0;
                double displayTotalPrice = item.getTotalPrice() / 100.0;

                String suggestedSuppliersStr = "N/A";
                if (item.getSuggestedSupplierIds() != null && !item.getSuggestedSupplierIds().isEmpty()) {
                    suggestedSuppliersStr = item.getSuggestedSupplierIds().stream().collect(Collectors.joining(", "));
                }

                itemsDetailTableModel.addRow(new Object[]{
                        item.getItemId(),
                        item.getItemCode(),
                        item.getItemName(),
                        item.getQuantity(),
                        String.format("%.2f", displayPrice),
                        String.format("%.2f", displayTotalPrice),
                        suggestedSuppliersStr // Add suggested suppliers here
                });
            }
        }
        JTable itemsDetailTable = new JTable(itemsDetailTableModel);
        // ... (itemsDetailTable styling as before) ...
        itemsDetailTable.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsDetailTable.setRowHeight(25);
        itemsDetailTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Suggested Suppliers column

        JScrollPane itemsScrollPane = new JScrollPane(itemsDetailTable);
        itemsScrollPane.setPreferredSize(new Dimension(580, 150)); // Increased width
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);

        mainDetailPanel.add(itemsPanel);

        // ... (rest of dialog setup: detailScrollPane, closeButton, buttonPanel, dialog.add, pack, etc.) ...
        JScrollPane detailScrollPane = new JScrollPane(mainDetailPanel);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(mediumBlue);

        JButton closeButton = createActionButton("Close", _ -> detailDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        buttonPanel.add(closeButton);

        detailDialog.add(detailScrollPane, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);

        detailDialog.setMinimumSize(new Dimension(650, 550)); // Adjusted size
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(parentFrame);
        detailDialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelName = new JLabel(label);
        labelName.setForeground(textWhite);
        labelName.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(labelName);

        JTextArea labelValueArea = new JTextArea(value != null ? value : "N/A");
        labelValueArea.setWrapStyleWord(true);
        labelValueArea.setLineWrap(true);
        labelValueArea.setOpaque(false);
        labelValueArea.setEditable(false);
        labelValueArea.setForeground(verylightBlue);
        labelValueArea.setFont(new Font("Arial", Font.PLAIN, 13));
        // To make JTextArea blend in, remove focusability and border if it's strictly for display
        labelValueArea.setFocusable(false);
        labelValueArea.setBorder(null);
        panel.add(labelValueArea);
    }

    // Main method for standalone testing (optional)
    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> {
    //         PurchaseRequisitionView view = new PurchaseRequisitionView();
    //         view.setTitle("PR View Test");
    //         view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //         view.setVisible(true);
    //     });
    // }
}