package view;

import controller.AuthController;
import controller.InventoryManagerController;
import controller.ItemController;
import model.RoleName;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
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
            InventoryManagerController controller = new InventoryManagerController(
              new ItemController()
            );

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

            Object[][] items = controller.loadItems();
            String[] columns = controller.getItemTableColumns();

            handleViewButton("No items available at the moment.", items, columns);
        });

        // TODO: inventory management
        JButton inventoryManagementButton = createButton("Inventory Management", e -> {
        });

        // TODO: view POs
        JButton viewPurchaseOrdersButton = createButton("View Purchase Orders", e -> {
            InventoryManagerController controller = new InventoryManagerController(new ItemController());

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
            InventoryManagerController controller = new InventoryManagerController(new ItemController());

            Object[][] items = controller.loadItems();
            String[] columns = controller.getItemTableColumns();

            handleViewButton("No items available at the moment.", items, columns);
        });
        viewItemsButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton inventoryManagementButton = createButton("Inventory Management", e -> {

        });
        inventoryManagementButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton viewPurchaseOrdersButton = createButton("View Purchase Orders", e -> {
            InventoryManagerController controller = new InventoryManagerController(new ItemController());

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

//    // testing the View
//    public static void main(String[] args) {
//        String userID = "WW01";
//        String username = "inventorytest";
//        String password = "inventory123";
//        String firstName = "John";
//        String lastName = "Doe";
//        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//
//        User testView = new User(userID, username, password, firstName, lastName, currentTimeStamp, currentTimeStamp) {
//            @Override
//            public String getUsername() {
//                return "testView";
//            }
//        };
//        new InventoryManagerView(testView);
//    }
}