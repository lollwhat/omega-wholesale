package view;

import controller.FMController;
import controller.SupplierController;
import controller.FileController;
import controller.SessionController;

import util.table.mappers.PurchaseOrderRowMapper;
import model.PurchaseOrder;
import model.PurchaseOrderItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FMPOApprovalView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;
    protected final Color veryLightBlue = UITheme.VERY_LIGHT_BLUE;

    private JTable poTable;
    private DefaultTableModel poTableModel;
    private FMController fmController;
    private PurchaseOrderRowMapper poRowMapper;
    private SupplierController supplierController;

    private final String[] columnNames = {
            "PO ID", "PR ID", "Supplier", "Notes", "Status", "Created At", "Actions"
    };
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_CANCELLED = 3;

    public FMPOApprovalView() {
        setTitle("Finance Manager Purchase Order Approval");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        fmController = new FMController();
        supplierController = new SupplierController();
        poRowMapper = new PurchaseOrderRowMapper();

        setLayout(new BorderLayout());
        add(createFMPOApprovalPanel(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(1200, 600));
        setLocationRelativeTo(null);
    }

    public JPanel createFMPOApprovalPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Purchase Order Approval");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createPOTable(), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        refreshTable();
        return mainPanel;
    }

    private JFrame getParentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            if (poTableModel == null) {
                if (poTable != null && poTable.getModel() instanceof DefaultTableModel) {
                    poTableModel = (DefaultTableModel) poTable.getModel();
                } else {
                    System.err.println("FMPOApprovalView.refreshTable(): poTableModel is null and table not ready.");
                    return;
                }
            }
            if (fmController == null) fmController = new FMController();
            if (supplierController == null) supplierController = new SupplierController();
            if (poRowMapper == null) poRowMapper = new PurchaseOrderRowMapper();

            poTableModel.setRowCount(0);
            List<PurchaseOrder> allPoHeaders = fmController.getAllPurchaseOrderHeaders();

            if (allPoHeaders != null) {
                for (PurchaseOrder purchaseOrder : allPoHeaders) {
                    if (purchaseOrder != null && purchaseOrder.getStatus() == STATUS_PENDING) {
//                        Object[] rowData = poRowMapper.mapPurchaseOrderToRow(purchaseOrder, columnNames.length - 1);
//                        if (rowData != null) {
//                            poTableModel.addRow(rowData);
//                        }
                    }
                }
            } else {
                System.out.println("No Purchase Orders retrieved from controller.");
            }
            if (poTableModel.getRowCount() == 0) {
                System.out.println("No Purchase Orders Pending.");
            }
        });
    }

    private JScrollPane createPOTable() {
        if (supplierController == null) supplierController = new SupplierController();
        if (poRowMapper == null) poRowMapper = new PurchaseOrderRowMapper();

        this.poTableModel = new DefaultTableModel(this.columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == columnNames.length - 1;
            }
        };

        this.poTable = new JTable(this.poTableModel);

        poTable.setBackground(darkBlue);
        poTable.setForeground(textWhite);
        poTable.setSelectionBackground(lightBlue);
        poTable.setSelectionForeground(textWhite);
        poTable.setRowHeight(40);
        poTable.getTableHeader().setBackground(veryLightBlue);
        poTable.getTableHeader().setForeground(textWhite);
        poTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        poTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PO ID
        poTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // PR ID
        poTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Notes
        poTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Supplier
        poTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        poTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Created At
        poTable.getColumnModel().getColumn(6).setPreferredWidth(150);// Actions

        FMPOActionButtonPanel actionPanel = new FMPOActionButtonPanel(this.poTable);
        int actionsColumnModelIndex = poTable.getColumn("Actions").getModelIndex();
        poTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellRenderer(actionPanel);
        poTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellEditor(actionPanel);

        poTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = poTable.rowAtPoint(e.getPoint());
                    int viewColumn = poTable.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewColumn >= 0 && poTable.convertColumnIndexToModel(viewColumn) != actionsColumnModelIndex) {
                        int modelRow = poTable.convertRowIndexToModel(viewRow);
                        String poId = (String) poTableModel.getValueAt(modelRow, 0);
                        if (poId != null && !poId.trim().isEmpty()) {
                            showPODetailsPopup(poId);
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

    private JButton createStyledActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setMargin(new Insets(4, 8, 4, 8));
        button.setForeground(textWhite);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }


    class FMPOActionButtonPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final JButton approveButton;
        private final JButton rejectButton; // Added Reject button
        private JTable containingTable;
        private int currentRow;

        public FMPOActionButtonPanel(JTable table) {
            this.containingTable = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            approveButton = createStyledActionButton("Approve");
            approveButton.setBackground(UITheme.SUCCESS_GREEN);
            rejectButton = createStyledActionButton("Reject");
            rejectButton.setBackground(UITheme.ERROR_RED);

//            approveButton.addActionListener(e -> handleApproveOrReject(1)); // 1 for approved
//            rejectButton.addActionListener(e -> handleApproveOrReject(2));  // 2 for rejected

            approveButton.addActionListener(e -> {
                fireEditingStopped();
                // currentRow here is the model row, set by getTableCellEditorComponent
                String poId = (String) containingTable.getModel().getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(getParentFrame(),
                        "Are you sure you want to APPROVE PO ID: " + poId + "?",
                        "Confirm Approval", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    handleStatusUpdate(poId, STATUS_APPROVED);
                }
            });

            rejectButton.addActionListener(e -> {
                fireEditingStopped();
                String poId = (String) containingTable.getModel().getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(getParentFrame(),
                        "Are you sure you want to REJECT (Cancel) PO ID: " + poId + "?",
                        "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    handleStatusUpdate(poId, STATUS_CANCELLED);
                }
            });

            panel.add(approveButton);
            panel.add(rejectButton);
        }

        private void handleStatusUpdate(String poId, int newStatus) {
            try {
                String fmUserId = SessionController.getInstance().getUserId();
                // Assuming your FMController has updatePurchaseOrderStatus
                boolean success = fmController.updatePurchaseOrderStatus(poId, newStatus, fmUserId);
                if (success) {
                    String action = (newStatus == STATUS_APPROVED) ? "approved" : "rejected (cancelled)";
                    JOptionPane.showMessageDialog(getParentFrame(), "PO " + poId + " " + action + " successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(getParentFrame(), "Failed to update status for PO " + poId + ".", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(getParentFrame(), "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            this.currentRow = r;
            panel.setBackground(isSel ? tbl.getSelectionBackground() : (r % 2 == 0 ? darkBlue : new Color(40, 50, 70)));
            return panel;
        }
        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object val, boolean isSel, int r, int c) {
            this.currentRow = tbl.convertRowIndexToModel(r);
            panel.setBackground(tbl.getSelectionBackground()); // Use selection color when cell is edited
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return ""; }

        @Override
        public boolean stopCellEditing() {
            super.stopCellEditing();
            return true;
        }
    }


    private void showPODetailsPopup(String poId) {
        PurchaseOrder purchaseOrder = fmController.getFullPurchaseOrderDetailsById(poId);
        if (purchaseOrder == null) {
            JOptionPane.showMessageDialog(getParentFrame(), "Details for PO " + poId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog detailDialog = new JDialog(getParentFrame(), "Purchase Order Details: " + poId, true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.getContentPane().setBackground(mediumBlue);
        detailDialog.setMinimumSize(new Dimension(700, 550)); // Adjusted size slightly

        JPanel mainDetailPanel = new JPanel();
        mainDetailPanel.setLayout(new BoxLayout(mainDetailPanel, BoxLayout.Y_AXIS));
        mainDetailPanel.setBackground(mediumBlue);
        mainDetailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerDetailsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        headerDetailsPanel.setBackground(mediumBlue);
        headerDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Header Information",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        if (poRowMapper == null) poRowMapper = new PurchaseOrderRowMapper();

        addDetailRowToPanel(headerDetailsPanel, "PO ID:", purchaseOrder.getPoId());
        addDetailRowToPanel(headerDetailsPanel, "PR ID:", purchaseOrder.getPrId());
//        addDetailRowToPanel(headerDetailsPanel, "Supplier:", purchaseOrder.getSupplierId());
        addDetailRowToPanel(headerDetailsPanel, "Status:", poRowMapper.getStatusString(purchaseOrder.getStatus()));
        addDetailRowToPanel(headerDetailsPanel, "Notes:", purchaseOrder.getNotes());
        addDetailRowToPanel(headerDetailsPanel, "Created At:", purchaseOrder.getCreatedAt());
        addDetailRowToPanel(headerDetailsPanel, "Created By:", purchaseOrder.getCreatedBy());
        addDetailRowToPanel(headerDetailsPanel, "Updated At:", purchaseOrder.getUpdatedAt());
        addDetailRowToPanel(headerDetailsPanel, "Updated By:", purchaseOrder.getUpdatedBy());
        addDetailRowToPanel(headerDetailsPanel, "Received At:", Objects.toString(purchaseOrder.getReceivedAt(), "N/A"));
        addDetailRowToPanel(headerDetailsPanel, "Received By:", Objects.toString(purchaseOrder.getReceivedBy(), "N/A"));

        mainDetailPanel.add(headerDetailsPanel);
        mainDetailPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(mediumBlue);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Ordered Items",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        String[] itemTableColumns = {"Item ID", "Item Code", "Item Name", "Qty", "Unit Price", "Total Price"};
        DefaultTableModel itemsDetailTableModel = new DefaultTableModel(itemTableColumns, 0);
        if (purchaseOrder.getItems() != null) {
            for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                itemsDetailTableModel.addRow(new Object[]{
                        item.getItemId(), item.getItemCode(), item.getItemName(),
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(item.getPrice()),
                        CURRENCY_FORMAT.format(item.getTotalPrice())
                });
            }
        }
        JTable itemsDetailTable = new JTable(itemsDetailTableModel);
        itemsDetailTable.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsDetailTable.setRowHeight(25);
        itemsDetailTable.setEnabled(false); // Read-only
        itemsDetailTable.setBackground(darkBlue); // Consistent styling
        itemsDetailTable.setForeground(textWhite);
        itemsDetailTable.getTableHeader().setBackground(lightBlue);
        itemsDetailTable.getTableHeader().setForeground(textWhite);


        JScrollPane itemsScrollPane = new JScrollPane(itemsDetailTable);
        itemsScrollPane.setPreferredSize(new Dimension(650, 200)); // Increased height for items
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        mainDetailPanel.add(itemsPanel);

        JScrollPane detailScrollPane = new JScrollPane(mainDetailPanel); // Scroll the whole thing if too long
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(mediumBlue);


        detailDialog.add(detailScrollPane, BorderLayout.CENTER);
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(getParentFrame());
        detailDialog.setVisible(true);
    }

    private void addDetailRowToPanel(JPanel panel, String label, String value) {
        JLabel labelName = new JLabel(label);
        labelName.setForeground(textWhite);
        labelName.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(labelName);

        JTextArea labelValueArea = new JTextArea(value != null ? value : "N/A");
        labelValueArea.setWrapStyleWord(true);
        labelValueArea.setLineWrap(true);
        labelValueArea.setOpaque(false);
        labelValueArea.setEditable(false);
        labelValueArea.setForeground(veryLightBlue);
        labelValueArea.setFont(new Font("Arial", Font.PLAIN, 13));
        labelValueArea.setFocusable(false);
        labelValueArea.setBorder(null);
        panel.add(labelValueArea);
    }
}