package view;

import model.User;
import model.RoleName;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class FinanceManagerView extends JFrame {
    private User currentUser;
    private String[] financeOptions = {"View Requisitions", "View Purchase Orders", "Financial Management"};

    private Color background = new Color(21, 31, 46);
    private Color panelColour = new Color(30, 41, 59);
    private Color lightBlue = new Color(96, 103, 205);
    private Color verylightBlue = new Color(165, 180, 252);
    private Color labelColour = new Color(78, 91, 249);
    private Color textColour = new Color(255, 255, 255);

    public FinanceManagerView(User user) {
        this.currentUser = user;
        RoleName roleName = RoleName.FinanceManager;

        setTitle(roleName.toString());
        setSize(1200,700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        JPanel navigationPanel = createNavigation();
        JPanel headerPanel = createHeaderPanel();
        JPanel financeDashboard = createFinanceDashboard();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(background);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        dashboardPanel.add(financeDashboard, BorderLayout.CENTER);


        add(mainPanel, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(dashboardPanel, BorderLayout.CENTER);

    }

    private JPanel createFinanceDashboard() {
        JPanel financeDashboard = new JPanel();
        financeDashboard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel dashboardTitle = new JLabel();
        dashboardTitle.setText("Welcome to Omega Wholesale Business System");
        dashboardTitle.setForeground(labelColour);
        dashboardTitle.setFont(new Font("SanSerif", Font.PLAIN, 16));
        dashboardTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dashboardGreeting = new JLabel("Hello, " + currentUser.getUsername() + "! You are logged in as " + RoleName.FinanceManager + ".");
        dashboardGreeting.setForeground(textColour);
        dashboardGreeting.setFont(new Font("SanSerif", Font.PLAIN, 16));
        dashboardGreeting.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dashboardInstruction = new JLabel("Please select an option from the menu to get started.");
        dashboardInstruction.setForeground(textColour);
        dashboardInstruction.setFont(new Font("SanSerif", Font.PLAIN, 16));
        dashboardInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel quickAccess = new JLabel("Quick Access");
        quickAccess.setForeground(labelColour);
        quickAccess.setFont(new Font("SanSerif", Font.PLAIN, 18));
        quickAccess.setAlignmentX(Component.CENTER_ALIGNMENT);

        financeDashboard.setLayout(new BoxLayout(financeDashboard, BoxLayout.Y_AXIS));
        financeDashboard.setPreferredSize(new Dimension(900, 500));
        financeDashboard.setBackground(panelColour);

        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.Y_AXIS));
        buttonWrapper.setBackground(panelColour);
        buttonWrapper.setPreferredSize(new Dimension(900, 500));
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));


        for(String options : financeOptions) {
            JButton dashboardButton = createDashboardButton(options);
            buttonWrapper.add(dashboardButton);
        }

//        JButton b1 = createDashboardButton("View Requisition", e -> {});
//        JButton b2 = createDashboardButton("View Purchase Order", e -> {});
//        JButton b3 = createDashboardButton("Financial Management", e -> {});

        financeDashboard.add(dashboardTitle);
        financeDashboard.add(dashboardGreeting);
        financeDashboard.add(dashboardInstruction);
        financeDashboard.add(quickAccess);
        financeDashboard.add(buttonWrapper);
//        buttonWrapper.add(b1);
//        buttonWrapper.add(b2);
//        buttonWrapper.add(b3);

        return financeDashboard;
    }

    protected JPanel createNavigation() {
        JPanel navigationPanel = new JPanel();

        navigationPanel.setBackground(panelColour);
        navigationPanel.setPreferredSize(new Dimension(250, getHeight()));
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        navigationPanel.setVisible(true);

        JLabel navigationLabel = new JLabel("OWSB System");
        navigationLabel.setForeground(labelColour);
        navigationLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        //user
        JLabel user = new JLabel();
        user.setText("User: " + currentUser.getUsername());
        user.setFont(new Font("SansSerif", Font.BOLD, 14));
        user.setForeground(textColour);
        user.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleName = new JLabel();
        roleName.setText("Role: " + RoleName.FinanceManager);
        roleName.setFont(new Font("SansSerif", Font.BOLD, 14));
        roleName.setForeground(textColour);
        roleName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.Y_AXIS));
        buttonWrapper.setBackground(panelColour);
        buttonWrapper.setPreferredSize(new Dimension(300, 500));
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));


        for(String options : financeOptions) {
            JButton navigationButton = createNavigationButton(options);
            buttonWrapper.add(navigationButton);
        }

