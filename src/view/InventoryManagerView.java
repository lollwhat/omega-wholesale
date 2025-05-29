package view;

import controller.AuthController;
import controller.InventoryManagerController;
import controller.ItemController;
import controller.StockController;
import model.RoleName;
import model.User;
import util.table.mappers.ItemRowMapper;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

public class InventoryManagerView extends JFrame {
    private User loggedInUsername;
    private JPanel quickAccessPanel;
    private Color darkBlue = new Color(21, 31, 46);
    private Color mediumBlue = new Color(30, 41, 59);
    private Color lightBlue = new Color(96, 103, 205);
    private Color highlightBlue = new Color(78, 91, 249);
    private JButton lastClickedButton = null;

    public InventoryManagerView(User user) {
        this.loggedInUsername = user;

        RoleName roleName = RoleName.InventoryManager;
        setTitle(roleName.getRoleName("IM"));
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sideBar = createSideBar();

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(20, 25, 45));
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 5));

        JPanel header = createDashboardHeader();
        mainContent.add(header);

        mainContent.add(Box.createVerticalStrut(10));

        quickAccessPanel = createQuickAccess();
        quickAccessPanel.setMaximumSize(new Dimension(800, 600));
        quickAccessPanel.setPreferredSize(new Dimension(800, 600));
        mainContent.add(quickAccessPanel);

        JPanel contentArea = new JPanel();
        contentArea.setBackground(darkBlue);
        contentArea.setPreferredSize(new Dimension(800, 300));
        mainContent.add(contentArea);

        add(sideBar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // helper method for table
    public class TableHelper {
        public static JPanel createTable(Object[][] data, String[] columnNames) {
            // non-editable cells
            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(tableModel);

            // center-align cell data
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            // styling
            table.setForeground(Color.WHITE);
            table.setBackground(new Color(30, 41, 59));
            table.setFont(new Font("SansSerif", Font.PLAIN, 10));
            table.setRowHeight(30);

            // row selection behavior
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(false);
            table.setSelectionBackground(new Color(165, 180, 252));

            // disable column reordering
            table.getTableHeader().setReorderingAllowed(false);

            table.setShowGrid(false);

            // table header
            JTableHeader tableHeader = table.getTableHeader();
            tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
            tableHeader.setBackground(new Color(165, 180, 252));
            tableHeader.setForeground(Color.WHITE);
            tableHeader.setPreferredSize(new Dimension(tableHeader.getPreferredSize().width, 35));

            // wrap in a JScrollPane for scrolling support
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(new Color(30, 41, 59));

            // create a panel to hold the table
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            return tablePanel;
        }

        public static JPanel createTableWithButton(JTable stockTable, JTable purchaseOrderTable, Object[][] data, String[] columnNames) {
            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == getColumnCount() - 1;
                }
            };

            JTable table = new JTable(tableModel);
            styleTable(table);

            try {
                TableColumn actionColumn = table.getColumn("Action");
                actionColumn.setCellRenderer(new ButtonRenderer());
                actionColumn.setCellEditor(new ButtonEditor(table, new StockController(), stockTable,
                        new InventoryManagerController(new ItemController(), new StockController())));
            } catch (IllegalArgumentException ex) {
                System.err.println("Error: 'Action' column not found. Ensure columnNames include 'Action'.");
            }

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(new Color(30, 41, 59));

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            return tablePanel;
        }

        private static void styleTable(JTable table) {
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            table.setForeground(Color.WHITE);
            table.setBackground(new Color(30, 41, 59));
            table.setFont(new Font("SansSerif", Font.PLAIN, 10));
            table.setRowHeight(30);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setSelectionBackground(new Color(165, 180, 252));
            table.setShowGrid(false);

            JTableHeader header = table.getTableHeader();
            header.setFont(new Font("SansSerif", Font.BOLD, 12));
            header.setBackground(new Color(165, 180, 252));
            header.setForeground(Color.WHITE);
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        }


        static class ButtonRenderer extends JPanel implements TableCellRenderer {
            private final JButton receiveButton = new JButton("Receive");
            private final JButton removeButton = new JButton("Remove");

            public ButtonRenderer() {
                setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
                setOpaque(true);

                receiveButton.setOpaque(true);
                receiveButton.setBorderPainted(false);
                receiveButton.setFocusPainted(false);
                receiveButton.setBackground(new Color(59, 130, 246));
                receiveButton.setForeground(Color.WHITE);

                removeButton.setOpaque(true);
                removeButton.setBorderPainted(false);
                removeButton.setFocusPainted(false);
                removeButton.setBackground(new Color(220, 38, 38));
                removeButton.setForeground(Color.WHITE);

                add(receiveButton);
                add(removeButton);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String status = table.getValueAt(row, table.getColumn("Status").getModelIndex()).toString();

                receiveButton.setVisible(!"Received".equalsIgnoreCase(status));
                removeButton.setVisible("Received".equalsIgnoreCase(status));

                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return this;
            }
        }

        static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            private final JButton receiveButton = new JButton("Receive");
            private final JButton removeButton = new JButton("Remove");
            private JTable table;
            private int row;

            private final StockController stockController;
            private final JTable stockTable;
            private final InventoryManagerController controller;

            public ButtonEditor(JTable table, StockController stockController, JTable stockTable, InventoryManagerController controller) {
                this.table = table;
                this.stockController = stockController;
                this.stockTable = stockTable;
                this.controller = controller;

                receiveButton.setOpaque(true);
                receiveButton.setBorderPainted(false);
                receiveButton.setFocusPainted(false);
                receiveButton.setBackground(new Color(59, 130, 246));
                receiveButton.setForeground(Color.WHITE);
                receiveButton.addActionListener(e -> handleReceiveAction());

                removeButton.setOpaque(true);
                removeButton.setBorderPainted(false);
                removeButton.setFocusPainted(false);
                removeButton.setBackground(new Color(220, 38, 38));
                removeButton.setForeground(Color.WHITE);
                removeButton.addActionListener(e -> handleRemoveAction());

                panel.add(receiveButton);
                panel.add(removeButton);
            }

            private void handleReceiveAction() {
                String poId = table.getValueAt(row, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(null, "Mark purchase order " + poId + " as received?", "Confirm Action", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = stockController.markPurchaseOrderAsReceived(poId);
                    if (success) {
                        table.setValueAt("Received", row, table.getColumn("Status").getModelIndex());
                        fireEditingStopped();

                        refreshStockTable(stockTable, controller);
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to mark purchase order as received.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            private void handleRemoveAction() {
                try {
                    String poId = table.getValueAt(row, 0).toString();
                    int confirm = JOptionPane.showConfirmDialog(null, "Remove purchase order " + poId + " from the list?", "Confirm Action", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.removeRow(row);
                        fireEditingStopped();
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.err.println("Array index out of bounds: " + ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("An error occurred: " + ex.getMessage());
                }
            }

            private void refreshStockTable(JTable stockTable, InventoryManagerController controller) {
                SwingUtilities.invokeLater(() -> {
                    // load stock data
                    Object[][] updatedStockData = controller.loadStocks();

                    DefaultTableModel model = (DefaultTableModel) stockTable.getModel();
                    model.setRowCount(0);

                    if (updatedStockData != null) {
                        for (Object[] row : updatedStockData) {
                            model.addRow(row);
                        }
                    }

                    model.fireTableDataChanged();
                    stockTable.repaint();
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.row = row;
                String status = table.getValueAt(row, table.getColumn("Status").getModelIndex()).toString();

                receiveButton.setVisible(!"Received".equalsIgnoreCase(status));
                removeButton.setVisible("Received".equalsIgnoreCase(status));
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return null;
            }
        }

        public static class StockTableRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String status = table.getValueAt(row, table.getColumn("Status").getModelIndex()).toString();
                if ("Out of Stock".equals(status)) {
                    cell.setBackground(Color.RED);
                    cell.setForeground(Color.WHITE);
                } else if ("Low Stock".equals(status)) {
                    cell.setBackground(Color.ORANGE);
                    cell.setForeground(Color.BLACK);
                } else {
                    cell.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    cell.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }

                return cell;
            }
        }
    }

    // helper method for display "no data available" message
    public class UIHelper {
        private static final Color DARK_BLUE = new Color(21, 31, 46);

        public static JPanel createDisplayNoDataAvailableMessage(String message) {
            JPanel centeredPanel = new JPanel(new GridBagLayout());
            centeredPanel.setBackground(DARK_BLUE);

            JLabel noDataLabel = new JLabel(message);
            noDataLabel.setForeground(Color.WHITE);
            noDataLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);

            centeredPanel.add(noDataLabel);
            return centeredPanel;
        }
    }

    // helper method for buttons
    private JButton createButton(String text, ActionListener actionListener) {
        JButton sideBarButton = new JButton(text);
        sideBarButton.setForeground(Color.WHITE);
        sideBarButton.setBackground(mediumBlue);
        sideBarButton.setOpaque(true);
        sideBarButton.setFocusPainted(false);
        sideBarButton.setBorderPainted(false);
        sideBarButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sideBarButton.setHorizontalAlignment(SwingConstants.LEFT);
        sideBarButton.setPreferredSize(new Dimension(170, 30));
        sideBarButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        sideBarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sideBarButton.addActionListener(actionListener);

        sideBarButton.addActionListener(e -> {
            if (lastClickedButton != null && lastClickedButton != sideBarButton) {
                lastClickedButton.setBackground(mediumBlue); // reset the previous button
            }

            sideBarButton.setBackground(lightBlue);

            lastClickedButton = sideBarButton;
            actionListener.actionPerformed(e);
        });

        sideBarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                sideBarButton.setBackground(lightBlue); // hover background

                if (lastClickedButton != sideBarButton) {
                    sideBarButton.setBackground(lightBlue);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
//                sideBarButton.setBackground(mediumBlue); // default background

                if (lastClickedButton != sideBarButton) {
                    sideBarButton.setBackground(mediumBlue);
                }
            }
        });

        return sideBarButton;
    }

    // helper method for date
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        return dateFormat.format(new Date());
    }

    // reusable method
    private void handleViewButton(String noDataMessage, Object[][] data, String[] columnNames) {
        quickAccessPanel.removeAll();

        if (data == null || data.length == 0) {
            JPanel noDataPanel = UIHelper.createDisplayNoDataAvailableMessage(noDataMessage);
            quickAccessPanel.add(noDataPanel, BorderLayout.CENTER);
        } else {
            JPanel tablePanel = TableHelper.createTable(data, columnNames);
            quickAccessPanel.add(tablePanel, BorderLayout.CENTER);
        }

        quickAccessPanel.revalidate();
        quickAccessPanel.repaint();
    }

    // helper method for stock
    public class StockHelper {
        private final StockController stockController;

        public StockHelper(StockController stockController) {
            this.stockController = stockController;
        }

        public int[] calculateStockCounts() {
            List<String> stockData = stockController.getAllStocks();
            int inStockCount = 0;
            int lowStockCount = 0;
            int outOfStockCount = 0;

            if (stockData != null) {
                for (String stock : stockData) {
                    if (stock == null || stock.trim().isEmpty()) continue;

                    String[] details = stock.split(",");
                    if (details.length > 5) {
                        String status = details[5].trim();
                        if ("In Stock".equalsIgnoreCase(status)) {
                            inStockCount++;
                        } else if ("Low Stock".equalsIgnoreCase(status)) {
                            lowStockCount++;
                        } else if ("Out of Stock".equalsIgnoreCase(status)) {
                            outOfStockCount++;
                        }
                    } else {
                        System.err.println("Malformed stock data: " + Arrays.toString(details));
                    }
                }
            }

            return new int[]{inStockCount, lowStockCount, outOfStockCount};
        }
    }

    // sidebar
    private JPanel createSideBar() {
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(mediumBlue);
        sideBar.setPreferredSize(new Dimension(225, getHeight()));
        sideBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel sideBarLabel = new JLabel("OWSB System");
        sideBarLabel.setForeground(highlightBlue);
        sideBarLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        sideBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // display user info
        JLabel userLabel = new JLabel("User: " + loggedInUsername.getUsername());
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userLabel.setForeground(Color.LIGHT_GRAY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("Role: " + RoleName.InventoryManager);
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roleLabel.setForeground(Color.LIGHT_GRAY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.Y_AXIS));
        buttonWrapper.setOpaque(false);

        JButton viewItemsButton = createButton("View Items", e -> {
//            InventoryManagerController controller = new InventoryManagerController(new ItemController(), new StockController());
//            Object[][] items = controller.loadItems();
//            if (items == null || items.length == 0) {
//                quickAccessPanel.removeAll();
//
//                JPanel centeredPanel = new JPanel(new GridBagLayout());
//                centeredPanel.setBackground(quickAccessPanel.getBackground());
//
//                JLabel noItemsLabel = new JLabel("No items available at the moment.");
//                noItemsLabel.setForeground(Color.WHITE);
//                noItemsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
//                noItemsLabel.setHorizontalAlignment(SwingConstants.CENTER);
//
//                centeredPanel.add(noItemsLabel);
//                quickAccessPanel.add(centeredPanel, BorderLayout.CENTER);
//                quickAccessPanel.revalidate();
//                quickAccessPanel.repaint();
//                return;
//            }
//
//            String[] columns = controller.getItemTableColumns();
//
//            quickAccessPanel.removeAll();
//
//            JPanel tablePanel = TableHelper.createTable(items, columns);
//            quickAccessPanel.add(tablePanel, BorderLayout.CENTER);
//
//            quickAccessPanel.revalidate();
//            quickAccessPanel.repaint();

//            Object[][] items = controller.loadItems();
//            String[] columns = controller.getItemTableColumns();
//
//            handleViewButton("No items available at the moment."

//            Object[][] items = controller.loadItems();

            List<Object[]> items = new ArrayList<>();
            try {
                List<String> lines = Files.readAllLines(Paths.get("data/item_details.txt"));
                for (String line : lines) {
                    String[] columns = line.split(",");
                    if (columns.length >= 10) {
                        items.add(new Object[]{
                                columns[0], // item ID
                                columns[1], // item code
                                columns[2], // item name
                                columns[3], // item unit
                                columns[4], // unit price
                                columns[5], // supplier ID
                                columns[6],  // created at
                                columns[8],  // created by
                                columns[7],  // updated at
                                columns[9]  // updated by
                        });
                    } else {
                        System.err.println("Malformed line in purchase_order_item.txt: " + line);
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error reading items: " + ex.getMessage());
            }

            Object[][] item = items.toArray(new Object[0][]);

            String[] columns = { // Made this an instance variable
                    "Item Entry ID", "Item Code", "Name", "Unit", "Price", "Supplier ID",
                    "Created At", "Created By", "Updated At", "Updated By"
            };

            handleViewButton("No items available at the moment.", item, columns);
        });

        JButton inventoryManagementButton = createButton("Inventory Management", e -> {
            InventoryManagerController controller = new InventoryManagerController(new ItemController(), new StockController());
            StockController stockController = controller.getStockController();
            StockHelper stockHelper = new StockHelper(stockController);

            Object[][] stocks = controller.loadStocks();
            String[] stockColumns = controller.getStockTableColumns();

            Object[][] purchaseOrdersWithItems = controller.loadApprovedPurchaseOrdersWithItems();
            String[] purchaseOrderWithItemsTableColumns = controller.getPurchaseOrderWithItemsTableColumns();

            DefaultTableModel stockTableModel = new DefaultTableModel(stocks, stockColumns);
            JTable stockTable = new JTable(stockTableModel);
            TableHelper.styleTable(stockTable);

            if (stocks != null && stocks.length > 0) {
                int statusColumnIndex = 5;

                // sort the data to prioritize critical statuses
//                Arrays.sort(stocks, (row1, row2) -> {
//                    String status1 = row1[statusColumnIndex].toString().trim();
//                    String status2 = row2[statusColumnIndex].toString().trim();
//
//                    int priority1 = "Out of Stock".equalsIgnoreCase(status1) ? 1
//                            : "Low Stock".equalsIgnoreCase(status1) ? 2
//                            : 3;
//                    int priority2 = "Out of Stock".equalsIgnoreCase(status2) ? 1
//                            : "Low Stock".equalsIgnoreCase(status2) ? 2
//                            : 3;
//
//                    return Integer.compare(priority1, priority2);
//                });

                stockTable.getColumnModel().getColumn(statusColumnIndex).setCellRenderer(new TableHelper.StockTableRowRenderer());
                for (int i = 0; i < stockTable.getColumnCount(); i++) {
                    stockTable.getColumnModel().getColumn(i).setCellRenderer(new TableHelper.StockTableRowRenderer());
                }
            }

            DefaultTableModel poTableModel = new DefaultTableModel(purchaseOrdersWithItems, purchaseOrderWithItemsTableColumns);
            JTable purchaseOrderTable = new JTable(poTableModel);

            JPanel stockTablePanel;
            if (stocks == null || stocks.length == 0) {
                stockTablePanel = UIHelper.createDisplayNoDataAvailableMessage("No stock data available.");
            } else {
                JScrollPane stockTableScrollPane = new JScrollPane(stockTable);
                stockTablePanel = new JPanel(new BorderLayout());
                stockTablePanel.add(stockTableScrollPane, BorderLayout.CENTER);
            }

            JPanel stockTableWrapper = new JPanel(new BorderLayout());
            stockTableWrapper.setBackground(quickAccessPanel.getBackground());

            int[] stockCounts = stockHelper.calculateStockCounts();
            JLabel inStockLabel = new JLabel("In Stock: " + stockCounts[0]);
            JLabel lowStockLabel = new JLabel("Low Stock: " + stockCounts[1]);
            JLabel outOfStockLabel = new JLabel("Out of Stock: " + stockCounts[2]);

            inStockLabel.setForeground(Color.GREEN);
            inStockLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            lowStockLabel.setForeground(Color.ORANGE);
            lowStockLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            outOfStockLabel.setForeground(Color.RED);
            outOfStockLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            JPanel alertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            alertPanel.setBackground(quickAccessPanel.getBackground());
            alertPanel.add(inStockLabel);
            alertPanel.add(lowStockLabel);
            alertPanel.add(outOfStockLabel);

            stockTableWrapper.add(alertPanel, BorderLayout.NORTH);
            stockTableWrapper.add(stockTablePanel, BorderLayout.CENTER);

            JPanel purchaseOrderTablePanel;
            if (purchaseOrdersWithItems == null || purchaseOrdersWithItems.length == 0) {
                purchaseOrderTablePanel = UIHelper.createDisplayNoDataAvailableMessage("No purchase orders available.");
            } else {
                purchaseOrderTablePanel = TableHelper.createTableWithButton(stockTable, purchaseOrderTable, purchaseOrdersWithItems, purchaseOrderWithItemsTableColumns);
            }

            JButton generateStockReportButton = new JButton("Generate Stock Report");
            generateStockReportButton.setOpaque(true);
            generateStockReportButton.setBackground(new Color(78, 91, 249));
            generateStockReportButton.setForeground(Color.WHITE);
            generateStockReportButton.setFocusPainted(false);
            generateStockReportButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            generateStockReportButton.addActionListener(event -> {
                String savePath = System.getProperty("user.home") + "/Downloads/Stock_Report.csv";

                try {
                    stockController.generateStockReport(savePath);
                    JOptionPane.showMessageDialog(
                            null,
                            "Report saved to: " + savePath,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Error generating report: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            quickAccessPanel.removeAll();
            quickAccessPanel.setLayout(new BorderLayout(10, 10));

            JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 10, 10));
            tablesPanel.setBackground(quickAccessPanel.getBackground());

//            JPanel stockTableWrapper = new JPanel(new BorderLayout());
            stockTableWrapper.setBackground(quickAccessPanel.getBackground());
            stockTableWrapper.add(stockTablePanel, BorderLayout.CENTER);

            JPanel purchaseOrderWrapper = new JPanel(new BorderLayout());
            purchaseOrderWrapper.setBackground(quickAccessPanel.getBackground());
            purchaseOrderWrapper.add(purchaseOrderTablePanel, BorderLayout.CENTER);

            tablesPanel.add(stockTableWrapper);
            tablesPanel.add(purchaseOrderWrapper);

            quickAccessPanel.add(tablesPanel, BorderLayout.CENTER);
            quickAccessPanel.add(generateStockReportButton, BorderLayout.SOUTH);

            quickAccessPanel.revalidate();
            quickAccessPanel.repaint();
        });

        JButton viewPurchaseOrdersButton = createButton("View Purchase Orders", e -> {
            InventoryManagerController controller = new InventoryManagerController(new ItemController(), new StockController());

            Object[][] purchaseOrders = controller.loadPurchaseOrders();
            String[] columns = controller.getPurchaseOrderTableColumns();

            handleViewButton("No purchase orders available at the moment.", purchaseOrders, columns);
        });

        buttonWrapper.add(viewItemsButton);
        buttonWrapper.add(Box.createVerticalStrut(20));
        buttonWrapper.add(inventoryManagementButton);
        buttonWrapper.add(Box.createVerticalStrut(20));
        buttonWrapper.add(viewPurchaseOrdersButton);

        // logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(lightBlue);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(true);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(150, 40));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginView(new AuthController());
        });

        // add components
        sideBar.add(Box.createVerticalStrut(20));
        sideBar.add(sideBarLabel);
        sideBar.add(Box.createVerticalStrut(20));
        sideBar.add(userLabel);
        sideBar.add(Box.createVerticalStrut(10));
        sideBar.add(roleLabel);
        sideBar.add(Box.createVerticalStrut(20));
        sideBar.add(buttonWrapper);
        sideBar.add(Box.createVerticalGlue());
        sideBar.add(logoutButton);

        return sideBar;
    }

    // dashboard header
    private JPanel createDashboardHeader() {
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(mediumBlue);
        headerContainer.setPreferredSize(new Dimension(800, 50));

        JLabel dashboardHeaderLabel = new JLabel("Dashboard");
        dashboardHeaderLabel.setForeground(highlightBlue);
        dashboardHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        dashboardHeaderLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel currentDateLabel = new JLabel(getCurrentDate());
        currentDateLabel.setForeground(Color.WHITE);
        currentDateLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        currentDateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        currentDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // add components
        headerContainer.add(dashboardHeaderLabel, BorderLayout.WEST);
        headerContainer.add(currentDateLabel, BorderLayout.EAST);

        return headerContainer;
    }

    // quick access panel
    private JPanel createQuickAccess() {
        JPanel quickAccessPanel = new JPanel();
        quickAccessPanel.setBackground(mediumBlue);
        quickAccessPanel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        quickAccessPanel.setLayout(new BoxLayout(quickAccessPanel, BoxLayout.Y_AXIS));
        quickAccessPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel welcomeMessage = new JLabel("Welcome to Omega Wholesale Business System");
        welcomeMessage.setForeground(Color.WHITE);
        welcomeMessage.setFont(new Font("SansSerif", Font.BOLD, 14));
        welcomeMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel inventoryManagerLabel = new JLabel("Hello, " + loggedInUsername.getUsername() + "! You are logged in as Inventory Manager.");
        inventoryManagerLabel.setForeground(Color.WHITE);
        inventoryManagerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inventoryManagerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Please select an option from the menu to get started.");
        instructionLabel.setForeground(Color.WHITE);
        inventoryManagerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel quickAccessLabel = new JLabel("Quick Access");
        quickAccessLabel.setForeground(highlightBlue);
        quickAccessLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        quickAccessLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setBackground(darkBlue);
        buttonWrapper.setLayout(new GridLayout(3, 1, 10, 10));

        JButton viewItemsButton = createButton("View Items", e -> {
//            InventoryManagerController controller = new InventoryManagerController(new ItemController(), new StockController());
//
//            Object[][] items = controller.loadItems();
//            String[] columns = controller.getItemTableColumns();
//
//            handleViewButton("No items available at the moment.", items, columns);
            List<Object[]> items = new ArrayList<>();
            try {
                List<String> lines = Files.readAllLines(Paths.get("data/item_details.txt"));
                for (String line : lines) {
                    String[] columns = line.split(",");
                    if (columns.length >= 10) {
                        items.add(new Object[]{
                                columns[0], // item ID
                                columns[1], // item code
                                columns[2], // item name
                                columns[3], // item unit
                                columns[4], // unit price
                                columns[5], // supplier ID
                                columns[6],  // created at
                                columns[8],  // created by
                                columns[7],  // updated at
                                columns[9]  // updated by
                        });
                    } else {
                        System.err.println("Malformed line in purchase_order_item.txt: " + line);
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error reading items: " + ex.getMessage());
            }

            Object[][] item = items.toArray(new Object[0][]);

            String[] columns = { // Made this an instance variable
                    "Item Entry ID", "Item Code", "Name", "Unit", "Price", "Supplier ID",
                    "Created At", "Created By", "Updated At", "Updated By"
            };

            handleViewButton("No items available at the moment.", item, columns);
        });
        viewItemsButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton inventoryManagementButton = createButton("Inventory Management", e -> {
            InventoryManagerController controller = new InventoryManagerController(new ItemController(), new StockController());
            StockController stockController = controller.getStockController();
            StockHelper stockHelper = new StockHelper(stockController);

            Object[][] stocks = controller.loadStocks();
            String[] stockColumns = controller.getStockTableColumns();

            Object[][] purchaseOrdersWithItems = controller.loadApprovedPurchaseOrdersWithItems();
            String[] purchaseOrderWithItemsTableColumns = controller.getPurchaseOrderWithItemsTableColumns();

            DefaultTableModel stockTableModel = new DefaultTableModel(stocks, stockColumns);
            JTable stockTable = new JTable(stockTableModel);
            TableHelper.styleTable(stockTable);

            if (stocks != null && stocks.length > 0) {
                int statusColumnIndex = 5;

                // sort the data to prioritize critical statuses
//                Arrays.sort(stocks, (row1, row2) -> {
//                    String status1 = row1[statusColumnIndex].toString();
//                    String status2 = row2[statusColumnIndex].toString();
//
//                    int priority1 = "Out of Stock".equals(status1) ? 1 : "Low Stock".equals(status1) ? 2 : 3;
//                    int priority2 = "Out of Stock".equals(status2) ? 1 : "Low Stock".equals(status2) ? 2 : 3;
//
//                    return Integer.compare(priority1, priority2);
//                });

                stockTable.getColumnModel().getColumn(statusColumnIndex).setCellRenderer(new TableHelper.StockTableRowRenderer());
                for (int i = 0; i < stockTable.getColumnCount(); i++) {
                    stockTable.getColumnModel().getColumn(i).setCellRenderer(new TableHelper.StockTableRowRenderer());
                }
            }

            DefaultTableModel poTableModel = new DefaultTableModel(purchaseOrdersWithItems, purchaseOrderWithItemsTableColumns);
            JTable purchaseOrderTable = new JTable(poTableModel);

            JPanel stockTablePanel;
            if (stocks == null || stocks.length == 0) {
                stockTablePanel = UIHelper.createDisplayNoDataAvailableMessage("No stock data available.");
            } else {
                JScrollPane stockTableScrollPane = new JScrollPane(stockTable);
                stockTablePanel = new JPanel(new BorderLayout());
                stockTablePanel.add(stockTableScrollPane, BorderLayout.CENTER);
            }

            JPanel stockTableWrapper = new JPanel(new BorderLayout());
            stockTableWrapper.setBackground(quickAccessPanel.getBackground());

            int[] stockCounts = stockHelper.calculateStockCounts();
            JLabel inStockLabel = new JLabel("In Stock: " + stockCounts[0]);
            JLabel lowStockLabel = new JLabel("Low Stock: " + stockCounts[1]);
            JLabel outOfStockLabel = new JLabel("Out of Stock: " + stockCounts[2]);

            inStockLabel.setForeground(Color.GREEN);
            inStockLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            lowStockLabel.setForeground(Color.ORANGE);
            lowStockLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            outOfStockLabel.setForeground(Color.RED);
            outOfStockLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            JPanel alertPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            alertPanel.setBackground(quickAccessPanel.getBackground());
            alertPanel.add(inStockLabel);
            alertPanel.add(lowStockLabel);
            alertPanel.add(outOfStockLabel);

            stockTableWrapper.add(alertPanel, BorderLayout.NORTH);
            stockTableWrapper.add(stockTablePanel, BorderLayout.CENTER);

            JPanel purchaseOrderTablePanel;
            if (purchaseOrdersWithItems == null || purchaseOrdersWithItems.length == 0) {
                purchaseOrderTablePanel = UIHelper.createDisplayNoDataAvailableMessage("No purchase orders available.");
            } else {
                purchaseOrderTablePanel = TableHelper.createTableWithButton(stockTable, purchaseOrderTable, purchaseOrdersWithItems, purchaseOrderWithItemsTableColumns);
            }

            JButton generateStockReportButton = new JButton("Generate Stock Report");
            generateStockReportButton.setOpaque(true);
            generateStockReportButton.setBackground(new Color(78, 91, 249));
            generateStockReportButton.setForeground(Color.WHITE);
            generateStockReportButton.setFocusPainted(false);
            generateStockReportButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            generateStockReportButton.addActionListener(event -> {
                String savePath = System.getProperty("user.home") + "/Downloads/Stock_Report.csv";

                try {
                    stockController.generateStockReport(savePath);
                    JOptionPane.showMessageDialog(
                            null,
                            "Report saved to: " + savePath,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Error generating report: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            quickAccessPanel.removeAll();
            quickAccessPanel.setLayout(new BorderLayout(10, 10));

            JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 10, 10));
            tablesPanel.setBackground(quickAccessPanel.getBackground());

//            JPanel stockTableWrapper = new JPanel(new BorderLayout());
            stockTableWrapper.setBackground(quickAccessPanel.getBackground());
            stockTableWrapper.add(stockTablePanel, BorderLayout.CENTER);

            JPanel purchaseOrderWrapper = new JPanel(new BorderLayout());
            purchaseOrderWrapper.setBackground(quickAccessPanel.getBackground());
            purchaseOrderWrapper.add(purchaseOrderTablePanel, BorderLayout.CENTER);

            tablesPanel.add(stockTableWrapper);
            tablesPanel.add(purchaseOrderWrapper);

            quickAccessPanel.add(tablesPanel, BorderLayout.CENTER);
            quickAccessPanel.add(generateStockReportButton, BorderLayout.SOUTH);

            quickAccessPanel.revalidate();
            quickAccessPanel.repaint();
        });
        inventoryManagementButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton viewPurchaseOrdersButton = createButton("View Purchase Orders", e -> {
            InventoryManagerController controller = new InventoryManagerController(new ItemController(), new StockController());

            Object[][] purchaseOrders = controller.loadPurchaseOrders();
            String[] columns = controller.getPurchaseOrderTableColumns();

            handleViewButton("No purchase orders available at the moment.", purchaseOrders, columns);
        });
        viewPurchaseOrdersButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // add components
        quickAccessPanel.add(welcomeMessage);
        quickAccessLabel.add(Box.createVerticalStrut(10));
        quickAccessPanel.add(inventoryManagerLabel);
        quickAccessLabel.add(Box.createVerticalStrut(10));
        quickAccessPanel.add(instructionLabel);
        quickAccessPanel.add(Box.createVerticalStrut(20));
        quickAccessPanel.add(quickAccessLabel);
        quickAccessPanel.add(Box.createVerticalStrut(20));

        buttonWrapper.add(viewItemsButton);
        buttonWrapper.add(inventoryManagementButton);
        buttonWrapper.add(viewPurchaseOrdersButton);

        quickAccessPanel.add(buttonWrapper, BorderLayout.CENTER);

        return quickAccessPanel;
    }

    // testing the View
    public static void main(String[] args) {
        String userID = "WW01";
        String username = "inventorytest";
        String password = "inventory123";
        String firstName = "John";
        String lastName = "Doe";
        String email = "johndoe@email.com";
        String status = "Active";
        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        User testView = new User(userID, username, password, firstName, lastName, email, status, currentTimeStamp, currentTimeStamp) {
            @Override
            public String getUsername() {
                return "testView";
            }
        };
        new InventoryManagerView(testView);
    }
}