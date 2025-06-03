package view;

import controller.FMController;
import model.PurchaseOrder;
import model.PurchaseOrderItem;
import util.table.mappers.PurchaseOrderRowMapper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;

public class FinancePurchaseOrderView extends JFrame {
    private Color background = new Color(21, 31, 46);
    private Color panelColour = new Color(30, 41, 59);
    private Color lightBlue = new Color(96, 103, 205);
    private Color verylightBlue = new Color(165, 180, 252);
    private Color labelColour = new Color(78, 91, 249);
    private Color textColour = new Color(255, 255, 255);

    private JTable purchaseOrderTable;
    private PurchaseOrderRowMapper poRowMapper;
    private DefaultTableModel purchaseOrderTableModel;
    private final String[] columnNames = {"PO ID", "PR ID", "Notes", "Status", "Created At", "Created By",
            "Updated At", "Updated By", "Received At", "Received By"};
    private static final String[] PO_STATUS_DIALOG_OPTIONS = {"Processing", "Received", "Cancelled"};
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private FMController fmController;

    public FinancePurchaseOrderView() {
        this.fmController = new FMController();
        this.poRowMapper = new PurchaseOrderRowMapper();

        setTitle("View Purchase Orders");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(financePurchaseOrderPanel(), BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    public JPanel financePurchaseOrderPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(panelColour);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(panelColour);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel purchaseOrderTitle = new JLabel("View Purchase Orders");
        purchaseOrderTitle.setForeground(labelColour);
        purchaseOrderTitle.setFont(new Font("Arial", Font.BOLD, 20));
        purchaseOrderTitle.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(purchaseOrderTitle, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(panelColour);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        tablePanel.add(createTable(), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JScrollPane createTable() {
        this.purchaseOrderTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table non-editable
            }
        };

        purchaseOrderTable = new JTable(purchaseOrderTableModel);
        purchaseOrderTable.setBackground(background);
        purchaseOrderTable.setForeground(textColour);
        purchaseOrderTable.setSelectionBackground(lightBlue);
        purchaseOrderTable.setSelectionForeground(textColour);
        purchaseOrderTable.setRowHeight(40);
        purchaseOrderTable.getTableHeader().setBackground(verylightBlue);
        purchaseOrderTable.getTableHeader().setForeground(textColour);
        purchaseOrderTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        purchaseOrderTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PO ID
        purchaseOrderTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // PR ID
        purchaseOrderTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Notes
//        purchaseOrderTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Supplier ID
        purchaseOrderTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Status
        purchaseOrderTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Created At
        purchaseOrderTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Created By
        purchaseOrderTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Updated At
        purchaseOrderTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Updated By
        purchaseOrderTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Received At
        purchaseOrderTable.getColumnModel().getColumn(9).setPreferredWidth(100);// Received By

        purchaseOrderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = purchaseOrderTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String poId = purchaseOrderTableModel.getValueAt(selectedRow, 0).toString();
                        if (poId != null && !poId.trim().isEmpty()) {
                            showPurchaseOrderDetails(poId);
                        }
                    }
                }
            }
        });

        loadPurchaseOrders();

        JScrollPane scrollPane = new JScrollPane(purchaseOrderTable);
        scrollPane.getViewport().setBackground(background);

        return scrollPane;
    }

    private void loadPurchaseOrders() {
        if (this.purchaseOrderTableModel == null) {
            System.err.println("PurchaseOrderTableModel is not initialized.");
            return;
        }
        if (this.fmController == null) {
            System.err.println("FMController is not initialized.");
            return;
        }
        if(this.poRowMapper == null) {
            System.err.println("Failed to retrieve purchase orders from controller.");
            return;
        }

        this.purchaseOrderTableModel.setRowCount(0); // Clear existing rows

        List<PurchaseOrder> poHeader = fmController.getAllPurchaseOrderHeaders();

        if (poHeader == null) {
            System.err.println("Failed to retrieve purchase orders from controller.");
            return;
        }

        System.out.println("Purchase orders retrieved: " + poHeader.size());

        if (!poHeader.isEmpty()) {
            for (PurchaseOrder purchaseOrder : poHeader) {
                Object[] rowData = {
                        purchaseOrder.getPoId(),
                        purchaseOrder.getPrId(),
                        purchaseOrder.getNotes(),
                        poRowMapper.getStatusString(purchaseOrder.getStatus()),
                        purchaseOrder.getCreatedAt(),
                        purchaseOrder.getCreatedBy(),
                        purchaseOrder.getUpdatedAt(),
                        purchaseOrder.getUpdatedBy(),
                        purchaseOrder.getReceivedAt() != null ? purchaseOrder.getReceivedAt() : "N/A",
                        purchaseOrder.getReceivedBy() != null ? purchaseOrder.getReceivedBy() : "N/A"
                };
                this.purchaseOrderTableModel.addRow(rowData);
            }
        } else {
            System.out.println("No purchase orders found.");
        }
    }

    private void showPurchaseOrderDetails(String poId) {
        JFrame parentDialogFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        this.fmController = new FMController();
        PurchaseOrder purchaseOrder = fmController.getFullPurchaseOrderDetailsById(poId);

        if (purchaseOrder == null) {
            JOptionPane.showMessageDialog(this, "Purchase Order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parentDialogFrame, "Purchase Order Details" + poId, true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(panelColour);
        dialog.setMinimumSize(new Dimension(700, 500));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(panelColour);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        headerPanel.setBackground(panelColour);
        headerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Purchase Order Header",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 16), textColour));

        addDetailRow(headerPanel, "PO ID:", purchaseOrder.getPoId());
        addDetailRow(headerPanel, "PR ID:", purchaseOrder.getPrId());
        addDetailRow(headerPanel, "Status:", String.valueOf(purchaseOrder.getStatus()));
        addDetailRow(headerPanel, "Notes:", purchaseOrder.getNotes());
        addDetailRow(headerPanel, "Created At:", purchaseOrder.getCreatedAt());
        addDetailRow(headerPanel, "Created By:", purchaseOrder.getCreatedBy());
        addDetailRow(headerPanel, "Updated At:", purchaseOrder.getUpdatedAt());
        addDetailRow(headerPanel, "Updated By:", purchaseOrder.getUpdatedBy());
        addDetailRow(headerPanel, "Received At:", purchaseOrder.getReceivedAt() != null ? purchaseOrder.getReceivedAt() : "N/A");
        addDetailRow(headerPanel, "Received By:", purchaseOrder.getReceivedBy() != null ? purchaseOrder.getReceivedBy() : "N/A");

        detailsPanel.add(headerPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        //Item Table

        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(panelColour);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Purchase Order Items",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textColour));

        String[] itemColumnNames = {"Item ID", "Item Code", "Item Name", "Qty", "Unit Price", "Total Price"};
        DefaultTableModel itemTableModel = new DefaultTableModel(itemColumnNames, 0);
        if (purchaseOrder.getItems() != null) {
            for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                double displayPrice = (double) item.getPrice();
                double displayTotalPrice = (double) item.getTotalPrice();

                itemTableModel.addRow(new Object[]{
                        item.getItemId(),
                        item.getItemCode(),
                        item.getItemName(),
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(displayPrice),
                        CURRENCY_FORMAT.format(displayTotalPrice)
                });
            }
        }

        JTable itemTable = new JTable(itemTableModel);
        itemTable.setFont(new Font("Arial", Font.PLAIN, 14));
        itemTable.setRowHeight(30);
        itemTable.setBackground(panelColour);
        itemTable.setForeground(textColour);
        itemTable.getTableHeader().setBackground(lightBlue);
        itemTable.getTableHeader().setForeground(textColour);

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setPreferredSize(new Dimension(550, 150));
        itemsPanel.add(scrollPane, BorderLayout.CENTER);

        detailsPanel.add(itemsPanel);

        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(parentDialogFrame);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String labelText, String value) {
        JLabel label = new JLabel(labelText);
        label.setForeground(textColour);

        JTextField field = new JTextField(value != null ? value : "N/A");
        field.setEditable(false);
        field.setBackground(panelColour);
        field.setForeground(textColour);
        field.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));

        panel.add(label);
        panel.add(field);
    }
}
