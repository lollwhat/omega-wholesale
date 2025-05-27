package view;

import controller.AuthController;
import controller.PurchaseManagerController;
import controller.ItemController;
import controller.SupplierController;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PurchaseManagerView extends JFrame {
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTable purchaseOrderTable;
    private DefaultTableModel tableModel;
    private JButton saveButton;
    private JComboBox<String> itemComboBox;
    private JComboBox<String> supplierComboBox;
    private JTextField qtyField;
    private JTextField dateField;
    private JComboBox<String> requisitionComboBox;
    private PurchaseManagerController controller;



    public PurchaseManagerView(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("OWSB System – Purchase Manager");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        AuthController authController = new AuthController();

        JPanel sidebar = new JPanel(null);
        sidebar.setBackground(new Color(20, 25, 45));
        sidebar.setBounds(0, 0, 300, 700);

        JLabel lblSystem = new JLabel("OWSB System");
        lblSystem.setFont(new Font("SanSerif UI", Font.BOLD, 22));
        lblSystem.setForeground(new Color(128, 140, 255));
        lblSystem.setBounds(20, 20, 260, 30);
        sidebar.add(lblSystem);

        JLabel lblUser = new JLabel("User: " + currentUser.getUsername());
        lblUser.setForeground(new Color(160, 160, 160));
        lblUser.setBounds(20, 60, 260, 20);
        sidebar.add(lblUser);

        JLabel lblRole = new JLabel("Role: Purchase Manager");
        lblRole.setForeground(new Color(160, 160, 160));
        lblRole.setBounds(20, 80, 260, 20);
        sidebar.add(lblRole);

        String[] menu = {
                "View Items", "View Suppliers", "View Requisitions",
                "Generate Purchase Order", "View Purchase Orders"
        };
        int y = 130;
        for (String m : menu) {
            JButton btn = new JButton(m);
            btn.setBounds(20, y, 260, 40);
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(30, 35, 55));
            btn.setFont(new Font("SanSerif UI", Font.BOLD, 14));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(113, 119, 255));
                }

                public void mouseExited(MouseEvent e) {
                    btn.setBackground(new Color(30, 35, 55));
                }
            });
            btn.addActionListener(e -> cardLayout.show(mainPanel, m));
            sidebar.add(btn);
            y += 50;
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(70, 550, 150, 40);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(96, 103, 205));
        logoutBtn.setFont(new Font("SanSerif UI", Font.BOLD, 14));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(new Color(30, 41, 59));
            }

            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(new Color(96, 41, 205));
            }
        });
        logoutBtn.addActionListener(e -> {
            dispose();
            authController.displayLoginMenu();
        });
        sidebar.add(logoutBtn);

        add(sidebar);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBounds(300, 0, 900, 700);

        mainPanel.add(createDashboardPanel(), "Dashboard");
        mainPanel.add(createViewItemsPanel(), "View Items");
        mainPanel.add(createSupplierPanel(), "View Suppliers");
        mainPanel.add(createRequisitionPanel(), "View Requisitions");
        mainPanel.add(createGeneratePurchaseOrderPanel(), "Generate Purchase Order");
        mainPanel.add(createViewPurchaseOrderPanel(), "View Purchase Orders");


        add(mainPanel);
        cardLayout.show(mainPanel, "Dashboard");

        this.addPropertyChangeListener("PO_CREATED", evt -> {
            refreshGeneratePOTable();
            refreshViewPOTable();
        });

        setVisible(true);
    }


    public static class TableHelper {
        public static JPanel createTable(Object[][] data, String[] columnNames) {
            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(tableModel);

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
            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(false);
            table.setSelectionBackground(new Color(165, 180, 252));

            table.getTableHeader().setReorderingAllowed(false);
            table.setShowGrid(false);

            JTableHeader tableHeader = table.getTableHeader();
            tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
            tableHeader.setBackground(new Color(165, 180, 252));
            tableHeader.setForeground(Color.WHITE);
            tableHeader.setPreferredSize(new Dimension(tableHeader.getPreferredSize().width, 35));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(new Color(30, 41, 59));

            // Adding horizontal scrolling
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            return tablePanel;
        }
    }


    public static class UIHelper {
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


    private JPanel createTopBar(String title) {
        JPanel bar = new JPanel(null);
        bar.setBackground(new Color(20, 25, 45));
        bar.setBounds(0, 0, 900, 100);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SanSerif UI", Font.BOLD, 24));
        lbl.setForeground(new Color(135, 142, 255));
        lbl.setBounds(20, 30, 400, 30);
        bar.add(lbl);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel date = new JLabel(today);
        date.setFont(new Font("SanSerif UI", Font.PLAIN, 14));
        date.setForeground(Color.WHITE);
        date.setBounds(750, 30, 120, 30);
        bar.add(date);
        return bar;
    }

    private JPanel createDashboardPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(36, 42, 64));

        p.add(createTopBar("Dashboard"));

        String username = currentUser.getUsername();

        JLabel welcomeLabel = new JLabel("Welcome to Omega Wholesale Business System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SanSerif", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(0, 120, 900, 30);
        p.add(welcomeLabel);

        JLabel greetingLabel = new JLabel("Hello, " + username + "! You are logged in as ", SwingConstants.CENTER);
        greetingLabel.setFont(new Font("SanSerif", Font.PLAIN, 14));
        greetingLabel.setForeground(Color.WHITE);
        greetingLabel.setBounds(0, 160, 900, 20);
        p.add(greetingLabel);

        JLabel roleLabel = new JLabel("Purchase Manager");
        roleLabel.setFont(new Font("SanSerif", Font.BOLD, 14));
        roleLabel.setForeground(new Color(160, 146, 246));
        roleLabel.setBounds(580, 160, 200, 20);
        p.add(roleLabel);

        JLabel instructionLabel = new JLabel("Please select an option from the menu to get started.", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("SanSerif", Font.PLAIN, 14));
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setBounds(0, 200, 900, 20);
        p.add(instructionLabel);

        JLabel quickAccessLabel = new JLabel("Quick Access", SwingConstants.CENTER);
        quickAccessLabel.setFont(new Font("SanSerif", Font.BOLD, 16));
        quickAccessLabel.setForeground(new Color(160, 146, 246));
        quickAccessLabel.setBounds(0, 240, 900, 25);
        p.add(quickAccessLabel);

        JLabel link1 = createQuickAccessLink("View Items", "View Items", 300);
        link1.setBounds(50, 270, 300, 30);
        JLabel link2 = createQuickAccessLink("View Suppliers", "View Suppliers", 340);
        link2.setBounds(50, 310, 300, 30);
        JLabel link3 = createQuickAccessLink("View Requisitions", "View Requisitions", 380);
        link3.setBounds(50, 350, 300, 30);
        p.add(link1);
        p.add(link2);
        p.add(link3);

        return p;
    }

    private JLabel createQuickAccessLink(String text, String panelName, int y) {
        JLabel link = new JLabel(text);
        link.setBounds(50, y, 300, 30);
        link.setForeground(Color.WHITE);
        link.setFont(new Font("SanSerif UI", Font.BOLD, 16));
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                link.setForeground(new Color(170, 150, 255));
            }

            public void mouseExited(MouseEvent e) {
                link.setForeground(Color.WHITE);
            }

            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, panelName);
            }
        });
        return link;
    }

    private JPanel createViewItemsPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(36, 42, 64));
        p.add(createTopBar("View Items"));

        // Create the controllers
        ItemController itemController = new ItemController();

        // Pass both controllers into the PurchaseManagerController
        PurchaseManagerController controller = new PurchaseManagerController(itemController, null);

        // Load item data and columns
        Object[][] itemData = controller.loadItems();
        String[] itemColumns = controller.getItemTableColumns();

        JPanel tablePanel;
        if (itemData != null && itemData.length > 0) {
            tablePanel = TableHelper.createTable(itemData, itemColumns);
        } else {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Items Available");
        }
        tablePanel.setBounds(50, 120, 800, 500);
        p.add(tablePanel);

        return p;
    }

    private JPanel createSupplierPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(36, 42, 64));
        p.add(createTopBar("View Suppliers"));

        SupplierController supplierController = new SupplierController();

        PurchaseManagerController controller = new PurchaseManagerController(null,supplierController);

        Object[][] supplierData = controller.loadSuppliers();
        String[] supplierColumns = controller.getSupplierTableColumns();

        JPanel tablePanel;
        if (supplierData != null && supplierData.length > 0) {
            tablePanel = TableHelper.createTable(supplierData, supplierColumns);
        } else {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Suppliers Available");
        }
        tablePanel.setBounds(50, 120, 800, 500);
        p.add(tablePanel);

        return p;

    }

    private JPanel createRequisitionPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(36, 42, 64));
        p.add(createTopBar("View Requisitions"));
        return p;
    }
    private JPanel createGeneratePurchaseOrderPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(36, 42, 64));
        p.add(createTopBar("Generate Purchase Order"));

        // Table Panel for Purchase Orders (Initially visible)
        PurchaseManagerController controller = new PurchaseManagerController(new ItemController(), new SupplierController());
        Object[][] purchaseOrderData = controller.loadPurchaseOrders(true);  // Correct variable name
        String[] purchaseOrderColumns = controller.getPurchaseOrderTableColumns(true);  // Correct variable name

        // Table Panel
        JPanel tablePanel;
        if (purchaseOrderData == null || purchaseOrderData.length == 0) {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No purchase orders available.");
        } else {
            tablePanel = TableHelper.createTable(purchaseOrderData, purchaseOrderColumns);  // Use the corrected names
        }

        // Adjust the position & size of the table
        tablePanel.setBounds(20, 160, 860, 460);  // Adjusted size for the table
        tablePanel.setPreferredSize(new Dimension(860, 460)); // Set preferred size to match bounds
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBounds(20, 160, 860, 460);  // Add the scrollbar with the same bounds as the table
        p.add(scrollPane);

        // Create button to show form
        JButton btnCreatePOForm = new JButton("Create New Purchase Order");
        btnCreatePOForm.setBounds(350, 100, 200, 30);
        btnCreatePOForm.setFocusPainted(false);
        btnCreatePOForm.setBackground(new Color(88, 101, 242));
        btnCreatePOForm.setForeground(Color.WHITE);
        btnCreatePOForm.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Action to show the popup form
        btnCreatePOForm.addActionListener(e -> {
            // Create and show the form as a JDialog popup
            JDialog createPOFormDialog = new JDialog((Frame) null, "Create New Purchase Order", true);
            createPOFormDialog.setSize(800, 350);
            createPOFormDialog.setLocationRelativeTo(null); // Center on screen
            createPOFormDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            createPOFormDialog.add(createFormPanel()); // Add the form to the dialog
            createPOFormDialog.setVisible(true);
        });

        p.add(btnCreatePOForm);
        return p;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(new Color(36, 42, 64));

        // PO ID
        JLabel lblPOID = new JLabel("Purchase Order ID:");
        lblPOID.setForeground(Color.WHITE);
        lblPOID.setBounds(50, 10, 140, 25);
        formPanel.add(lblPOID);

        JTextField tfPOID = new JTextField("Auto-Generated if left blank");
        tfPOID.setBounds(200, 10, 200, 30);
        tfPOID.setForeground(Color.GRAY);
        formPanel.add(tfPOID);

        // Date
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        lblDate.setForeground(Color.WHITE);
        lblDate.setBounds(420, 10, 140, 25);
        formPanel.add(lblDate);

        JTextField tfDate = new JTextField(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        tfDate.setBounds(580, 10, 200, 30);
        formPanel.add(tfDate);

        // Requisition
        JLabel lblReq = new JLabel("Requisition:");
        lblReq.setForeground(Color.WHITE);
        lblReq.setBounds(50, 50, 140, 25);
        formPanel.add(lblReq);

        JComboBox<String> cbReq = new JComboBox<>();
        for (String req : PurchaseManagerController.loadRequisitionOptions()) {
            cbReq.addItem(req);
        }
        cbReq.setBounds(200, 50, 200, 30);
        formPanel.add(cbReq);

        // Item
        JLabel lblItem = new JLabel("Item:");
        lblItem.setForeground(Color.WHITE);
        lblItem.setBounds(420, 50, 140, 25);
        formPanel.add(lblItem);

        JComboBox<String> cbItem = new JComboBox<>();
        for (String item : PurchaseManagerController.loadItemOptions()) {
            cbItem.addItem(item);
        }
        cbItem.setBounds(580, 50, 200, 30);
        formPanel.add(cbItem);

        // Quantity
        JLabel lblQty = new JLabel("Quantity:");
        lblQty.setForeground(Color.WHITE);
        lblQty.setBounds(50, 90, 140, 25);
        formPanel.add(lblQty);

        JSpinner spQty = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        spQty.setBounds(200, 90, 200, 30);
        formPanel.add(spQty);

        // Supplier
        JLabel lblSup = new JLabel("Supplier:");
        lblSup.setForeground(Color.WHITE);
        lblSup.setBounds(420, 90, 140, 25);
        formPanel.add(lblSup);

        JComboBox<String> cbSup = new JComboBox<>();
        for (String supplier : PurchaseManagerController.loadSupplierOptions()) {
            cbSup.addItem(supplier);
        }
        cbSup.setBounds(580, 90, 200, 30);
        formPanel.add(cbSup);

        // Create button
        JButton btnCreate = new JButton("Create Purchase Order");
        btnCreate.setBounds(350, 130, 200, 35);
        btnCreate.setFocusPainted(false);
        btnCreate.setBackground(new Color(88, 101, 242));
        btnCreate.setForeground(Color.WHITE);
        formPanel.add(btnCreate);

        btnCreate.addActionListener(e -> {
            // 1. Determine PO ID
            String poID = tfPOID.getText().trim();
            if (poID.equals("Auto-Generated if left blank") || poID.isEmpty()) {
                poID = PurchaseManagerController.generateNewPOID();
            }

            String date            = tfDate.getText().trim();
            String requisition     = (String) cbReq.getSelectedItem();
            String itemDisplay     = (String) cbItem.getSelectedItem();
            String qty             = spQty.getValue().toString();
            String supplierDisplay = (String) cbSup.getSelectedItem();

            // 3. Save to file
            boolean saved = PurchaseManagerController.saveNewPO(
                    poID, date, requisition, itemDisplay, qty, supplierDisplay
            );

            if (saved) {
                JOptionPane.showMessageDialog(formPanel,
                        "Purchase Order created with ID: " + poID);

                // Close dialog
                Window w = SwingUtilities.getWindowAncestor(formPanel);
                if (w != null) w.dispose();

                // Get the correct PurchaseManagerView
                Window parentWindow = SwingUtilities.getWindowAncestor(formPanel);
                if (parentWindow instanceof PurchaseManagerView) {
                    PurchaseManagerView view = (PurchaseManagerView) parentWindow;
                    view.firePropertyChange("PO_CREATED", false, true);  // Notify the main window
                }
            } else {
                JOptionPane.showMessageDialog(formPanel,
                        "Failed to save Purchase Order.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return formPanel;
    }

    private void refreshGeneratePOTable() {
        // 1. Re-load purchase orders with actions column (true)
        PurchaseManagerController ctrl = new PurchaseManagerController(
                new ItemController(), new SupplierController()
        );
        Object[][] data = ctrl.loadPurchaseOrders(true);
        String[] cols = ctrl.getPurchaseOrderTableColumns(true);

        // 2. Build a fresh panel
        JPanel newGeneratePanel = createGeneratePurchaseOrderPanel();

        // 3. Replace the old card
        mainPanel.remove(4);  // "Generate Purchase Order" is the 5th card (0-based index)
        mainPanel.add(newGeneratePanel, "Generate Purchase Order");

        cardLayout.show(mainPanel, "Generate Purchase Order");

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void refreshViewPOTable() {
        // 1. Re-load purchase orders without actions column (false)
        PurchaseManagerController ctrl = new PurchaseManagerController(
                new ItemController(), new SupplierController()
        );
        Object[][] data = ctrl.loadPurchaseOrders(false);
        String[] cols = ctrl.getPurchaseOrderTableColumns(false);

        JPanel newViewPanel = createViewPurchaseOrderPanel();

        mainPanel.remove(5);  // "View Purchase Orders" is the 6th card
        mainPanel.add(newViewPanel, "View Purchase Orders");

        cardLayout.show(mainPanel, "View Purchase Orders");

        mainPanel.revalidate();
        mainPanel.repaint();
    }


    private JPanel createViewPurchaseOrderPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(36, 42, 64));
        p.add(createTopBar("View Purchase Orders"));

        // Initialize the controller
        PurchaseManagerController controller = new PurchaseManagerController(new ItemController(), new SupplierController());

        // Load Purchase Orders for the "View Purchase Orders" page (No actions column)
        Object[][] purchaseOrderData = controller.loadPurchaseOrders(false);
        String[] purchaseOrderColumns = controller.getPurchaseOrderTableColumns(false);

        // Create table based on fetched data
        JPanel tablePanel;
        if (purchaseOrderData != null && purchaseOrderData.length > 0) {
            tablePanel = TableHelper.createTable(purchaseOrderData, purchaseOrderColumns);
        } else {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Purchase Orders Available");
        }
        tablePanel.setBounds(50, 120, 800, 500); // Adjust table position and size
        p.add(tablePanel);

        return p;
    }
}