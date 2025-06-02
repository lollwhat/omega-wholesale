package view;

import controller.FMController;
import model.PurchaseOrder;
import model.PurchaseOrderItem;
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.text.DecimalFormat;
import java.util.List;

public class FinancePurchaseRequisitionView extends JFrame {
    private Color background = new Color(21, 31, 46);
    private Color panelColour = new Color(30, 41, 59);
    private Color lightBlue = new Color(96, 103, 205);
    private Color verylightBlue = new Color(165, 180, 252);
    private Color labelColour = new Color(78, 91, 249);
    private Color textColour = new Color(255, 255, 255);

    private JTable requisitionTable;
    private DefaultTableModel requisitionTableModel;
    private String[] columnNames = {"PR ID", "Notes", "Supplier", "Status", "Created At", "Created By", "Updated At", "Updated By"};
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private FMController fmController;

    public FinancePurchaseRequisitionView() {
        this.fmController = new FMController();

        setTitle("View Purchase Requisition");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(createRequisitionPanel(), BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    public JPanel createRequisitionPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(panelColour);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(background);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel requisitionTitle = new JLabel("View Purchase Requisition");
        requisitionTitle.setForeground(labelColour);
        requisitionTitle.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(requisitionTitle, BorderLayout.WEST);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(background);
        tablePanel.add(createTable(), BorderLayout.CENTER);


//        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        return mainPanel;
    }


    private JScrollPane createTable() {
        this.requisitionTableModel = new DefaultTableModel(this.columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table non-editable
            }
        };

        requisitionTable = new JTable(requisitionTableModel);
        requisitionTable.setBackground(panelColour);
        requisitionTable.setForeground(textColour);
        requisitionTable.setSelectionBackground(lightBlue);
        requisitionTable.setSelectionForeground(textColour);
        requisitionTable.setRowHeight(40);
        requisitionTable.getTableHeader().setBackground(verylightBlue);
        requisitionTable.getTableHeader().setForeground(textColour);
        requisitionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));


        requisitionTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        requisitionTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        requisitionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        requisitionTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        requisitionTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        requisitionTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        requisitionTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        requisitionTable.getColumnModel().getColumn(7).setPreferredWidth(100);

        requisitionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = requisitionTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String prId = requisitionTableModel.getValueAt(selectedRow, 0).toString();
                        if (prId != null && !prId.trim().isEmpty()) {
                            showPurchaseRequisitionDetails(prId);
                        }
                    }
                }
            }
        });

        loadRequisitions();

        JScrollPane scrollPane = new JScrollPane(requisitionTable);
        scrollPane.getViewport().setBackground(panelColour);

        return scrollPane;
    }

    private void loadRequisitions() {
        if(this.requisitionTableModel == null) {
            System.out.println("Requisition table model is not initialized.");
            return;
        }
        if(this.fmController == null) {
            System.out.println("FMController is not initialized.");
            return;
        }
        this.requisitionTableModel.setRowCount(0); // Clear existing rows

        List<PurchaseRequisition> prHeader = this.fmController.getAllPurchaseRequisitionsHeaders();
        System.out.println("Requisitions loaded: " + prHeader.size());

        if (!prHeader.isEmpty()) {
            for(PurchaseRequisition requisition : prHeader) {
                Object[] rowData = {
                        requisition.getPrId(),
                        requisition.getNotes(),
                        requisition.getStatus(),
                        requisition.getCreatedAt(),
                        requisition.getCreatedBy(),
                        requisition.getUpdatedAt(),
                        requisition.getUpdatedBy()
                };
                this.requisitionTableModel.addRow(rowData);
            }
        } else if (prHeader == null) {
            System.out.println("No purchase requisitions found or error retrieving data.");
        } else {
            System.out.println("Purchase requisitions list is empty.");
        }
    }

    private void showPurchaseRequisitionDetails(String prId) {
        JFrame parentDialogFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        this.fmController = new FMController();
        PurchaseRequisition purchaseRequisition = fmController.getFullPurchaseRequisitionDetailsById(prId);

        if (purchaseRequisition == null) {
            JOptionPane.showMessageDialog(this, "Purchase Order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parentDialogFrame, "Purchase Requisition Details" + prId, true);
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

        addDetailRow(headerPanel, "PR ID:", purchaseRequisition.getPrId());
        addDetailRow(headerPanel, "Status:", String.valueOf(purchaseRequisition.getStatus()));
        addDetailRow(headerPanel, "Notes:", purchaseRequisition.getNotes());
        addDetailRow(headerPanel, "Created At:", purchaseRequisition.getCreatedAt());
        addDetailRow(headerPanel, "Created By:", purchaseRequisition.getCreatedBy());
        addDetailRow(headerPanel, "Updated At:", purchaseRequisition.getUpdatedAt());
        addDetailRow(headerPanel, "Updated By:", purchaseRequisition.getUpdatedBy());

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
        if (purchaseRequisition.getItems() != null) {
            for (PurchaseRequisitionItem item : purchaseRequisition.getItems()) {
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
