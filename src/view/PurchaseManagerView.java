package view;

import controller.*;
import model.User;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.jdatepicker.impl.*;
import java.util.Properties;

public class PurchaseManagerView extends JFrame {
    private final User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JScrollPane generatePurchaseOrderScrollPane;
    private JScrollPane viewPurchaseOrderScrollPane;

    private PurchaseManagerController purchaseManagerController;
    private PurchaseOrderController purchaseOrderController;
    private ItemController itemController;
    private SupplierController supplierController;
    private PurchaseRequisitionController requisitionController;

    public PurchaseManagerView(User user) {
        this.currentUser = user;
        itemController = new ItemController();
        supplierController = new SupplierController();
        requisitionController = new PurchaseRequisitionController();
        this.purchaseOrderController = new PurchaseOrderController();
        this.purchaseManagerController = new PurchaseManagerController(itemController, supplierController, requisitionController);
        TableHelper.viewRef = this;

        initComponents();
    }

    public PurchaseOrderController getPurchaseOrderController() {
        return purchaseOrderController;
    }
    private void initComponents() {
        setTitle("OWSB System – Purchase Manager");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        AuthController authController = new AuthController();

        JPanel sidebar = new JPanel(null);
        sidebar.setBackground(UITheme.DARK_BLUE);
        sidebar.setBounds(0, 0, 300, 700);

        JLabel lblSystem = new JLabel("OWSB System");
        lblSystem.setFont(new Font("SanSerif UI", Font.BOLD, 22));
        lblSystem.setForeground(UITheme.LIGHT_BLUE);
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
            btn.setBackground(UITheme.DARK_BLUE);
            btn.setFont(new Font("SanSerif UI", Font.BOLD, 14));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(UITheme.VERY_LIGHT_BLUE);
                }