//        JButton b1 = createNavigationButton("View Requisition", e -> {});
//        JButton b2 = createNavigationButton("View Purchase Order", e -> {});
//        JButton b3 = createNavigationButton("Financial Management", e -> {});

        navigationPanel.add(navigationLabel);
        navigationPanel.add(user);
        navigationPanel.add(roleName);
        navigationPanel.add(buttonWrapper);
//        buttonWrapper.add(b1);
//        buttonWrapper.add(b2);
//        buttonWrapper.add(b3);

        return navigationPanel;
    }

    protected JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();

        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(panelColour);
        headerPanel.setPreferredSize(new Dimension(900, 100));

        JLabel headerLabel = new JLabel("Dashboard");
        headerLabel.setForeground(labelColour);
        headerLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));

        JLabel headerDate = new JLabel();
        headerDate.setText(java.time.LocalDate.now() + "");
        headerDate.setForeground(textColour);
        headerDate.setFont(new Font("SansSerif", Font.PLAIN, 16));
        headerDate.setBorder(BorderFactory.createEmptyBorder(0,800,20,20));

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(headerDate, BorderLayout.EAST);
        return headerPanel;
    }

    private JButton createNavigationButton(String text) {

        JButton navigationButton = new JButton(text);
        navigationButton.setBackground(panelColour);
        navigationButton.setForeground(textColour);
        navigationButton.setFont(new Font("SansSerif", Font.BOLD, 16));
//        navigationButton.addActionListener(actionListener);
        navigationButton.setSize(new Dimension(250, 100));
        navigationButton.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        navigationButton.setFocusPainted(false);

        navigationButton.addMouseListener(new java.awt.event.MouseAdapter() {;
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navigationButton.setBackground(verylightBlue);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                navigationButton.setBackground(panelColour);
            }
        });

        return navigationButton;
    }

    private JButton createDashboardButton (String text) {
        JButton dashboardButton = new JButton(text);
        dashboardButton.setBackground(panelColour);
        dashboardButton.setForeground(textColour);
        dashboardButton.setFont(new Font("SansSerif", Font.BOLD, 16));
//        dashboardButton.addActionListener(actionListener);
        dashboardButton.setSize(new Dimension(900, 100));
        dashboardButton.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 15));
        dashboardButton.setFocusPainted(false);

        dashboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dashboardButton.setBackground(verylightBlue);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dashboardButton.setBackground(panelColour);
            }
        });

        return dashboardButton;
    }

    //PO approval
    public void createPOApproval() {
        JPanel poApprovalPanel = new JPanel();
        poApprovalPanel.setLayout(new BorderLayout());
        poApprovalPanel.setPreferredSize(new Dimension(700, 400));
        poApprovalPanel.setBackground(panelColour);
        poApprovalPanel.setVisible(true);

        JLabel title = new JLabel("Purchase Order Approval");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(textColour);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        poApprovalPanel.add(title, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(verylightBlue);
        textArea.setForeground(textColour);
        textArea.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(textArea);
        poApprovalPanel.add(scrollPane, BorderLayout.CENTER);

        JButton approveButton = new JButton("Approve");
        approveButton.setBackground(labelColour);
        approveButton.setForeground(textColour);
        approveButton.setPreferredSize(new Dimension(150, 50));

    }



}