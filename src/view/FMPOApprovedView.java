package view;

import controller.FMController;
import controller.FinanceController;
import controller.SupplierController;
import model.PurchaseOrder;
import model.PurchaseOrderItem;
import util.table.mappers.PurchaseOrderRowMapper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FMPOApprovedView extends JFrame {
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
    private FinanceController financeController;

    private static final Color DIALOG_BACKGROUND = UITheme.MEDIUM_BLUE;
    private static final Color LABEL_TEXT_COLOR = UITheme.TEXT_WHITE;
    private static final Color EMPHASIZED_TEXT_COLOR = UITheme.VERY_LIGHT_BLUE;
    private static final Color PAY_BUTTON_BACKGROUND = UITheme.SUCCESS_GREEN;
    private static final Color PAY_BUTTON_TEXT = UITheme.TEXT_WHITE;
    private static final Color CANCEL_BUTTON_BACKGROUND = new Color(108, 117, 125);
    private static final Color CANCEL_BUTTON_TEXT = UITheme.TEXT_WHITE;

    private final String[] columnNames = {
            "PO ID", "PR ID", "Notes", "Status", "Created At", "Created By",
            "Updated At", "Updated By", "Received At", "Received By"
    };
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_RECEIVED = 2; // Assuming 2 means "Received"

    public FMPOApprovedView() {
        setTitle("Approved Purchase Orders & Payment Processing");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        fmController = new FMController();
        supplierController = new SupplierController();
        poRowMapper = new PurchaseOrderRowMapper();
        financeController = new FinanceController();

        setLayout(new BorderLayout());
        add(createFMPOApprovedPanel(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(1200, 600));
        setLocationRelativeTo(null);
    }

    public JPanel createFMPOApprovedPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Approved Purchase Orders & Payment Processing");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createPOTable(), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        JButton generateFinancialReportButton = new JButton("Generate Financial Transactions Report");
        generateFinancialReportButton.setOpaque(true);
        generateFinancialReportButton.setBackground(UITheme.HIGHLIGHT_BLUE); // Consistent color
        generateFinancialReportButton.setForeground(Color.WHITE);
        generateFinancialReportButton.setFocusPainted(false);
        generateFinancialReportButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        generateFinancialReportButton.addActionListener(event -> {
            String savePath = System.getProperty("user.home") + "/Downloads/Finance_Transactions_Report.csv";
            try {
                financeController.generateFinancialTransactionsReport(savePath);
                JOptionPane.showMessageDialog(
                        null,
                        "Financial Transactions Report saved to: " + savePath,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error generating financial report: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (UnsupportedOperationException uoe) {
                JOptionPane.showMessageDialog(
                        null,
                        "Report generation for financial transactions is not yet implemented.\n" + uoe.getMessage(),
                        "Feature Not Implemented",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.add(generateFinancialReportButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

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
                    System.err.println("FMPOApprovedView.refreshTable(): poTableModel is null and table not ready.");
                    return;
                }
            }
            if (fmController == null) fmController = new FMController();
            if (supplierController == null) supplierController = new SupplierController();
            if (poRowMapper == null) poRowMapper = new PurchaseOrderRowMapper();

            poTableModel.setRowCount(0);
            List<PurchaseOrder> allPoHeaders = fmController.getAllPurchaseOrderHeaders();

            if (allPoHeaders != null && !allPoHeaders.isEmpty()) {
                for (PurchaseOrder purchaseOrder : allPoHeaders) {
                    if (purchaseOrder != null && (purchaseOrder.getStatus() == STATUS_APPROVED || purchaseOrder.getStatus() == STATUS_RECEIVED)) {
                        String poHeader = purchaseOrder.toCSV();
                        String[] fields = poHeader.split(",", -1);
                        Object[] rowData = poRowMapper.mapFieldsToRow(fields, columnNames.length);
                        if (rowData != null) {
                            poTableModel.addRow(rowData);
                        }
                    }
                }
            } else {
                System.out.println("No Approved or Received Purchase Orders retrieved from controller.");
            }
            if (poTableModel.getRowCount() == 0) {
                System.out.println("No Approved or Received Purchase Orders to display.");
            }
        });
    }

    private JScrollPane createPOTable() {
        if (supplierController == null) supplierController = new SupplierController();
        if (poRowMapper == null) poRowMapper = new PurchaseOrderRowMapper();

        this.poTableModel = new DefaultTableModel(this.columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.poTable = new JTable(this.poTableModel);
        styleTable(poTable);

        poTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click to open details
                    int viewRow = poTable.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
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

    private void styleTable(JTable table) {
        table.setBackground(darkBlue);
        table.setForeground(textWhite);
        table.setSelectionBackground(lightBlue);
        table.setSelectionForeground(textWhite);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(veryLightBlue);
        table.getTableHeader().setForeground(textWhite); // Darker text for better contrast on veryLightBlue
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setGridColor(mediumBlue); // Grid color
        table.setShowGrid(true); // Show grid lines

        // Column widths (adjust as needed)
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // PO ID
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // PR ID
        table.getColumnModel().getColumn(2).setPreferredWidth(250); // Notes
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(150); // Created At
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Created By
        table.getColumnModel().getColumn(6).setPreferredWidth(150); // Updated At
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Updated By
        table.getColumnModel().getColumn(8).setPreferredWidth(150); // Received At
        table.getColumnModel().getColumn(9).setPreferredWidth(100);// Received By
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
        detailDialog.setMinimumSize(new Dimension(700, 550));

        JPanel mainDetailPanel = new JPanel();
        mainDetailPanel.setLayout(new BoxLayout(mainDetailPanel, BoxLayout.Y_AXIS));
        mainDetailPanel.setBackground(mediumBlue);
        mainDetailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Details Panel
        JPanel headerDetailsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        headerDetailsPanel.setBackground(mediumBlue);
        headerDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Header Information",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        if (poRowMapper == null) poRowMapper = new PurchaseOrderRowMapper(); // Ensure mapper is initialized

        addDetailRowToPanel(headerDetailsPanel, "PO ID:", purchaseOrder.getPoId());
        addDetailRowToPanel(headerDetailsPanel, "PR ID:", purchaseOrder.getPrId());
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

        // Items Panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(mediumBlue);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Ordered Items",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        String[] itemTableColumns = {"Item Code", "Item Name", "Supplier", "Qty", "Unit Price", "Total Price"};
        DefaultTableModel itemsDetailTableModel = new DefaultTableModel(itemTableColumns, 0);
        if (purchaseOrder.getItems() != null) {
            for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                String supplierDisplay = "N/A";
                if (item.getSelectedSupplierId() != null && !item.getSelectedSupplierId().isEmpty()) {
                    String supData = supplierController.getOneWithId(item.getSelectedSupplierId()); // Using SupplierController
                    if (supData != null) {
                        String[] supParts = supData.split(",");
                        supplierDisplay = supParts.length > 1 ? supParts[1] : supParts[0]; // Show name if available, else ID
                    } else {
                        supplierDisplay = item.getSelectedSupplierId() + " (Details N/A)";
                    }
                }
                itemsDetailTableModel.addRow(new Object[]{
                        item.getItemCode(), item.getItemName(), supplierDisplay,
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(item.getPrice() / 100.0), // Assuming price in cents
                        CURRENCY_FORMAT.format(item.getTotalPrice() / 100.0) // Assuming total in cents
                });
            }
        }
        JTable itemsDetailTable = new JTable(itemsDetailTableModel);
        stylePopupTable(itemsDetailTable); // Apply styling to the popup's item table
        JScrollPane itemsScrollPane = new JScrollPane(itemsDetailTable);
        itemsScrollPane.setPreferredSize(new Dimension(650, 150));
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        mainDetailPanel.add(itemsPanel);

        JScrollPane detailScrollPane = new JScrollPane(mainDetailPanel);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(mediumBlue);
        detailDialog.add(detailScrollPane, BorderLayout.CENTER);

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtonPanel.setBackground(mediumBlue);
        bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton processPaymentButton = new JButton("Process Payment");
        configureButtonStyle(processPaymentButton, PAY_BUTTON_BACKGROUND, PAY_BUTTON_TEXT, new Font("Arial", Font.BOLD, 13));
        processPaymentButton.setVisible(purchaseOrder.getStatus() == STATUS_RECEIVED);
        processPaymentButton.addActionListener(e -> {
            detailDialog.dispose();
            showGroupedPaymentDialog(purchaseOrder);
        });

        JButton closeButton = new JButton("Close");
        configureButtonStyle(closeButton, CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_TEXT, new Font("Arial", Font.BOLD, 13));
        closeButton.addActionListener(e -> detailDialog.dispose());

        if(processPaymentButton.isVisible()) {
            bottomButtonPanel.add(processPaymentButton);
        }
        bottomButtonPanel.add(closeButton);
        detailDialog.add(bottomButtonPanel, BorderLayout.SOUTH);


        detailDialog.setMinimumSize(new Dimension(700, Math.max(550, detailDialog.getPreferredSize().height))); // Ensure min size
        detailDialog.setLocationRelativeTo(getParentFrame());
        detailDialog.setVisible(true);
    }

    private void stylePopupTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setEnabled(false); // Read-only
        table.setBackground(darkBlue);
        table.setForeground(textWhite);
        table.setGridColor(mediumBlue);
        table.getTableHeader().setBackground(lightBlue);
        table.getTableHeader().setForeground(textWhite);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    }


    private void showGroupedPaymentDialog(PurchaseOrder po) {
        JDialog paymentDialog = new JDialog(getParentFrame(), "Process Payment for PO: " + po.getPoId(), true);
        paymentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        paymentDialog.getContentPane().setBackground(DIALOG_BACKGROUND);
        paymentDialog.setMinimumSize(new Dimension(600, 450)); // Adjusted size
        paymentDialog.setLayout(new BorderLayout(10,10));
//        paymentDialog.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        // Group items by supplier
        Map<String, List<PurchaseOrderItem>> itemsBySupplier = new HashMap<>();
        Map<String, String> supplierNames = new HashMap<>(); // Store supplier names for display
        double grandTotalPayment = 0;

        if (po.getItems() != null) {
            for (PurchaseOrderItem item : po.getItems()) {
                String supplierId = item.getSelectedSupplierId();
                if (supplierId == null || supplierId.trim().isEmpty()) {
                    supplierId = "UNKNOWN_SUPPLIER"; // Group items with no specified supplier
                }
                itemsBySupplier.computeIfAbsent(supplierId, k -> new ArrayList<>()).add(item);
                if (!supplierNames.containsKey(supplierId) && !"UNKNOWN_SUPPLIER".equals(supplierId)) {
                    String supData = supplierController.getOneWithId(supplierId);
                    if (supData != null) {
                        supplierNames.put(supplierId, supData.split(",")[1]); // Assuming name is at index 1
                    } else {
                        supplierNames.put(supplierId, supplierId); // Fallback to ID
                    }
                } else if ("UNKNOWN_SUPPLIER".equals(supplierId)) {
                    supplierNames.put(supplierId, "Unknown/Direct");
                }
                grandTotalPayment += item.getTotalPrice();
            }
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(DIALOG_BACKGROUND);

        JLabel poIdLabel = new JLabel("Processing Payment for PO ID: " + po.getPoId());
        configureLabelStyle(poIdLabel, new Font("Arial", Font.BOLD, 16), EMPHASIZED_TEXT_COLOR);
        poIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(poIdLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0,15)));

        JPanel itemsGridPanel = new JPanel(); // Will hold supplier panels in a grid or box layout
        itemsGridPanel.setLayout(new BoxLayout(itemsGridPanel, BoxLayout.Y_AXIS)); // Vertical list of supplier panels
        itemsGridPanel.setBackground(DIALOG_BACKGROUND);


        for (Map.Entry<String, List<PurchaseOrderItem>> entry : itemsBySupplier.entrySet()) {
            String supplierId = entry.getKey();
            List<PurchaseOrderItem> supplierItems = entry.getValue();
            String supplierName = supplierNames.getOrDefault(supplierId, supplierId);
            double supplierSubtotal = 0;

            JPanel supplierPanel = new JPanel(new BorderLayout(5,5));
            supplierPanel.setBackground(darkBlue); // Slightly different background for each supplier block
            supplierPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(lightBlue),
                    "Supplier: " + supplierName + " (" + supplierId + ")",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("Arial", Font.BOLD, 13), textWhite
            ));

            DefaultTableModel paymentItemsModel = new DefaultTableModel(new String[]{"Item", "Amount"}, 0){
                @Override public boolean isCellEditable(int r, int c){ return false; }
            };
            for (PurchaseOrderItem item : supplierItems) {
                double itemAmount = item.getTotalPrice() / 100.0; // Assuming cents
                paymentItemsModel.addRow(new Object[]{item.getItemName() + " (Code: " + item.getItemCode() + ")", CURRENCY_FORMAT.format(itemAmount)});
                supplierSubtotal += itemAmount;
            }
            JTable paymentItemsTable = new JTable(paymentItemsModel);
            stylePopupTable(paymentItemsTable); // Reuse styling
            paymentItemsTable.setTableHeader(null); // Hide table header for cleaner look in list

            JScrollPane itemsForSupplierScrollPane = new JScrollPane(paymentItemsTable);
            itemsForSupplierScrollPane.setPreferredSize(new Dimension(500, Math.min(80, supplierItems.size() * 25 + 5))); // Dynamic height
            supplierPanel.add(itemsForSupplierScrollPane, BorderLayout.CENTER);

            JLabel subtotalLabel = new JLabel("Subtotal for " + supplierName + ": " + CURRENCY_FORMAT.format(supplierSubtotal));
            configureLabelStyle(subtotalLabel, new Font("Arial", Font.BOLD, 13), EMPHASIZED_TEXT_COLOR);
            subtotalLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            supplierPanel.add(subtotalLabel, BorderLayout.SOUTH);

            itemsGridPanel.add(supplierPanel);
            itemsGridPanel.add(Box.createRigidArea(new Dimension(0,10))); // Spacer between supplier blocks
        }

        JScrollPane mainScrollPane = new JScrollPane(itemsGridPanel); // Scroll if many suppliers
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getViewport().setBackground(DIALOG_BACKGROUND);
        contentPanel.add(mainScrollPane);
        contentPanel.add(Box.createRigidArea(new Dimension(0,10)));


        JLabel grandTotalLabel = new JLabel("Grand Total Payment: " + CURRENCY_FORMAT.format(grandTotalPayment / 100.0));
        configureLabelStyle(grandTotalLabel, new Font("Arial", Font.BOLD, 16), EMPHASIZED_TEXT_COLOR);
        grandTotalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(grandTotalLabel);

        paymentDialog.add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(DIALOG_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        JButton confirmPaymentButton = new JButton("Confirm Payment");
        configureButtonStyle(confirmPaymentButton, PAY_BUTTON_BACKGROUND, PAY_BUTTON_TEXT, new Font("Arial", Font.BOLD, 13));
        confirmPaymentButton.addActionListener(e -> {
            // CONCEPTUAL: Call FinanceController to record transactions
            // For now, we'll simulate this.
            System.out.println("FinanceController.recordTransactions called (simulated) for PO: " + po.getPoId());
            List<Map<String, String>> transactions = new ArrayList<>();
            for (PurchaseOrderItem item : po.getItems()) {
                Map<String, String> transaction = new HashMap<>();
                transaction.put("PO_ID", po.getPoId());
                transaction.put("ItemID", item.getItemId());
                transaction.put("ItemName", item.getItemName());
                transaction.put("SupplierID", item.getSelectedSupplierId());
                transaction.put("SupplierName", supplierNames.getOrDefault(item.getSelectedSupplierId(), item.getSelectedSupplierId()));
                transaction.put("AmountPaid", String.valueOf(item.getTotalPrice())); // Store in cents
                transaction.put("PaymentDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                // transaction.put("ProcessedByUser", SessionController.getInstance().getUserId()); // Assuming SessionController
                System.out.println("  - Transaction: " + transaction);
                transactions.add(transaction);
            }
             financeController.recordTransactions(transactions);

            JOptionPane.showMessageDialog(paymentDialog, "Payment processed successfully for PO: " + po.getPoId() + " (Simulated - check console for transaction details).", "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
            paymentDialog.dispose();
            refreshTable();
        });

        JButton cancelButton = new JButton("Cancel");
        configureButtonStyle(cancelButton, CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_TEXT, new Font("Arial", Font.BOLD, 13));
        cancelButton.addActionListener(e -> paymentDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmPaymentButton);
        paymentDialog.add(buttonPanel, BorderLayout.SOUTH);

        paymentDialog.setLocationRelativeTo(getParentFrame());
        paymentDialog.setVisible(true);
    }

    private void addDetailRowToPanel(JPanel panel, String label, String value) {
        JLabel labelName = new JLabel(label);
        labelName.setForeground(textWhite);
        labelName.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(labelName);

        JTextArea labelValueArea = new JTextArea(value != null && !value.trim().isEmpty() ? value : "N/A");
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

    private void configureLabelStyle(JLabel label, Font font, Color textColor) {
        label.setFont(font);
        label.setForeground(textColor);
    }

    private void configureButtonStyle(JButton button, Color bgColor, Color fgColor, Font font) {
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}