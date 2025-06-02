package view;

import controller.PurchaseOrderController;
import controller.StockController;
import controller.SupplierController;
import model.PurchaseOrder;
import model.PurchaseOrderItem;
import util.table.GenericModelHelper;
import util.table.mappers.PurchaseOrderRowMapper;

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

public class IMPurchaseOrderView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE; // Example: new Color(23, 32, 42);
    private final Color mediumBlue = UITheme.MEDIUM_BLUE; // Example: new Color(44, 62, 80);
    private final Color lightBlue = UITheme.LIGHT_BLUE; // Example: new Color(52, 152, 219);
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE; // Example: new Color(212, 230, 241);
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE; // Example: new Color(133, 193, 233);
    private final Color textWhite = UITheme.TEXT_WHITE; // Example: Color.WHITE;

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
    // Ensure this order matches your status logic, especially for "Approved"
    private static final String[] PO_STATUS_DIALOG_OPTIONS = {"Pending", "Approved", "Received", "Cancelled"};
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");


    public IMPurchaseOrderView() {
        setTitle("Purchase Order Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        poController = new PurchaseOrderController();
        supplierController = new SupplierController();
        poRowMapper = new PurchaseOrderRowMapper();

        add(createPurchaseOrderPanel());

        setMinimumSize(new Dimension(1100, 600));
        pack();
        setLocationRelativeTo(null);
    }

    public JPanel createPurchaseOrderPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Receive Purchase Order");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(mediumBlue);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createPOTable(), BorderLayout.CENTER); // POActionButtonPanel is constructed here

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        refreshTable(); // Data is loaded after POActionButtonPanel construction
        return mainPanel;
    }

    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            if (poTableModel == null || poController == null || poRowMapper == null) {
                System.err.println("PurchaseOrderView.refreshTable(): Components not initialized.");
                if (poController == null) poController = new PurchaseOrderController();
                if (supplierController == null) supplierController = new SupplierController();
                if (poTableModel == null && poTable != null && poTable.getModel() instanceof DefaultTableModel) {
                    poTableModel = (DefaultTableModel) poTable.getModel();
                } else if (poTableModel == null) {
                    System.err.println("POTableModel is null. Refresh aborted."); return;
                }
            }

            poTableModel.setRowCount(0);
            // this.poController = new PurchaseOrderController(); // Re-instantiating might lose state if intended
            List<PurchaseOrder> poHeaders = poController.getAllPurchaseOrderHeaders();

            if (poHeaders != null && !poHeaders.isEmpty()) {
                for (PurchaseOrder poHeaderObj : poHeaders) {
                    if (poHeaderObj == null) continue;
                    String headerCsvLine = poHeaderObj.toCSV();
                    String[] fields = headerCsvLine.split(",");
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
        if (this.supplierController == null) this.supplierController = new SupplierController();

        List<String> initialEmptyData = new ArrayList<>(); // Table starts empty
        this.poTableModel = GenericModelHelper.createGenericTableModel(
                initialEmptyData,
                this.poColumnNames,
                this.poRowMapper,
                GenericModelHelper.LAST_COLUMN_EDITABLE
        );

        this.poTable = new JTable(this.poTableModel);
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

        int colIndex = 0;
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(80);  // PO ID
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(80);  // PR ID
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(200); // Notes
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Status
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(150); // Created At
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Created By
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(150); // Updated At
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Updated By
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(150); // Received At
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Received By
        poTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Actions


        POActionButtonPanel actionPanel = new POActionButtonPanel(this.poTable);
        int actionsColumnIndex = poColumnNames.length - 1;
        poTable.getColumnModel().getColumn(actionsColumnIndex).setCellRenderer(actionPanel);
        poTable.getColumnModel().getColumn(actionsColumnIndex).setCellEditor(actionPanel);

        poTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int viewRow = poTable.rowAtPoint(e.getPoint());
                    int viewColumn = poTable.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewColumn >= 0 && viewColumn != actionsColumnIndex) {
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

    // --- MODIFIED POActionButtonPanel ---
    class POActionButtonPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final JButton statusButton; // Made statusButton an instance variable
        private final JTable containingTable;
        private int currentRow;

        private final String APPROVED_STATUS_STRING = PO_STATUS_DIALOG_OPTIONS[1];

        public POActionButtonPanel(JTable table) {
            this.containingTable = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5));
            panel.setOpaque(true); // Good practice for renderers

            // Create the button using the outer class's method.
            // The ActionListener will only be effectively triggered if the button is enabled.
            statusButton = createActionButton("Update Status", e -> {
                fireEditingStopped(); // Important for JTable to commit any editor changes

                // 'currentRow' is set by getTableCellEditorComponent
                if (currentRow >= 0 && currentRow < this.containingTable.getRowCount()) {
                    String poId = (String) this.containingTable.getValueAt(currentRow, 0);
                    Object statusValue = this.containingTable.getValueAt(currentRow, 3); // Status column index
                    // Default to first option if current status is null, or use actual status
                    String currentStatusStr = (statusValue != null) ? statusValue.toString() : PO_STATUS_DIALOG_OPTIONS[0];

                    updatePOStatusDialog(poId, currentStatusStr);
                } else {
                    System.err.println("POActionButtonPanel: Invalid currentRow (" + currentRow + ") when Update Status clicked.");
                }
            });
            panel.add(statusButton);
            // NO data access (getValueAt) in constructor anymore
        }

        private void refreshButtonEnabledState(JTable tableContext, int row) {
            boolean enableButton = false;
            if (row >= 0 && row < tableContext.getRowCount()) {
                // Status is at column index 3 ("Status")
                Object statusCell = tableContext.getValueAt(row, 3);
                String currentStatus = (statusCell != null) ? statusCell.toString() : "";

                if (APPROVED_STATUS_STRING.equalsIgnoreCase(currentStatus)) {
                    enableButton = true;
                }
            }
            statusButton.setEnabled(enableButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            // Update the button's enabled state for the current row being rendered
            refreshButtonEnabledState(tbl, r);

            // Set panel background (using your existing logic for colors)
            if (isSel) {
                panel.setBackground(tbl.getSelectionBackground());
            } else {
                // 'darkBlue' is an accessible member of the outer class IMPurchaseOrderView
                panel.setBackground(r % 2 == 0 ? darkBlue : new Color(40, 50, 70));
            }
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object val, boolean isSel, int r, int c) {
            this.currentRow = r; // Set the current row when editing begins

            refreshButtonEnabledState(tbl, r);

            panel.setBackground(tbl.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return ""; // Or any other appropriate value, like the button's text or an action command
        }

        @Override
        public boolean stopCellEditing() {
            // Important to call super.stopCellEditing() for proper event firing
            return super.stopCellEditing();
        }
    }

    // TODO: Implement the method to show the Create PO form
    private void updatePOStatusDialog(String poId, String currentStatusStr) {
        JFrame parentDialogFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        int currentStatusIndex = 0;
        StockController stockController = new StockController();
        for (int i = 0; i < PO_STATUS_DIALOG_OPTIONS.length; i++) {
            if (PO_STATUS_DIALOG_OPTIONS[i].equalsIgnoreCase(currentStatusStr)) {
                currentStatusIndex = i;
                break;
            }
        }

        String newStatusStr = (String) JOptionPane.showInputDialog(
                parentDialogFrame,
                "Select new status for PO: " + poId,
                "Update PO Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                PO_STATUS_DIALOG_OPTIONS,
                PO_STATUS_DIALOG_OPTIONS[currentStatusIndex] // Default to current status
        );

        if (newStatusStr != null) { // User selected a status and didn't cancel
            int newStatusInt = -1;
            for (int i = 0; i < PO_STATUS_DIALOG_OPTIONS.length; i++) {
                if (PO_STATUS_DIALOG_OPTIONS[i].equals(newStatusStr)) {
                    newStatusInt = i;
                    break;
                }
            }

            if (newStatusInt != -1) {
                boolean operationSuccess = false;
                String successMessage = "";
                String errorMessage = "";

                try {
                    if (newStatusInt == 2) { // If new status is "Received"
                        operationSuccess = stockController.markPurchaseOrderAsReceived(poId);
                        if (operationSuccess) {
                            successMessage = "PO " + poId + " marked as received and stock updated successfully.";
                        } else {
                            // markPurchaseOrderAsReceived typically prints specific errors to System.err
                            errorMessage = "Failed to process PO " + poId + " as received. Check console for details.";
                        }
                    } else { // For other statuses (e.g., "Pending", "Approved", "Cancelled")
                        operationSuccess = poController.updatePurchaseOrderStatus(poId, newStatusInt);
                        if (operationSuccess) {
                            successMessage = "PO " + poId + " status updated to " + newStatusStr + ".";
                        } else {
                            errorMessage = "Failed to update status for PO " + poId + ".";
                        }
                    }

                    if (operationSuccess) {
                        JOptionPane.showMessageDialog(parentDialogFrame, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(parentDialogFrame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentDialogFrame, "Error during PO status update: " + ex.getMessage(), "Operation Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshTable(); // Refresh table to show the updated status or reflect failed attempts
                }
            }
        }
    }


    private void showPurchaseOrderDetailsPopup(String poId) {
        JFrame parentDialogFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        // poController is an instance variable, no need to re-initialize
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

        JPanel headerDetailsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        headerDetailsPanel.setBackground(mediumBlue);
        headerDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Header Information",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        addDetailRow(headerDetailsPanel, "PO ID:", po.getPoId());
        addDetailRow(headerDetailsPanel, "PR ID:", po.getPrId());
        addDetailRow(headerDetailsPanel, "Notes:", po.getNotes());
        if (this.poRowMapper == null) this.poRowMapper = new PurchaseOrderRowMapper();
        addDetailRow(headerDetailsPanel, "Status:", poRowMapper.getStatusString(po.getStatus()));
        addDetailRow(headerDetailsPanel, "Created At:", po.getCreatedAt());
        addDetailRow(headerDetailsPanel, "Created By:", po.getCreatedBy());
        addDetailRow(headerDetailsPanel, "Updated At:", po.getUpdatedAt());
        addDetailRow(headerDetailsPanel, "Updated By:", po.getUpdatedBy());
        addDetailRow(headerDetailsPanel, "Received At:", po.getReceivedAt() != null ? po.getReceivedAt() : "N/A");
        addDetailRow(headerDetailsPanel, "Received By:", po.getReceivedBy() != null ? po.getReceivedBy() : "N/A");

        mainDetailPanel.add(headerDetailsPanel);
        mainDetailPanel.add(Box.createRigidArea(new Dimension(0,15)));

        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(mediumBlue);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Ordered Items",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        // Add "Supplier" column to items table in popup
        String[] itemTableColumns = {"Item ID", "Item Code", "Item Name", "Supplier", "Qty", "Unit Price (ea.)", "Total Price"};
        DefaultTableModel itemsDetailTableModel = new DefaultTableModel(itemTableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
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
                        itemSupplierDisplay, // Display item's supplier
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(displayPrice),
                        CURRENCY_FORMAT.format(displayTotalPrice)
                });
            }
        }
        JTable itemsDetailTable = new JTable(itemsDetailTableModel);
        itemsDetailTable.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsDetailTable.setRowHeight(25);
        itemsDetailTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Supplier column width

        JScrollPane itemsScrollPane = new JScrollPane(itemsDetailTable);
        itemsScrollPane.setPreferredSize(new Dimension(650, 150));
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);

        mainDetailPanel.add(itemsPanel);

        JScrollPane detailScrollPane = new JScrollPane(mainDetailPanel);
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

        detailDialog.setMinimumSize(new Dimension(700, 600));
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(parentDialogFrame);
        detailDialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelName = new JLabel(label);
        labelName.setForeground(textWhite);
        labelName.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(labelName);

        JLabel labelValue = new JLabel(value != null && !value.trim().isEmpty() ? value : "N/A");
        labelValue.setForeground(verylightBlue);
        labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(labelValue);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("Test PO Management View");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            IMPurchaseOrderView poView = new IMPurchaseOrderView();
            testFrame.getContentPane().add(poView.createPurchaseOrderPanel()); // Use the panel directly or the frame
            testFrame.pack();
            testFrame.setLocationRelativeTo(null);
            testFrame.setVisible(true);
        });
    }
}