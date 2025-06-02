package view;

import controller.FileController;
import controller.ItemController; // Needed to get ItemEntryId
import controller.SMStockController;
import controller.StockController;
import model.PurchaseRequisitionItem; // For passing to AddPurchaseRequisitionForm
import util.table.GenericModelHelper;
import util.table.mappers.StockRowMapper;
import view.forms.AddPurchaseRequisitionForm; // To open the PR form

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor; // For ActionButtonPanel
import javax.swing.table.TableCellRenderer; // For ActionButtonPanel
import java.awt.*;
import java.awt.event.ActionListener; // For ActionButtonPanel
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList; // For passing items to PR form
import java.util.Arrays;
import java.util.List;

public class StockManagementView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE; //
    private final Color mediumBlue = UITheme.MEDIUM_BLUE; //
    private final Color lightBlue = UITheme.LIGHT_BLUE; //
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE; //
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE; //
    private final Color textWhite = UITheme.TEXT_WHITE; //
    private final Color errorRed = UITheme.ERROR_RED; //
    private final Color warningOrange = new Color(255, 165, 0);
    private final Color successGreen = UITheme.SUCCESS_GREEN; //

    private JTable stockTable;
    private DefaultTableModel stockTableModel;
    private SMStockController smStockController;
    private StockRowMapper stockRowMapper;
    private ItemController itemController; // Added to fetch item details

    private final String[] stockColumnNames = {
            "Stock ID", "Item Code", "Item Name", "Current Stock", "Min Stock", "Max Stock", "Status", "Last Update Date", "Actions" // Added "Actions"
    };

    public StockManagementView() {
        setTitle("Stock Management View");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        smStockController = new SMStockController();
        stockRowMapper = new StockRowMapper();
        itemController = new ItemController(); // Initialize ItemController

        add(createStockManagementPanel());

        pack();
        setMinimumSize(new Dimension(900, 500)); // Ensure it's reasonably sized
        setLocationRelativeTo(null);
    }

    public JPanel createStockManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Stock Overview & Requisition");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createStockTable(), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        refreshStockTable();
        return mainPanel;
    }

    public void refreshStockTable() {
        SwingUtilities.invokeLater(() -> {
            if (stockTableModel == null) {
                if (stockTable != null && stockTable.getModel() instanceof DefaultTableModel) {
                    stockTableModel = (DefaultTableModel) stockTable.getModel();
                } else {
                    System.err.println("StockManagementView.refreshStockTable(): stockTableModel is null and table not ready.");
                    return;
                }
            }
            if (smStockController == null) smStockController = new SMStockController(); //
            if (stockRowMapper == null) stockRowMapper = new StockRowMapper();


            stockTableModel.setRowCount(0);
            new FileController("data/stock_details.txt"); // Workaround for static FileController path
            List<String> rawStockData = smStockController.getAll(); //

            if (rawStockData != null && !rawStockData.isEmpty()) {
                for (String line : rawStockData) {
                    if (line == null || line.trim().isEmpty()) continue;
                    String[] fields = line.split(",");
                    Object[] rowData = stockRowMapper.mapFieldsToRow(fields, stockTableModel.getColumnCount());
                    if (rowData != null) {
                        stockTableModel.addRow(rowData);
                    }
                }
            } else {
                System.out.println("No stock data to display.");
            }
        });
    }

    private JScrollPane createStockTable() {
        new FileController("data/stock_details.txt"); // Workaround
        List<String> rawStockData = smStockController.getAll(); //

        this.stockTableModel = GenericModelHelper.createGenericTableModel(
                rawStockData,
                this.stockColumnNames,
                this.stockRowMapper,
                (row, column, totalColumns) -> column == totalColumns -1 // Only Actions column editable (for button)
        );

        this.stockTable = new JTable(this.stockTableModel);
        styleTable(this.stockTable);
        stockTable.setAutoCreateRowSorter(true);

        int colIndex = 0;
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(80);  // Stock ID
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Item Code
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(250); // Item Name
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Current Stock
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(80);  // Min Stock
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(80);  // Max Stock
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(100); // Status
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(120); // Last Update Date
        stockTable.getColumnModel().getColumn(colIndex++).setPreferredWidth(120); // Actions

        stockTable.getColumn("Status").setCellRenderer(new StatusCellRenderer());
        // Setup ActionButtonPanel for the "Actions" column
        StockActionButtonPanel actionPanel = new StockActionButtonPanel(this.stockTable);
        stockTable.getColumn("Actions").setCellRenderer(actionPanel);
        stockTable.getColumn("Actions").setCellEditor(actionPanel);


        stockTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int viewRow = stockTable.rowAtPoint(e.getPoint());
                    int viewColumn = stockTable.columnAtPoint(e.getPoint());
                    // Prevent popup if clicking on the Actions column
                    if (viewRow >= 0 && viewColumn != stockTable.getColumn("Actions").getModelIndex()) {
                        int modelRow = stockTable.convertRowIndexToModel(viewRow);
                        String stockId = (String) stockTableModel.getValueAt(modelRow, 0);
                        if (stockId != null && !stockId.trim().isEmpty()) {
                            showStockDetailsPopup(stockId);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.stockTable);
        scrollPane.getViewport().setBackground(darkBlue);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private void styleTable(JTable tableToStyle) {
        tableToStyle.setBackground(darkBlue);
        tableToStyle.setForeground(textWhite);
        tableToStyle.setGridColor(new Color(50, 60, 80));
        tableToStyle.setRowHeight(45);
        tableToStyle.setFont(new Font("Arial", Font.PLAIN, 13));
        tableToStyle.getTableHeader().setBackground(new Color(150, 165, 235));
        tableToStyle.getTableHeader().setForeground(textWhite);
        tableToStyle.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tableToStyle.setSelectionBackground(new Color(60, 70, 90));
        tableToStyle.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";
            setHorizontalAlignment(JLabel.CENTER);

            if ("In Stock".equalsIgnoreCase(status)) {
                cellComponent.setForeground(isSelected ? textWhite : successGreen);
            } else if ("Low Stock".equalsIgnoreCase(status)) {
                cellComponent.setForeground(isSelected ? textWhite : warningOrange);
            } else if ("Out of Stock".equalsIgnoreCase(status)) {
                cellComponent.setForeground(isSelected ? textWhite : errorRed);
            } else {
                cellComponent.setForeground(isSelected ? textWhite : table.getForeground());
            }
            cellComponent.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return cellComponent;
        }
    }

    // Inner class for Action Buttons Panel in Stock Table
    class StockActionButtonPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final JButton raisePRButton;
        private JTable containingTable;
        private int currentRow; // To store the current row being edited/rendered

        public StockActionButtonPanel(JTable table) {
            this.containingTable = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            raisePRButton = new JButton("Raise PR");
            styleActionButton(raisePRButton);
            raisePRButton.addActionListener(e -> {
                fireEditingStopped(); // Important for table to commit edits

                String itemCode = (String) containingTable.getValueAt(currentRow, 1); // Get Item Code
                String itemName = (String) containingTable.getValueAt(currentRow, 2); // Get Item Name
                Object currentStockObj = containingTable.getValueAt(currentRow, 3);
                Object maxStockObj = containingTable.getValueAt(currentRow, 5);

                // Fetch ItemEntryId (IMxxx) from item_details.txt using itemCode
                new FileController("data/item_details.txt"); // Ensure path for ItemController
                String itemDetailsData = itemController.getOneByItemCode(itemCode); //
                if (itemDetailsData == null) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(panel),
                            "Could not find item details for Item Code: " + itemCode, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String itemEntryId = itemDetailsData.split(",")[0]; // IMxxx
                String unitPriceStr = itemDetailsData.split(",")[4]; // Price from item_details

                // Suggested quantity: maxStock - currentStock, or default 1 if info missing/negative
                int suggestedQty = 1;
                try {
                    int currentStockVal = Integer.parseInt(currentStockObj.toString());
                    int maxStockVal = Integer.parseInt(maxStockObj.toString());
                    if (maxStockVal > currentStockVal) {
                        suggestedQty = maxStockVal - currentStockVal;
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Could not parse stock values for suggested PR quantity: " + ex.getMessage());
                }


                PurchaseRequisitionItem prItem = new PurchaseRequisitionItem(
                        null, // PR ID will be set by AddPurchaseRequisitionForm or its controller
                        itemEntryId,
                        itemCode,
                        itemName,
                        suggestedQty, // Initial quantity
                        (int) (Double.parseDouble(unitPriceStr) * 100) // Price in cents/smallest unit
                );
                List<PurchaseRequisitionItem> itemsForPR = new ArrayList<>();
                itemsForPR.add(prItem);

                // Open AddPurchaseRequisitionForm, passing the pre-filled item
                // Need a new constructor or method in AddPurchaseRequisitionForm
                AddPurchaseRequisitionForm prForm = new AddPurchaseRequisitionForm(
                        (JFrame) SwingUtilities.getWindowAncestor(StockManagementView.this), // Parent Frame
                        itemsForPR
                );
                prForm.setVisible(true);
            });
            panel.add(raisePRButton);
        }

        private void styleActionButton(JButton button) {
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setForeground(textWhite);
            button.setBackground(lightBlue);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setMargin(new Insets(2, 5, 2, 5));
        }

        private void updateButtonVisibility(int row) {
            if (containingTable == null || row < 0 || row >= containingTable.getRowCount()) {
                raisePRButton.setVisible(false);
                return;
            }
            Object statusObj = containingTable.getValueAt(row, 6); // Status column index
            String status = (statusObj != null) ? statusObj.toString() : "";
            boolean isLowOrOutOfStock = "Low Stock".equalsIgnoreCase(status) || "Out of Stock".equalsIgnoreCase(status);
            raisePRButton.setVisible(isLowOrOutOfStock);
            raisePRButton.setEnabled(isLowOrOutOfStock);
        }


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.currentRow = row; // Store current row for action listener
            updateButtonVisibility(row);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row; // Store current row for action listener
            updateButtonVisibility(row);
            panel.setBackground(table.getSelectionBackground()); // Match selection background
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return "";  } // Not important for buttons

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }

    private void showStockDetailsPopup(String stockId) {
        new FileController("data/stock_details.txt");
        String stockDataString = smStockController.getOneWithId(stockId);

        if (stockDataString == null || stockDataString.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not retrieve details for stock ID: " + stockId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] stockDetails = stockDataString.split(",");

        JDialog detailDialog = new JDialog(this, "Stock Item Details: " + stockId, true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.getContentPane().setBackground(mediumBlue);

        JPanel detailsContentPanel = new JPanel(new GridLayout(stockColumnNames.length -1, 2, 8, 8)); // Exclude "Actions"
        detailsContentPanel.setBackground(mediumBlue);
        detailsContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int i = 0; i < stockColumnNames.length -1; i++) { // Iterate up to stockColumnNames.length - 1 (excluding Actions)
            JLabel labelName = new JLabel(stockColumnNames[i] + ":");
            labelName.setForeground(textWhite);
            labelName.setFont(new Font("Arial", Font.BOLD, 13));
            detailsContentPanel.add(labelName);

            String value = (i < stockDetails.length) ? stockDetails[i].trim() : "N/A";
            JLabel labelValue = new JLabel(value);
            labelValue.setForeground(verylightBlue);
            labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
            detailsContentPanel.add(labelValue);
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

        detailDialog.pack();
        detailDialog.setMinimumSize(new Dimension(450, detailDialog.getPreferredSize().height));
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setVisible(true);
    }
}