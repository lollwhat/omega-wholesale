package view;

import controller.PurchaseOrderController;
import controller.SupplierController; // Still needed for item-level supplier details in popup
import controller.FileController; // Import if used directly, e.g. for path setting workarounds
import model.PurchaseOrder;
import model.PurchaseOrderItem;
import util.table.GenericModelHelper;
import util.table.mappers.PurchaseOrderRowMapper; // Assumes this is already updated
import view.forms.AddPurchaseOrderForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PMPurchaseOrderView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;

    private JTable poTable;
    private DefaultTableModel poTableModel;
    private PurchaseOrderController poController;
    private PurchaseOrderRowMapper poRowMapper;
    private SupplierController supplierController;

    private final String[] poColumnNames = {
            "PO ID", "PR ID", "Notes", "Status",
            "Created At", "Created By", "Updated At", "Updated By",
            "Received At", "Received By", "Actions"
    };
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    public JPanel createPurchaseOrderPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Purchase Order Management"); // Corrected label
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(mediumBlue);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createPOTable(), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        refreshTable();
        return mainPanel;
    }

    // showCreatePOForm remains, it's for initiating PO creation from PRs if that's a feature for PMs
    private void showCreatePOForm(String prId) {
        // This would typically open AddPurchaseOrderForm with PR data
        // For now, placeholder:
        JOptionPane.showMessageDialog(this, "Functionality to create PO from PR " + prId + " would open AddPurchaseOrderForm.");
        // Example:
        // PurchaseRequisitionController prController = new PurchaseRequisitionController();
        // PurchaseRequisition prData = prController.getFullPurchaseRequisitionById(prId);
        // if (prData != null) {
        //     AddPurchaseOrderForm poForm = new AddPurchaseOrderForm(this, prData);
        //     poForm.setVisible(true);
        // } else {
        //     JOptionPane.showMessageDialog(this, "Could not load PR: " + prId, "Error", JOptionPane.ERROR_MESSAGE);
        // }
    }

    // This method is called by the "Edit" button in POActionButtonPanel
    public void showEditPurchaseOrderForm(String poIdToEdit){ // Made public to be callable from inner class more easily
        AddPurchaseOrderForm editPOForm = new AddPurchaseOrderForm(this, poIdToEdit);
        editPOForm.setVisible(true);
    }


    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            // Ensure components are initialized
            if (poController == null) poController = new PurchaseOrderController();
            // supplierController is initialized in constructor
            if (poRowMapper == null) poRowMapper = new PurchaseOrderRowMapper(); // Constructor updated

            if (poTableModel == null) { // Initialize if null
                if (poTable != null && poTable.getModel() instanceof DefaultTableModel) {
                    poTableModel = (DefaultTableModel) poTable.getModel();
                } else {
                    // If table is also null, we might be calling refresh too early or table wasn't created.
                    // For now, let's assume createPOTable will setup poTableModel if it's null.
                    // If createPOTable hasn't run, poTableModel will be created there.
                    // If it has, poTableModel should exist. This case is a fallback.
                    System.err.println("PMPurchaseOrderView.refreshTable(): poTableModel is null, table might not be fully initialized yet.");
                    return;
                }
            }


            poTableModel.setRowCount(0);
            // No need to re-initialize poController here unless specifically intended
            // this.poController = new PurchaseOrderController();
            List<PurchaseOrder> poHeaders = poController.getAllPurchaseOrderHeaders();

            if (poHeaders != null && !poHeaders.isEmpty()) {
                for (PurchaseOrder poHeaderObj : poHeaders) {
                    if (poHeaderObj == null) continue;
                    // poHeaderObj.toCSV() now returns CSV without header supplierId
                    String headerCsvLine = poHeaderObj.toCSV();
                    String[] fields = headerCsvLine.split(",");
                    // poRowMapper.mapFieldsToRow now expects CSV without header supplierId
                    // and maps to poColumnNames (which also doesn't have header "Supplier")
                    Object[] rowData = poRowMapper.mapFieldsToRow(fields, poTableModel.getColumnCount());
                    if (rowData != null) {
                        poTableModel.addRow(rowData);
                    }
                }
            } else {
                System.out.println("No Purchase Orders to display.");
            }
        });
    }

    private JScrollPane createPOTable() {
        // supplierController is initialized in constructor
        // poRowMapper constructor no longer takes supplierController
        if (this.poRowMapper == null) this.poRowMapper = new PurchaseOrderRowMapper();

        List<String> initialEmptyData = new ArrayList<>();
        // poColumnNames is now without the header "Supplier"
        this.poTableModel = GenericModelHelper.createGenericTableModel(
                initialEmptyData,
                this.poColumnNames,
                this.poRowMapper,
                GenericModelHelper.LAST_COLUMN_EDITABLE // For "Actions" column
        );

        this.poTable = new JTable(this.poTableModel);
        // Standard table styling
        poTable.setBackground(darkBlue);
        poTable.setForeground(textWhite);
        poTable.setGridColor(new Color(50, 60, 80));
        poTable.setRowHeight(45);
        poTable.setFont(new Font("Arial", Font.PLAIN, 13));
        poTable.getTableHeader().setBackground(new Color(150, 165, 235));
        poTable.getTableHeader().setForeground(textWhite);
        poTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        poTable.setSelectionBackground(new Color(60, 70, 90));
        poTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        poTable.setAutoCreateRowSorter(true);


        // Adjust column widths according to the new poColumnNames
        int colIndex = 0;
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(80);  // PO ID
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(80);  // PR ID
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(200); // Notes
        // Supplier column (was headerFields[3] / poColumnNames[3]) is REMOVED from main table view
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Status (new index 3)
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(150); // Created At (new index 4)
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Created By (new index 5)
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(150); // Updated At (new index 6)
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Updated By (new index 7)
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(150); // Received At (new index 8)
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Received By (new index 9)
        // Actions column (new index 10, as "Supplier" was removed)
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(180); // Actions


        POActionButtonPanel actionButtonPanel = new POActionButtonPanel(this.poTable, this);
        // Get actions column index by name for robustness, or use poColumnNames.length - 1
        int actionsColumnModelIndex = poTable.getColumn("Actions").getModelIndex();
        poTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellRenderer(actionButtonPanel);
        poTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellEditor(actionButtonPanel);

        poTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int viewRow = poTable.rowAtPoint(e.getPoint());
                    int viewColumn = poTable.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewColumn >= 0 && viewColumn != actionsColumnModelIndex) {
                        int modelRow = poTable.convertRowIndexToModel(viewRow);
                        String poId = (String) poTable.getModel().getValueAt(modelRow, 0);
                        if (poId != null && !poId.trim().isEmpty()) {
                            showPurchaseOrderDetailsPopup(poId);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.poTable);
        scrollPane.getViewport().setBackground(darkBlue);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    // createActionButton helper method (can be kept as is)
    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(textWhite);
        button.setBackground(lightBlue);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (listener != null) {
            button.addActionListener(listener);
        }
        return button;
    }

    // POActionButtonPanel inner class (already revised to work with currentPoId)
    // Ensure it uses the correct parentView type (PMPurchaseOrderView)
    class POActionButtonPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final JButton editButton;
        private final JButton deleteButton;
        private JTable containingTable;
        private PMPurchaseOrderView parentView;
        private String currentPoId;

        public POActionButtonPanel(JTable table, PMPurchaseOrderView parentView) {
            this.containingTable = table;
            this.parentView = parentView;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            panel.setOpaque(true);

            editButton = createLocalActionButton("Edit");
            editButton.addActionListener(e -> {
                if (currentPoId != null && !currentPoId.isEmpty()) {
                    parentView.showEditPurchaseOrderForm(currentPoId);
                }
                fireEditingStopped();
            });

            deleteButton = createLocalActionButton("Delete");
            deleteButton.addActionListener(e -> {
                if (currentPoId != null && !currentPoId.isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(
                            parentView,
                            "Are you sure you want to delete Purchase Order ID: " + currentPoId + "?",
                            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            parentView.poController.delete(currentPoId); // Use parent's controller instance
                            parentView.refreshTable();
                            JOptionPane.showMessageDialog(parentView, "PO " + currentPoId + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(parentView, "Error deleting PO " + currentPoId + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                }
                fireEditingStopped();
            });
            panel.add(editButton);
            panel.add(deleteButton);
        }

        private JButton createLocalActionButton(String text) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setForeground(parentView.textWhite);
            button.setBackground(parentView.lightBlue);
            button.setMargin(new Insets(2, 8, 2, 8));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            return button;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            panel.setBackground(isSel ? tbl.getSelectionBackground() : (r % 2 == 0 ? darkBlue : new Color(40,50,70) ) ); // Example alternating color
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object val, boolean isSel, int r, int c) {
            int modelRow = tbl.convertRowIndexToModel(r);
            if (modelRow >=0 && modelRow < tbl.getModel().getRowCount()) {
                this.currentPoId = (String) tbl.getModel().getValueAt(modelRow, 0);
            } else {
                this.currentPoId = null;
            }
            panel.setBackground(tbl.getSelectionBackground());
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }


    private void showPurchaseOrderDetailsPopup(String poId) {
        JFrame parentDialogFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        // No need to re-initialize poController if it's an instance variable and already initialized
        // this.poController = new PurchaseOrderController();
        PurchaseOrder po = poController.getFullPurchaseOrderById(poId);

        if (po == null) {
            JOptionPane.showMessageDialog(parentDialogFrame, "Could not retrieve details for PO ID: " + poId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog detailDialog = new JDialog(parentDialogFrame, "Purchase Order Details: " + poId, true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.getContentPane().setBackground(mediumBlue);

        JPanel mainDetailPanel = new JPanel();
        mainDetailPanel.setLayout(new BoxLayout(mainDetailPanel, BoxLayout.Y_AXIS));
        mainDetailPanel.setBackground(mediumBlue);
        mainDetailPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel headerDetailsPanel = new JPanel(new GridLayout(0, 2, 8, 8)); // Auto rows based on content
        headerDetailsPanel.setBackground(mediumBlue);
        headerDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Header Information",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        addDetailRow(headerDetailsPanel, "PO ID:", po.getPoId());
        addDetailRow(headerDetailsPanel, "PR ID:", po.getPrId());
        addDetailRow(headerDetailsPanel, "Notes:", po.getNotes());
        // Supplier is no longer a header detail for the PO itself
        // addDetailRow(headerDetailsPanel, "Supplier:", supplierDisplay); // REMOVE THIS
        if (this.poRowMapper == null) this.poRowMapper = new PurchaseOrderRowMapper(); // Ensure mapper is init
        addDetailRow(headerDetailsPanel, "Status:", poRowMapper.getStatusString(po.getStatus()));
        addDetailRow(headerDetailsPanel, "Created At:", po.getCreatedAt());
        addDetailRow(headerDetailsPanel, "Created By:", po.getCreatedBy());
        addDetailRow(headerDetailsPanel, "Updated At:", po.getUpdatedAt());
        addDetailRow(headerDetailsPanel, "Updated By:", po.getUpdatedBy());
        addDetailRow(headerDetailsPanel, "Received At:", po.getReceivedAt() != null ? po.getReceivedAt() : "N/A");
        addDetailRow(headerDetailsPanel, "Received By:", po.getReceivedBy() != null ? po.getReceivedBy() : "N/A");

        mainDetailPanel.add(headerDetailsPanel);
        mainDetailPanel.add(Box.createRigidArea(new Dimension(0,15)));

        // --- Items Panel ---
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(mediumBlue);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Ordered Items",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        // New column for item-specific supplier
        String[] itemTableColumns = {"Item ID", "Item Code", "Item Name", "Supplier", "Qty", "Unit Price (ea.)", "Total Price"};
        DefaultTableModel itemsDetailTableModel = new DefaultTableModel(itemTableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Make popup table non-editable
        };

        if (po.getItems() != null) {
            for (PurchaseOrderItem item : po.getItems()) {
                String itemSupplierDisplay = "N/A";
                if (item.getSelectedSupplierId() != null && !item.getSelectedSupplierId().isEmpty()) {
                    if (supplierController == null) supplierController = new SupplierController();
                    String supData = supplierController.getOneWithId(item.getSelectedSupplierId());
                    if (supData != null) {
                        String[] supParts = supData.split(",");
                        itemSupplierDisplay = supParts[0] + (supParts.length > 1 ? (" - " + supParts[1]) : "");
                    } else {
                        itemSupplierDisplay = item.getSelectedSupplierId() + " (Details N/A)";
                    }
                }

                double displayPrice = item.getPrice() / 100.0; // Assuming price is in cents
                double displayTotalPrice = item.getTotalPrice() / 100.0; // Assuming total is in cents

                itemsDetailTableModel.addRow(new Object[]{
                        item.getItemId(), item.getItemCode(), item.getItemName(),
                        itemSupplierDisplay, // Display supplier for THIS item
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(displayPrice),
                        CURRENCY_FORMAT.format(displayTotalPrice)
                });
            }
        }
        JTable itemsDetailTable = new JTable(itemsDetailTableModel);
        itemsDetailTable.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsDetailTable.setRowHeight(25);
        // Optionally set column widths for the new items table
        itemsDetailTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Supplier column width

        JScrollPane itemsScrollPane = new JScrollPane(itemsDetailTable);
        itemsScrollPane.setPreferredSize(new Dimension(650, 150)); // Adjusted width for new column
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);

        mainDetailPanel.add(itemsPanel);

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

        detailDialog.setMinimumSize(new Dimension(700, 600)); // Adjusted size
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(parentDialogFrame);
        detailDialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelName = new JLabel(label);
        labelName.setForeground(textWhite);
        labelName.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(labelName);

        JTextArea labelValueArea = new JTextArea(value != null && !value.trim().isEmpty() ? value : "N/A");
        labelValueArea.setWrapStyleWord(true);
        labelValueArea.setLineWrap(true);
        labelValueArea.setOpaque(false); // Make transparent
        labelValueArea.setEditable(false);
        labelValueArea.setForeground(verylightBlue);
        labelValueArea.setFont(new Font("Arial", Font.PLAIN, 13));
        labelValueArea.setFocusable(false);
        labelValueArea.setBorder(null); // Remove border
        panel.add(labelValueArea);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("Test PO Management View");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Create an instance of PMPurchaseOrderView and add its panel
            PMPurchaseOrderView poViewInstance = new PMPurchaseOrderView();
            testFrame.getContentPane().add(poViewInstance.createPurchaseOrderPanel()); // Use the panel
            // Or if PMPurchaseOrderView is intended to be the frame itself:
            // PMPurchaseOrderView poViewInstance = new PMPurchaseOrderView();
            // poViewInstance.setVisible(true);
            // But for main method testing, adding panel to a test frame is common.

            testFrame.pack();
            testFrame.setLocationRelativeTo(null);
            testFrame.setVisible(true);
        });
    }
}