                public void mouseExited(MouseEvent e) {
                    btn.setBackground(UITheme.MEDIUM_BLUE);
                }
            });
            btn.addActionListener(e -> cardLayout.show(mainPanel, m));
            sidebar.add(btn);
            y += 50;
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(70, 550, 150, 40);
        logoutBtn.setForeground(UITheme.TEXT_WHITE);
        logoutBtn.setBackground(UITheme.LIGHT_BLUE);
        logoutBtn.setFont(new Font("SanSerif UI", Font.BOLD, 14));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(UITheme.MEDIUM_BLUE);
            }

            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(UITheme.LIGHT_BLUE);
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

        // Add panels to mainPanel using controller data
        mainPanel.add(createDashboardPanel(), "Dashboard");
        mainPanel.add(createViewItemsPanel(), "View Items");
        mainPanel.add(createSupplierPanel(), "View Suppliers");
        mainPanel.add(createRequisitionPanel(), "View Requisitions");
        mainPanel.add(createGeneratePurchaseOrderPanel(), "Generate Purchase Order");
        mainPanel.add(createViewPurchaseOrderPanel(), "View Purchase Orders");
        cardLayout.show(mainPanel, "Dashboard");
        add(mainPanel);


        // Property change listener for refreshing PO tables after a new PO is created
        this.addPropertyChangeListener("PO_CREATED", evt -> {

        });

        setVisible(true);

    }

    public static class TableHelper {

        // Reference to view for callback (must set this externally)
        public static PurchaseManagerView viewRef;

        // Method to create a table
        public static JPanel createTable(Object[][] data, String[] columnNames) {
            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return columnNames[column].equalsIgnoreCase("Actions");
                }
            };

            JTable table = new JTable(tableModel);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


            // Center align cells except "Actions"
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (!columnNames[i].equalsIgnoreCase("Actions")) {
                    table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                }
            }

            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equalsIgnoreCase("Actions")) {
                    table.getColumnModel().getColumn(i).setCellRenderer(new ActionButtonRenderer(table));
                    table.getColumnModel().getColumn(i).setCellEditor(new ActionButtonEditor(new JCheckBox(), table));
                    table.getColumnModel().getColumn(i).setPreferredWidth(120);
                    table.getColumnModel().getColumn(i).setMaxWidth(120);
                }
            }

            // Styling
            table.setForeground(UITheme.TEXT_WHITE);
            table.setBackground(UITheme.MEDIUM_BLUE);
            table.setFont(new Font("SansSerif", Font.PLAIN, 10));
            table.setRowHeight(30);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setSelectionBackground(UITheme.VERY_LIGHT_BLUE);
            table.getTableHeader().setReorderingAllowed(false);
            table.setShowGrid(false);

            JTableHeader tableHeader = table.getTableHeader();
            tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
            tableHeader.setBackground(UITheme.VERY_LIGHT_BLUE);
            tableHeader.setForeground(UITheme.TEXT_WHITE);
            tableHeader.setPreferredSize(new Dimension(tableHeader.getPreferredSize().width, 35));

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(UITheme.MEDIUM_BLUE);
            panel.add(table.getTableHeader(), BorderLayout.NORTH);
            panel.add(table, BorderLayout.CENTER);

            return panel;

        }

        static class ActionButtonRenderer extends JPanel implements TableCellRenderer {
            private final JTable table;

            public ActionButtonRenderer(JTable table) {
                this.table = table;
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setOpaque(true);
                setBackground(UITheme.DARK_BLUE);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                removeAll();
                String poID = table.getValueAt(row, 0).toString();
                JButton[] buttons = createActionButtons(poID);

                for (int i = 0; i < buttons.length; i++) {
                    add(buttons[i]);
                    if (i < buttons.length - 1) {
                        add(Box.createRigidArea(new Dimension(5, 0)));
                    }
                }

                return this;
            }
        }

        static class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel panel;
            private final JTable table;

            public ActionButtonEditor(JCheckBox checkBox, JTable table) {
                this.table = table;
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.setBackground(UITheme.DARK_BLUE);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                panel.removeAll();
                String poID = table.getValueAt(row, 0).toString();
                JButton[] buttons = createActionButtons(poID);

                for (int i = 0; i < buttons.length; i++) {
                    panel.add(buttons[i]);
                    if (i < buttons.length - 1) {
                        panel.add(Box.createRigidArea(new Dimension(5, 0)));
                    }
                }

                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return null;
            }
        }

        private static JButton[] createActionButtons(String poID) {
            JButton btnEdit = new JButton("Edit");
            JButton btnDelete = new JButton("Delete");

            btnEdit.setFont(new Font("SansSerif", Font.PLAIN, 10));
            btnDelete.setFont(new Font("SansSerif", Font.PLAIN, 10));

            // Set colors
            btnEdit.setBackground(new Color(0, 128, 0));  // dark green
            btnEdit.setForeground(UITheme.TEXT_WHITE);           // white text for contrast

            btnDelete.setBackground(new Color(204, 0, 0)); // dark red
            btnDelete.setForeground(UITheme.TEXT_WHITE);          // white text

            btnEdit.addActionListener(e -> {
                String[] data = viewRef.getPurchaseOrderController().getPurchaseOrderDataById(poID);

                if (data != null && data.length >= 9) { // at least 9 fields expected
                    try {
                        String date = data[1];
                        String requisition = data[2];
                        String item = data[3];
                        int quantity = Integer.parseInt(data[4].trim());
                        String supplier = data[5];
                        String status = data[6];
                        String createdBy = data[7];
                        String approvedBy = data[8];

                        JPanel editPanel = viewRef.createEditFormPanel(
                                poID, date, requisition, item, quantity, supplier, status, createdBy, approvedBy
                        );

                        JDialog dialog = new JDialog((Frame) null, "Edit Purchase Order " + poID, true);
                        dialog.getContentPane().add(editPanel);
                        dialog.pack();
                        dialog.setSize(420, 570);
                        dialog.setLocationRelativeTo(null);
                        dialog.setResizable(false);

                        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosed(java.awt.event.WindowEvent e) {
                                viewRef.updateTables();
                            }

                            @Override
                            public void windowClosing(java.awt.event.WindowEvent e) {
                                viewRef.updateTables();
                            }
                        });

                        dialog.setVisible(true);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error loading edit form: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Could not load Purchase Order data.");
                }
            });

            btnDelete.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to delete PO " + poID + "?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // delete method in controller is void, so no return
                        viewRef.getPurchaseOrderController().delete(poID);
                        JOptionPane.showMessageDialog(null, "Purchase Order deleted.");
                        viewRef.updateTables(); // refresh the table
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Failed to delete Purchase Order.\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            return new JButton[]{btnEdit, btnDelete};
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
        lbl.setForeground(UITheme.PURPLE);
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
        welcomeLabel.setBounds(20, 120, 900, 30);
        p.add(welcomeLabel);

        JLabel greetingLabel = new JLabel("Hello, " + username + "! You are logged in as ", SwingConstants.CENTER);
        greetingLabel.setFont(new Font("SanSerif", Font.PLAIN, 14));
        greetingLabel.setForeground(Color.WHITE);
        greetingLabel.setBounds(-60, 160, 900, 20);
        p.add(greetingLabel);

        JLabel roleLabel = new JLabel("Purchase Manager");
        roleLabel.setFont(new Font("SanSerif", Font.BOLD, 14));
        roleLabel.setForeground(UITheme.PURPLE);
        roleLabel.setBounds(520, 160, 200, 20);
        p.add(roleLabel);

        JLabel instructionLabel = new JLabel("Please select an option from the menu to get started.", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("SanSerif", Font.PLAIN, 14));
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setBounds(0, 200, 900, 20);
        p.add(instructionLabel);

        JLabel quickAccessLabel = new JLabel("Quick Access", SwingConstants.CENTER);
        quickAccessLabel.setFont(new Font("SanSerif", Font.BOLD, 16));
        quickAccessLabel.setForeground(UITheme.PURPLE);
        quickAccessLabel.setBounds(0, 240, 900, 25);
        p.add(quickAccessLabel);

        JLabel link1 = createQuickAccessLink("View Items", "View Items", 300);
        link1.setBounds(50, 320, 300, 30);
        JLabel link2 = createQuickAccessLink("View Suppliers", "View Suppliers", 340);
        link2.setBounds(50, 360, 300, 30);
        JLabel link3 = createQuickAccessLink("View Requisitions", "View Requisitions", 380);
        link3.setBounds(50, 400, 300, 30);
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
                link.setForeground(UITheme.LIGHT_PURPLE);
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
        p.setBackground(UITheme.MEDIUM_BLUE);
        p.add(createTopBar("View Items"));

        // Create the controllers
        ItemController itemController = new ItemController();

        // Pass both controllers into the PurchaseManagerController
        PurchaseManagerController controller = new PurchaseManagerController(itemController, null, null);


        // Load item data and columns
        Object[][] itemData = controller.loadItems();
        String[] itemColumns = controller.getItemTableColumns();

        // Create either a table or a "no data" message panel
        JPanel tablePanel;
        if (itemData == null || itemData.length == 0) {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Items Available");
        } else {
            tablePanel = TableHelper.createTable(itemData, itemColumns);  // Returns a JPanel containing JTable
        }

        // Ensure preferred size is larger than scroll pane size to enable horizontal scroll
        tablePanel.setPreferredSize(new Dimension(1000, 500));

        // ✅ Wrap the tablePanel in a JScrollPane
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBounds(50, 120, 800, 500);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.MEDIUM_BLUE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        p.add(scrollPane);

        return p;
    }

    private JPanel createSupplierPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(UITheme.MEDIUM_BLUE);
        p.add(createTopBar("View Suppliers"));

        SupplierController supplierController = new SupplierController();
        PurchaseManagerController controller = new PurchaseManagerController(null, supplierController, null);

        // Load supplier data and columns
        Object[][] supplierData = controller.loadSuppliers();
        String[] supplierColumns = controller.getSupplierTableColumns();

        // Create either a table or a "no data" message panel
        JPanel tablePanel;
        if (supplierData == null || supplierData.length == 0) {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Suppliers Available");
        } else {
            tablePanel = TableHelper.createTable(supplierData, supplierColumns);

            tablePanel.setPreferredSize(new Dimension(1000, 500));
        }

        // Wrap in JScrollPane
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBounds(50, 120, 800, 500);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.MEDIUM_BLUE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


        p.add(scrollPane);
        return p;
    }

    private JPanel createRequisitionPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(UITheme.MEDIUM_BLUE);
        p.add(createTopBar("View Requisitions"));

        Object[][] requisitionData = purchaseManagerController.loadRequisitions(); // ← this should call your wrapper method
        String[] requisitionColumns = purchaseManagerController.getRequisitionTableColumns();

        JPanel tablePanel;
        if (requisitionData == null || requisitionData.length == 0) {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Requisitions Available");
        } else {
            tablePanel = TableHelper.createTable(requisitionData, requisitionColumns);
            tablePanel.setPreferredSize(new Dimension(1000, 500)); // Allow scroll horizontally if needed
        }

        // Wrap in JScrollPane
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBounds(50, 120, 800, 500);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.MEDIUM_BLUE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


        p.add(scrollPane);
        return p;
    }

    private JPanel createGeneratePurchaseOrderPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(UITheme.MEDIUM_BLUE);
        p.add(createTopBar("Generate Purchase Order"));

        Object[][] purchaseOrderData = purchaseManagerController.loadPurchaseOrders(true);
        String[] purchaseOrderColumns = purchaseManagerController.getPurchaseOrderTableColumns(true);

        JPanel tablePanel;
        if (purchaseOrderData == null || purchaseOrderData.length == 0) {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No purchase orders available.");
        } else {
            tablePanel = TableHelper.createTable(purchaseOrderData, purchaseOrderColumns);
        }

        tablePanel.setPreferredSize(new Dimension(1000, 500));  // same size as View Items

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBounds(50, 150, 800, 500);  // match View Items bounds
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.MEDIUM_BLUE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        p.add(scrollPane);


        JButton btnCreatePOForm = new JButton("Create New Purchase Order");
        btnCreatePOForm.setBounds(350, 100, 240, 30);
        btnCreatePOForm.setFocusPainted(false);
        btnCreatePOForm.setBackground(UITheme.HIGHLIGHT_BLUE);
        btnCreatePOForm.setForeground(Color.WHITE);
        btnCreatePOForm.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCreatePOForm.addActionListener(e -> {
            JDialog createPOFormDialog = new JDialog(this, "Create New Purchase Order", true);
            JPanel formPanel = createFormPanel();
            formPanel.setPreferredSize(new Dimension(800, 700));

            JScrollPane scrollPaneForForm = new JScrollPane(formPanel);
            scrollPaneForForm.setPreferredSize(new Dimension(800, 500));
            scrollPaneForForm.setBorder(BorderFactory.createEmptyBorder());
            scrollPaneForForm.getVerticalScrollBar().setUnitIncrement(16);

            createPOFormDialog.setSize(820, 550);
            createPOFormDialog.setLocationRelativeTo(null);
            createPOFormDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            createPOFormDialog.add(scrollPaneForForm);
            createPOFormDialog.setVisible(true);
        });

        p.add(btnCreatePOForm);

        return p;
    }

    public JPanel createFormPanel(){
        return  createPOFormPanel(false, null, null, null, null, 1, null, null, null, null);
    }

    public JPanel createEditFormPanel(String poID, String date, String requisition, String item, int quantity,
                                      String supplier, String status, String createdBy, String approvedBy) {
        return createPOFormPanel(true, poID, date, requisition, item, quantity, supplier, status, createdBy, approvedBy);
    }

    public JPanel createPOFormPanel(
            boolean isEditMode,
            String poID,
            String date,
            String requisition,
            String item,
            int quantity,
            String supplier,
            String status,
            String createdBy,
            String approvedBy
    ) {
        PurchaseOrderController controller = new PurchaseOrderController();

        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(UITheme.DARK_BLUE);

        JLabel lblPOID = createLabel("Purchase Order ID:", 50, 10);
        JTextField tfPOID = styleTextField(new JTextField(isEditMode ? poID : "Auto-Generated if left blank"));
        tfPOID.setBounds(50, 35, 300, 35);
        tfPOID.setEditable(!isEditMode);

        // ---- Label ----
        JLabel lblDate = createLabel("Date (YYYY-MM-DD):", 50, 80);

        JTextField tfDate = styleTextField(new JTextField(
                isEditMode ? date : java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ));
        tfDate.setBounds(50, 105, 270, 35);

        JButton btnCalendar = new JButton("📅");
        btnCalendar.setBounds(325, 105, 25, 35);
        btnCalendar.setFocusPainted(false);
        btnCalendar.setBorderPainted(false);
        btnCalendar.setBackground(UITheme.LIGHT_BLUE);
        btnCalendar.setForeground(Color.WHITE);

        btnCalendar.addActionListener(e -> {
            // Initialize date model from text field
            UtilDateModel model = new UtilDateModel();
            try {
                model.setValue(java.sql.Date.valueOf(tfDate.getText()));
            } catch (Exception ex) {
                model.setValue(new java.util.Date());
            }
            model.setSelected(true);

            Properties p = new Properties();
            p.put("text.today", "Today");
            p.put("text.month", "Month");
            p.put("text.year", "Year");

            JDatePanelImpl datePanel = new JDatePanelImpl(model, p);

            // Create popup menu for calendar
            JPopupMenu popup = new JPopupMenu();
            popup.setLayout(new BorderLayout());
            popup.add(datePanel, BorderLayout.CENTER);

            // On date selected, update tfDate and close popup
            datePanel.addActionListener(ev -> {
                java.util.Date selectedDate = (java.util.Date) model.getValue();
                if (selectedDate != null) {
                    String formatted = new java.text.SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
                    tfDate.setText(formatted);
                }
                popup.setVisible(false); // close after selection
            });

            // Show below 📅 button
            popup.show(btnCalendar, 0, btnCalendar.getHeight());
        });

        JLabel lblReq = createLabel("Requisition:", 50, 150);
        JComboBox<String> cbReq = styleComboBox(new JComboBox<>());
        cbReq.setBounds(50, 175, 300, 35);
        cbReq.addItem("Direct Order (No Requisition)");
        for (String req : PurchaseOrderController.loadRequisitionOptions()) {
            cbReq.addItem(req);
        }
        cbReq.setSelectedItem(isEditMode && !requisition.equals("Direct") ? requisition : "Direct Order (No Requisition)");

        JLabel lblItem = createLabel("Item:", 50, 220);
        JComboBox<String> cbItem = styleComboBox(new JComboBox<>());
        cbItem.setBounds(50, 245, 300, 35);

        if (!isEditMode) {
            cbItem.addItem("Select Item");  // Placeholder only in create mode
        }
        for (String i : PurchaseOrderController.loadItemOptions()) {
            cbItem.addItem(i);
        }
        if (isEditMode) {
            cbItem.setSelectedItem(item);
        } else {
            cbItem.setSelectedIndex(0); // Select placeholder initially in create mode
        }

        JLabel lblQty = createLabel("Quantity:", 50, 290);
        JSpinner spQty = createStyledSpinner(50, 315);
        if (isEditMode) spQty.setValue(quantity);

        JLabel lblSup = createLabel("Supplier:", 50, 360);
        JComboBox<String> cbSup = styleComboBox(new JComboBox<>());
        cbSup.setBounds(50, 385, 300, 35);

        if (!isEditMode) {
            cbSup.addItem("Select Supplier");  // Placeholder only in create mode
        }
        for (String sup : PurchaseOrderController.loadSupplierOptions()) {
            cbSup.addItem(sup);
        }
        if (isEditMode) {
            cbSup.setSelectedItem(supplier);
        } else {
            cbSup.setSelectedIndex(0); // Select placeholder initially in create mode
        }

        JTextField tfSupDisplay = styleTextField(new JTextField());
        tfSupDisplay.setBounds(50, 245, 300, 35);
        tfSupDisplay.setEditable(false);
        tfSupDisplay.setVisible(false);

        JButton btnSubmit = createSubmitButton(isEditMode ? "Update Purchase Order" : "Create Purchase Order", 50, 440);

        final String[] statusWrapper = {status != null ? status : "Pending"};
        final String type = "purchase";
        final String[] extraStatusWrapper = {status != null ? status : "Pending"};

        // Requisition logic
        cbReq.addActionListener(e -> {
            boolean isDirect = cbReq.getSelectedItem().equals("Direct Order (No Requisition)");

            lblItem.setVisible(isDirect);
            cbItem.setVisible(isDirect);
            lblQty.setVisible(isDirect);
            spQty.setVisible(isDirect);

            cbSup.setVisible(isDirect);
            tfSupDisplay.setVisible(!isDirect);

            if (!isDirect) {
                String autoSup = controller.getSupplierForRequisition((String) cbReq.getSelectedItem());
                tfSupDisplay.setText(autoSup);
                lblSup.setBounds(50, 220, 300, 20);
                tfSupDisplay.setBounds(50, 245, 300, 35);
            } else {
                cbSup.removeAllItems();
                for (String sup : PurchaseOrderController.loadSupplierOptions()) {
                    cbSup.addItem(sup);
                }
                cbSup.setSelectedItem(isEditMode ? supplier : null);
                lblSup.setBounds(50, 360, 300, 20);
                cbSup.setBounds(50, 385, 300, 35);
            }
        });

        // Trigger initial state
        cbReq.setSelectedItem(isEditMode && !requisition.equals("Direct") ? requisition : "Direct Order (No Requisition)");
        cbReq.getActionListeners()[0].actionPerformed(null); // force apply logic

        // Submit button handler
        btnSubmit.addActionListener(e -> {
            String actualPOID = tfPOID.getText().trim();
            if (!isEditMode && (actualPOID.isEmpty() || actualPOID.equals("Auto-Generated if left blank"))) {
                actualPOID = controller.generateNewPOID();
            }

            String updatedDate = tfDate.getText().trim();
            String updatedReq;
            String updatedItem;
            int updatedQty;
            String updatedSup;
            boolean isDirect = cbReq.getSelectedItem().equals("Direct Order (No Requisition)");

            if (isDirect) {
                updatedReq = "Direct";
                updatedItem = (String) cbItem.getSelectedItem();
                updatedQty = (int) spQty.getValue();
                updatedSup = (String) cbSup.getSelectedItem();
                statusWrapper[0] = "Pending";
                extraStatusWrapper[0] = "Pending";
            } else {
                updatedReq = (String) cbReq.getSelectedItem();
                PurchaseOrderController.RequisitionDetails rd = controller.getRequisitionDetails(updatedReq);
                if (rd == null) {
                    JOptionPane.showMessageDialog(formPanel, "Requisition not found.");
                    return;
                }
                updatedItem = rd.item;
                updatedQty = rd.quantity;
                updatedSup = rd.supplier;
                statusWrapper[0] = rd.status;
                extraStatusWrapper[0] = rd.status;
            }

            try {
                boolean success;
                if (isEditMode) {
                    success = controller.updatePurchaseOrder(
                            actualPOID, updatedDate, updatedReq, updatedItem, updatedQty,
                            updatedSup, statusWrapper[0], type, extraStatusWrapper[0]
                    );
                } else {
                    success = controller.createPurchaseOrder(
                            actualPOID, updatedDate, updatedReq, updatedItem, updatedQty,
                            updatedSup, statusWrapper[0], type, extraStatusWrapper[0]
                    );
                }

                if (success) {
                    JOptionPane.showMessageDialog(formPanel, isEditMode ? "Purchase Order updated." : "Purchase Order created with ID: " + actualPOID);
                    Window w = SwingUtilities.getWindowAncestor(formPanel);
                    if (w != null) w.dispose();
                    updateTables();
                } else {
                    JOptionPane.showMessageDialog(formPanel, "Failed to " + (isEditMode ? "update" : "create") + " Purchase Order.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(formPanel, "Error occurred during " + (isEditMode ? "update." : "creation."));
            }
        });

        // Add components
        formPanel.add(lblPOID);
        formPanel.add(tfPOID);
        formPanel.add(lblDate);
        formPanel.add(tfDate);
        formPanel.add(btnCalendar);
        formPanel.add(lblReq);
        formPanel.add(cbReq);
        formPanel.add(lblItem);
        formPanel.add(cbItem);
        formPanel.add(lblQty);
        formPanel.add(spQty);
        formPanel.add(lblSup);
        formPanel.add(cbSup);
        formPanel.add(tfSupDisplay);
        formPanel.add(btnSubmit);

        return formPanel;
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(UITheme.TEXT_WHITE);
        label.setBounds(x, y, 200, 25);
        return label;
    }

    private JSpinner createStyledSpinner(int x, int y) {
        Font fieldFont = new Font("SansSerif", Font.PLAIN, 14);
        Color fieldBackground = (UITheme.MEDIUM_BLUE);
        Color borderColor = (UITheme.DARK_GRAY);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        spinner.setFont(fieldFont);
        JComponent editor = spinner.getEditor();
        JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
        textField.setBackground(fieldBackground);
        textField.setForeground(UITheme.TEXT_WHITE);
        textField.setCaretColor(UITheme.TEXT_WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        spinner.setBounds(x, y, 300, 35);
        return spinner;
    }

    private JButton createSubmitButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 300, 40);
        button.setFocusPainted(false);
        button.setBackground(UITheme.HIGHLIGHT_BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    // Styles - no change needed
    private JTextField styleTextField(JTextField field) {
        Font fieldFont = new Font("SansSerif", Font.PLAIN, 14);
        Color fieldBackground = (UITheme.DARK_BLUE);
        Color borderColor = (UITheme.DARK_GRAY);

        field.setFont(fieldFont);
        field.setBackground(fieldBackground);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JComboBox<String> styleComboBox(JComboBox<String> box) {
        Font fieldFont = new Font("SansSerif", Font.PLAIN, 14);
        Color fieldBackground = (UITheme.MEDIUM_BLUE);
        Color borderColor = (UITheme.DARK_GRAY );

        box.setFont(fieldFont);
        box.setBackground(fieldBackground);
        box.setForeground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        box.setFocusable(false);
        return box;
    }

    private JPanel createViewPurchaseOrderPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(UITheme.MEDIUM_BLUE);
        p.add(createTopBar("View Purchase Orders"));

        Object[][] purchaseOrderData = purchaseManagerController.loadPurchaseOrders(false);
        String[] purchaseOrderColumns = purchaseManagerController.getPurchaseOrderTableColumns(false);

        JPanel tablePanel;
        if (purchaseOrderData == null || purchaseOrderData.length == 0) {
            tablePanel = UIHelper.createDisplayNoDataAvailableMessage("No purchase orders available.");
        } else {
            tablePanel = TableHelper.createTable(purchaseOrderData, purchaseOrderColumns);
        }

        // Set preferred size bigger than scroll pane to enable horizontal scrolling
        tablePanel.setPreferredSize(new Dimension(1000, 500));

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBounds(20, 160, 860, 460);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.MEDIUM_BLUE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


        p.add(scrollPane);


        viewPurchaseOrderScrollPane = scrollPane;

        return p;
    }

    public void updateTables() {
        if (purchaseOrderController == null || purchaseManagerController == null) {
            System.err.println("Controllers are not initialized!");
            return;
        }

        // Load fresh purchase order data with full status details
        Object[][] fullData = purchaseManagerController.loadPurchaseOrders(true);

        // Columns for Generate Purchase Order table (with Actions like Edit/Delete buttons)
        String[] columnsWithActions = purchaseManagerController.getPurchaseOrderTableColumns(true);

        JPanel generateTablePanel;
        if (fullData != null && fullData.length > 0) {
            generateTablePanel = TableHelper.createTable(fullData, columnsWithActions);
        } else {
            generateTablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Purchase Orders Available");
        }

        if (generatePurchaseOrderScrollPane != null) {
            generatePurchaseOrderScrollPane.setViewportView(generateTablePanel);
        }

        // Columns for View Purchase Order table (without Actions)
        String[] columnsWithoutActions = purchaseManagerController.getPurchaseOrderTableColumns(false);

        JPanel viewTablePanel;
        if (fullData != null && fullData.length > 0) {
            viewTablePanel = TableHelper.createTable(fullData, columnsWithoutActions);
        } else {
            viewTablePanel = UIHelper.createDisplayNoDataAvailableMessage("No Purchase Orders Available");
        }

        if (viewPurchaseOrderScrollPane != null) {
            viewPurchaseOrderScrollPane.setViewportView(viewTablePanel);
        }
    }

    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws java.text.ParseException {
            return dateFormatter.parse(text);
        }

        @Override
        public String valueToString(Object value) throws java.text.ParseException {
            if (value != null) {
                java.util.Calendar cal = (java.util.Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }
}