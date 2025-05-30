package view;

import controller.FileController; // For static FileController path setting workaround
import controller.PurchaseRequisitionController;
import controller.PurchaseOrderController; // Will be needed for creating PO
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;
import model.PurchaseOrderItem; // For creating PO
import util.table.GenericModelHelper;
import util.table.mappers.PurchaseRequisitionRowMapper;
import view.forms.AddPurchaseOrderForm;
import view.forms.AddPurchaseRequisitionForm; // For viewing PR details if needed, or a new PO form
// We will need a new form for creating PO, e.g., CreatePurchaseOrderForm.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import controller.SessionController; // To get current user for "updatedBy"

public class PMPurchaseRequisitionView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;
    protected final Color veryLightBlue = UITheme.VERY_LIGHT_BLUE;

    private JTable prTable;
    private DefaultTableModel prTableModel;
    private PurchaseRequisitionController prController;
    private PurchaseRequisitionRowMapper prRowMapper;
    private PurchaseOrderController poController; // For creating POs later

    private final String[] prColumnNames = {
            "PR ID", "Notes", "Status",
            "Created At", "Created By", "Updated At", "Updated By", "Actions"
    };

    public PMPurchaseRequisitionView() {
        setTitle("Purchase Manager - View Requisitions");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        prController = new PurchaseRequisitionController();
        poController = new PurchaseOrderController(); // Initialize PO controller
        prRowMapper = new PurchaseRequisitionRowMapper();

        setLayout(new BorderLayout());
        add(createPMPurchaseRequisitionPanel(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 600));
        pack();
        setLocationRelativeTo(null);
    }

    public JPanel createPMPurchaseRequisitionPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Purchase Requisition Overview & Create Purchase Order");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

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

    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            if (prTableModel == null) {
                if (prTable != null && prTable.getModel() instanceof DefaultTableModel) {
                    prTableModel = (DefaultTableModel) prTable.getModel();
                } else {
                    System.err.println("PMPurchaseRequisitionView.refreshTable(): prTableModel is null and table not ready.");
                    return;
                }
            }
            if (prController == null) prController = new PurchaseRequisitionController();
            if (prRowMapper == null) prRowMapper = new PurchaseRequisitionRowMapper();

            prTableModel.setRowCount(0);
            List<PurchaseRequisition> prHeaders = prController.getAllPurchaseRequisitionHeaders();

            if (prHeaders != null && !prHeaders.isEmpty()) {
                for (PurchaseRequisition prHeaderObj : prHeaders) {
                    if (prHeaderObj == null) continue;
                    String headerCsvLine = prHeaderObj.toCSV();
                    String[] fields = headerCsvLine.split(",", -1);
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
        if (prRowMapper == null) prRowMapper = new PurchaseRequisitionRowMapper();

        List<String> initialEmptyData = new ArrayList<>();
        this.prTableModel = GenericModelHelper.createGenericTableModel(
                initialEmptyData,
                this.prColumnNames,
                this.prRowMapper,
                (row, col, totalCols) -> col == totalCols - 1 // Actions column editable
        );

        this.prTable = new JTable(this.prTableModel);
        styleTable(this.prTable); // Apply common styling
        prTable.setAutoCreateRowSorter(true);

        int colIdx = 0;
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(80);  // PR ID
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(250); // Notes
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(100); // Status
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(160); // Created At
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(100); // Created By
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(160); // Updated At
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(100); // Updated By
        prTable.getColumnModel().getColumn(colIdx++).setPreferredWidth(220); // Actions (wider for two buttons)

        PMActionButtonPanel actionPanel = new PMActionButtonPanel(this.prTable);
        int actionsColumnModelIndex = prTable.getColumn("Actions").getModelIndex();
        prTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellRenderer(actionPanel);
        prTable.getColumnModel().getColumn(actionsColumnModelIndex).setCellEditor(actionPanel);

        prTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int viewRow = prTable.rowAtPoint(e.getPoint());
                    int viewColumn = prTable.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewColumn >= 0 && viewColumn != actionsColumnModelIndex) {
                        int modelRow = prTable.convertRowIndexToModel(viewRow);
                        String prId = (String) prTable.getModel().getValueAt(modelRow, 0);
                        if (prId != null && !prId.trim().isEmpty()) {
                            // Use the existing details popup from PurchaseRequisitionView for now
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

    private void styleTable(JTable tableToStyle) {
        tableToStyle.setBackground(darkBlue);
        tableToStyle.setForeground(textWhite);
        tableToStyle.setGridColor(new Color(50, 60, 80));
        tableToStyle.setRowHeight(45);
        prTable.setFont(new Font("Arial", Font.PLAIN, 14));
        prTable.getTableHeader().setBackground(new Color(150, 165, 235));
        prTable.getTableHeader().setForeground(textWhite);
        prTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        prTable.setSelectionBackground(new Color(60, 70, 90));
        prTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        prTable.setAutoCreateRowSorter(true); // Enable sorting
    }

    private JButton createStyledActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 11)); // Slightly smaller font for more buttons
        button.setMargin(new Insets(2, 3, 2, 3));
        // Colors can be set per button type if needed
        button.setForeground(textWhite);
        button.setBackground(lightBlue);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    class PMActionButtonPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final JButton approveButton;
        private final JButton rejectButton; // Added Reject button
        private final JButton createPOButton;
        private JTable containingTable;
        private int currentRow;
        private final JLabel statusDisplayLabel;

        public PMActionButtonPanel(JTable table) {
            this.containingTable = table;
            // Using a GridBagLayout for more control if needed, or simple FlowLayout
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            approveButton = createStyledActionButton("Approve");
            rejectButton = createStyledActionButton("Reject");
            rejectButton.setBackground(UITheme.ERROR_RED); // Style reject button
            createPOButton = createStyledActionButton("Create PO");
            createPOButton.setBackground(UITheme.SUCCESS_GREEN); // Style PO button

            statusDisplayLabel = new JLabel("");
            statusDisplayLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            statusDisplayLabel.setForeground(textWhite);

            approveButton.addActionListener(e -> handleApproveOrReject(1)); // 1 for Approved
            rejectButton.addActionListener(e -> handleApproveOrReject(2));  // 2 for Rejected
            createPOButton.addActionListener(e -> handleCreatePO());

            // Add all components to the panel. Visibility will be controlled.
            panel.add(approveButton);
            panel.add(rejectButton);
            panel.add(createPOButton);
            panel.add(statusDisplayLabel);
        }

        private void handleApproveOrReject(int newStatus) {
            fireEditingStopped();
            String prId = (String) containingTable.getValueAt(currentRow, 0);
            String statusAction = (newStatus == 1) ? "approve" : "reject";
            int confirm = JOptionPane.showConfirmDialog(getParentFrame(),
                    "Are you sure you want to " + statusAction + " PR ID: " + prId + "?",
                    "Confirm Action", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    PurchaseRequisition prToUpdate = prController.getFullPurchaseRequisitionById(prId);
                    if (prToUpdate != null) {
                        prController.updatePurchaseRequisition(
                                prToUpdate.getPrId(),
                                prToUpdate.getNotes(),
                                newStatus,
                                prToUpdate.getItems()
                        );
                        refreshTable();
                        JOptionPane.showMessageDialog(getParentFrame(),
                                "PR ID " + prId + " status updated to '" + prRowMapper.getStatusString(newStatus) + "'.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(getParentFrame(), "Could not find PR " + prId + " to update.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(getParentFrame(),
                            "Error updating PR status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }

        private void handleCreatePO() {
            fireEditingStopped();
            String prId = (String) containingTable.getValueAt(currentRow, 0);
            PurchaseRequisition prForPO = prController.getFullPurchaseRequisitionById(prId);

            if (prForPO == null) {
                JOptionPane.showMessageDialog(getParentFrame(), "Could not load PR details for PO creation.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (prForPO.getStatus() != 1) { // Status 1 = Approved
                JOptionPane.showMessageDialog(getParentFrame(), "Purchase Order can only be created from an 'Approved' PR.", "Action Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }

            System.out.println("PMView: Triggering PO Creation for PR ID: " + prId);
            System.out.println("PMView: PR Items: " + prForPO.getItems().size());
            for(PurchaseRequisitionItem prItem : prForPO.getItems()){
                System.out.println("  - Item: " + prItem.getItemName() + ", Qty: " + prItem.getQuantity() + ", Suggested Suppliers: " + prItem.getSuggestedSupplierIds());
            }

            AddPurchaseOrderForm poForm = new AddPurchaseOrderForm(getParentFrame(), prForPO);
            poForm.setVisible(true);
//            JOptionPane.showMessageDialog(getParentFrame(), "Placeholder: Launch 'Create Purchase Order Form' for PR: " + prId, "Create PO", JOptionPane.INFORMATION_MESSAGE);
        }

        private void updateButtonStates(int row) {
            if (containingTable == null || row < 0 || row >= containingTable.getRowCount()) {
                approveButton.setVisible(false);
                rejectButton.setVisible(false);
                createPOButton.setVisible(false);
                statusDisplayLabel.setVisible(false);
                return;
            }

            int statusColumnIndex = containingTable.getColumn("Status").getModelIndex();
            String currentStatusDisplay = containingTable.getValueAt(row, statusColumnIndex).toString();

            // Default visibility
            approveButton.setVisible(false);
            rejectButton.setVisible(false);
            createPOButton.setVisible(false);
            statusDisplayLabel.setVisible(false);
            statusDisplayLabel.setText("");

            if ("Pending".equalsIgnoreCase(currentStatusDisplay)) {
                approveButton.setVisible(true);
                rejectButton.setVisible(true);
            } else if ("Approved".equalsIgnoreCase(currentStatusDisplay)) {
                createPOButton.setVisible(true);
            } else if ("Rejected".equalsIgnoreCase(currentStatusDisplay)) {
                statusDisplayLabel.setText("PR Rejected");
                statusDisplayLabel.setForeground(UITheme.ERROR_RED); //
                statusDisplayLabel.setVisible(true);
            } else if ("Cancelled".equalsIgnoreCase(currentStatusDisplay)) {
                statusDisplayLabel.setText("PR Cancelled");
                statusDisplayLabel.setForeground(Color.GRAY);
                statusDisplayLabel.setVisible(true);
            } else if ("PO Created".equalsIgnoreCase(currentStatusDisplay)) {
                 statusDisplayLabel.setText("PO Created");
                 statusDisplayLabel.setForeground(UITheme.SUCCESS_GREEN);
                 statusDisplayLabel.setVisible(true);
             }
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            this.currentRow = r;
            updateButtonStates(r);
            panel.setBackground(isSel ? tbl.getSelectionBackground() : (r % 2 == 0 ? darkBlue : new Color(40, 50, 70)));
            return panel;
        }
        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object val, boolean isSel, int r, int c) {
            this.currentRow = r;
            updateButtonStates(r);
            panel.setBackground(tbl.getSelectionBackground());
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public boolean stopCellEditing() { return super.stopCellEditing(); }
    }

    // In PMPurchaseRequisitionView.java

    private void showPurchaseRequisitionDetailsPopup(String prId) {
        JFrame parentFrame = getParentFrame();
        // Ensure prController path setting for its operations
        new FileController(this.prController.getPrHeaderFilePath()); // For PR header read by getFullPurchaseRequisitionById
        final PurchaseRequisition pr = prController.getFullPurchaseRequisitionById(prId); // Make pr final for use in inner classes

        if (pr == null) {
            JOptionPane.showMessageDialog(parentFrame, "Could not retrieve details for PR ID: " + prId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final JDialog detailDialog = new JDialog(parentFrame, "Purchase Requisition Details: " + prId, true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.getContentPane().setBackground(mediumBlue);

        // --- Main Detail Panel (Header + Items) ---
        JPanel mainDetailPanel = new JPanel();
        mainDetailPanel.setLayout(new BoxLayout(mainDetailPanel, BoxLayout.Y_AXIS));
        mainDetailPanel.setBackground(mediumBlue);
        mainDetailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Header Details Panel ---
        JPanel headerDetailsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        headerDetailsPanel.setBackground(mediumBlue);
        headerDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Header Information",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        addDetailRowToPanel(headerDetailsPanel, "PR ID:", pr.getPrId());
        addDetailRowToPanel(headerDetailsPanel, "Notes:", pr.getNotes());
        if (prRowMapper == null) prRowMapper = new PurchaseRequisitionRowMapper();
        addDetailRowToPanel(headerDetailsPanel, "Status:", prRowMapper.getStatusString(pr.getStatus()));
        addDetailRowToPanel(headerDetailsPanel, "Created At:", pr.getCreatedAt());
        addDetailRowToPanel(headerDetailsPanel, "Created By:", pr.getCreatedBy());
        addDetailRowToPanel(headerDetailsPanel, "Updated At:", pr.getUpdatedAt());
        addDetailRowToPanel(headerDetailsPanel, "Updated By:", pr.getUpdatedBy());
        mainDetailPanel.add(headerDetailsPanel);
        mainDetailPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Items Panel ---
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(mediumBlue);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Requested Items",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        String[] itemPopupTableColumns = {"Item ID", "Item Code", "Item Name", "Qty", "Unit Price (est.)", "Total Price (est.)", "Suggested Suppliers"};
        DefaultTableModel itemsDetailTableModel = new DefaultTableModel(itemPopupTableColumns, 0);

        if (pr.getItems() != null) {
            for (PurchaseRequisitionItem item : pr.getItems()) {
                if (item == null) continue;
                double displayPrice = item.getPrice() / 100.0; // Assuming price is in cents
                double displayTotalPrice = item.getTotalPrice() / 100.0; // Assuming total is in cents
                String suggestedSuppliersStr = item.getSuggestedSupplierIds().stream().collect(Collectors.joining(", "));
                if (suggestedSuppliersStr.isEmpty()) suggestedSuppliersStr = "N/A";

                itemsDetailTableModel.addRow(new Object[]{
                        item.getItemId(), item.getItemCode(), item.getItemName(),
                        item.getQuantity(), String.format("%.2f", displayPrice),
                        String.format("%.2f", displayTotalPrice), suggestedSuppliersStr
                });
            }
        }
        JTable itemsDetailTable = new JTable(itemsDetailTableModel);
        itemsDetailTable.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsDetailTable.setRowHeight(25);
        itemsDetailTable.setEnabled(false); // Make table view-only in popup
        itemsDetailTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Suggested Suppliers column
        JScrollPane itemsScrollPane = new JScrollPane(itemsDetailTable);
        itemsScrollPane.setPreferredSize(new Dimension(620, 150)); // Adjusted width
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        mainDetailPanel.add(itemsPanel);

        JScrollPane detailScrollPane = new JScrollPane(mainDetailPanel);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(mediumBlue);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align buttons to the right
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton approveButton = createStyledActionButton("Approve");
        JButton rejectButton = createStyledActionButton("Reject");
        JButton closeButton = createStyledActionButton("Close");
        closeButton.setBackground(lightBlue); // Keep close distinct or same style

        // Determine if Approve/Reject should be visible/enabled
        boolean canApproveOrReject = (pr.getStatus() == 0); // Status 0 = Pending

        approveButton.setVisible(canApproveOrReject);
        approveButton.setEnabled(canApproveOrReject);
        rejectButton.setVisible(canApproveOrReject);
        rejectButton.setEnabled(canApproveOrReject);

        approveButton.addActionListener(_ -> {
            performApproveOrRejectPR(pr, 1, detailDialog); // 1 for Approved
        });

        rejectButton.addActionListener(_ -> {
            performApproveOrRejectPR(pr, 2, detailDialog); // 2 for Rejected
        });

        closeButton.addActionListener(_ -> detailDialog.dispose());

        if (canApproveOrReject) {
            buttonPanel.add(approveButton);
            buttonPanel.add(rejectButton);
        }
        buttonPanel.add(closeButton);

        detailDialog.add(detailScrollPane, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailDialog.setMinimumSize(new Dimension(680, 600)); // Adjusted size
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(parentFrame);
        detailDialog.setVisible(true);
    }

    // Helper method to handle the actual approval/rejection logic
    private void performApproveOrRejectPR(PurchaseRequisition pr, int newStatus, JDialog parentDialog) {
        String actionText = (newStatus == 1) ? "approve" : "reject";
        int confirm = JOptionPane.showConfirmDialog(parentDialog,
                "Are you sure you want to " + actionText + " PR ID: " + pr.getPrId() + "?",
                "Confirm Action",
                JOptionPane.YES_NO_OPTION,
                (newStatus == 1) ? JOptionPane.QUESTION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // The existing updatePurchaseRequisition method can be used.
                // It requires all fields, so we pass them from the 'pr' object.
                // The PurchaseRequisitionController will handle setting the "updatedBy" and "updatedAt".
                prController.updatePurchaseRequisition(
                        pr.getPrId(),
                        pr.getNotes(),
                        newStatus, // The new status
                        pr.getItems() // Pass existing items
                );
                refreshTable(); // Refresh the main table in PMPurchaseRequisitionView
                parentDialog.dispose(); // Close the popup
                JOptionPane.showMessageDialog(getParentFrame(),
                        "PR ID " + pr.getPrId() + " has been " + prRowMapper.getStatusString(newStatus) + ".",
                        "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(getParentFrame(),
                        "Error updating PR status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
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