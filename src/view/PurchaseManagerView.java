package view;

import controller.AuthController;
import model.PurchaseManager;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PurchaseManagerView extends JFrame {
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public PurchaseManagerView(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        // Frame setup
        setTitle("OWSB System – Purchase Manager");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // Create an AuthController instance for logout
        AuthController authController = new AuthController();

        // ── Sidebar ────────────────────────────────────────────────
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
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(113,119,255)); }
                public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(30,35,55));   }
            });
            btn.addActionListener(e -> cardLayout.show(mainPanel, m));
            sidebar.add(btn);
            y += 50;
        }

        // ── Logout button ───────────────────────────────────────────
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(20, 640, 260, 40);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(200, 50, 50));
        logoutBtn.setFont(new Font("SanSerif UI", Font.BOLD, 14));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(new Color(230, 80, 80)); }
            public void mouseExited(MouseEvent e)  { logoutBtn.setBackground(new Color(200, 50, 50)); }
        });
        logoutBtn.addActionListener(e -> {
            dispose();                        // close this dashboard
            authController.displayLoginMenu(); // show login view
        });
        sidebar.add(logoutBtn);

        add(sidebar);

        // ── Main panel with CardLayout ─────────────────────────────
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBounds(300, 0, 900, 700);

        mainPanel.add(createDashboardPanel(),       "Dashboard");
        mainPanel.add(createItemPanel(),            "View Items");
        mainPanel.add(createSupplierPanel(),        "View Suppliers");
        mainPanel.add(createRequisitionPanel(),     "View Requisitions");
        mainPanel.add(createPurchaseOrderPanel(),   "Generate Purchase Order");
        mainPanel.add(createPurchaseOrderViewPanel(),"View Purchase Orders");

        add(mainPanel);
        cardLayout.show(mainPanel, "Dashboard");

        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(36, 42, 64));

        // Top Panel (title + date)
        JPanel topPanel = new JPanel(null);
        topPanel.setBackground(new Color(20, 25, 45));
        topPanel.setBounds(0, 0, 900, 100);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SanSerif UI", Font.BOLD, 24));
        title.setForeground(new Color(135, 142, 255));
        title.setBounds(20, 30, 200, 30);
        topPanel.add(title);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel date = new JLabel(today);
        date.setForeground(Color.WHITE);
        date.setFont(new Font("SanSerif UI", Font.PLAIN, 14));
        date.setBounds(750, 30, 120, 30);
        topPanel.add(date);

        p.add(topPanel);

        String username = currentUser.getUsername();

        // Updated welcomeText with smaller font size and adjusted line height
        String welcomeText = "<html><div style='text-align: center; font-size: 12px; line-height: 1.4;'>"
                + "Welcome to Omega Wholesale Business System<br><br>"
                + "Hello, " + username + "! You are logged in as <span style='color: #a092f6;'>Purchase Manager</span>.<br><br>"
                + "Please select an option from the menu to get started.<br><br>"
                + "<span style='color: #a092f6;'>Quick Access</span><br>"
                +"<br><br><div></html>";

        // Create the JLabel and ensure it's properly rendered
        JLabel lblWelcome = new JLabel();
        lblWelcome.setText(welcomeText);  // This is where HTML is set
        lblWelcome.setBounds(50, 120, 800, 160);  // Increased height to allow more space for links
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setVerticalAlignment(SwingConstants.TOP);  // Align text to top if necessary
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);  // Center align the text horizontally

        p.add(lblWelcome);

        // Move all the links up a bit
        p.add(createQuickAccessLink("View Items", "View Items", 340));  // Moved "View Items" link up (from 350 to 340)
        p.add(createQuickAccessLink("View Suppliers", "View Suppliers", 390)); // Moved "View Suppliers" link up (from 420 to 390)
        p.add(createQuickAccessLink("View Requisitions", "View Requisitions", 440)); // Moved "View Requisitions" link up (from 470 to 440)

        return p;
    }


    /** Quick-access link labels */
    private JLabel createQuickAccessLink(String text, String panelName, int y) {
        JLabel link = new JLabel(text);
        link.setBounds(50, y, 300, 30);
        link.setForeground(Color.WHITE);
        link.setFont(new Font("SanSerif UI", Font.BOLD, 16));
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e)  { link.setForeground(new Color(170,150,255)); }
            public void mouseExited(MouseEvent e)   { link.setForeground(Color.WHITE);       }
            public void mouseClicked(MouseEvent e)  { cardLayout.show(mainPanel, panelName); }
        });
        return link;
    }

    // Stub panels
    private JPanel createItemPanel()            { return new JPanel(); }
    private JPanel createSupplierPanel()        { return new JPanel(); }
    private JPanel createRequisitionPanel()     { return new JPanel(); }
    private JPanel createPurchaseOrderPanel()  { return new JPanel(); }
    private JPanel createPurchaseOrderViewPanel(){ return new JPanel(); }

    public static void main(String[] args) {
        PurchaseManager pm = new PurchaseManager(
                "PM001", "purchase1", "purchase123",
                "Michael", "Williams", "michealwilliams@oswb.com",  "active", "2025-04-01", "2025-04-01"
        );
        SwingUtilities.invokeLater(() -> new PurchaseManagerView(pm));
    }
}

