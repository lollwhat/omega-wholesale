package view.forms;

import controller.DailySalesEntryController;
import controller.ItemController;
import view.DailySalesEntryView;
import view.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDailySalesForm extends BaseForm {
    private JComboBox<String> itemComboBox;
    private JComboBox<String> salesDayComboBox;
    private JComboBox<String> salesMonthComboBox;
    private JComboBox<String> salesYearComboBox;
    private JTextField unitPriceDisplayField;
    private JTextField quantityDisplayField;
    private JButton minusQuantityButton;
    private JButton plusQuantityButton;
    private int currentQuantity = 1;

    private JTextField netIncomeField;
    private JLabel generalErrorLabel;

    private final String editSalesEntryId;

    private final String NET_INCOME_PLACEHOLDER = "Calculated net income";
    private final String UNIT_PRICE_PLACEHOLDER = "0.00";
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final DecimalFormat priceDf = new DecimalFormat("0.00");

    private Map<String, Double> itemUnitPrices;
    private Map<String, String> itemFullData;


    public AddDailySalesForm(JFrame parent) {
        super(parent, "Add New Daily Sales Entry", 580);
        this.editSalesEntryId = null;
    }

    public AddDailySalesForm(JFrame parent, String dbSalesEntryId) {
        super(parent, "Edit Daily Sales Entry", 580);
        this.editSalesEntryId = dbSalesEntryId;
        if (this.editSalesEntryId != null) {
            SwingUtilities.invokeLater(this::loadEditData);
        }
    }

    private void loadEditData() {
        if (editSalesEntryId != null) {
            loadSalesEntryData();
        }
    }

    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        itemComboBox = new JComboBox<>();
        JPanel itemPanel = createComboBoxPanel("Select Item:", itemComboBox);
        loadItems();
        itemComboBox.addItemListener(e -> {
            String selectedItemString = (String) itemComboBox.getSelectedItem();
            if (selectedItemString != null && !selectedItemString.equals("-- Select Item --")) {
                if (currentQuantity == 0) {
                    currentQuantity = 1;
                }
                setQuantityButtonsEnabled(true);
            } else {
                currentQuantity = 0;
                setQuantityButtonsEnabled(false);
            }
            updateUnitPriceDisplay();
            updateQuantityDisplay();
            calculateAndDisplayNetIncome();
        });
        formPanel.add(itemPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel datePanelContainer = createSalesDatePanel();
        formPanel.add(datePanelContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel unitPricePanel = createFormFieldPanel("Unit Price:", UNIT_PRICE_PLACEHOLDER);
        unitPriceDisplayField = findTextFieldInPanel(unitPricePanel);
        if (unitPriceDisplayField != null) {
            unitPriceDisplayField.setEditable(false);
            unitPriceDisplayField.setFocusable(false);
            unitPriceDisplayField.setBackground(UITheme.DARK_BLUE);
        }
        formPanel.add(unitPricePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel quantitySpinnerPanel = createQuantitySpinnerPanel("Quantity:");
        formPanel.add(quantitySpinnerPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel netIncomePanel = createFormFieldPanel("Net Income:", NET_INCOME_PLACEHOLDER);
        netIncomeField = findTextFieldInPanel(netIncomePanel);
        if (netIncomeField != null) {
            netIncomeField.setEditable(false);
            netIncomeField.setFocusable(false);
            netIncomeField.setBackground(UITheme.DARK_BLUE);
        }
        formPanel.add(netIncomePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        generalErrorLabel = new JLabel(" ");
        generalErrorLabel.setForeground(UITheme.ERROR_RED);
        generalErrorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        generalErrorLabel.setVisible(false);
        JPanel generalErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        generalErrorPanel.setBackground(mediumBlue);
        generalErrorPanel.add(generalErrorLabel);
        formPanel.add(generalErrorPanel);

        formPanel.add(Box.createVerticalGlue());

        updateUnitPriceDisplay();
        updateQuantityDisplay();
        setQuantityButtonsEnabled(false);

        return formPanel;
    }

    private JPanel createQuantitySpinnerPanel(String labelText) {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 2));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setName("quantitySpinnerContainer");

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(textWhite);
        mainPanel.add(label, BorderLayout.NORTH);

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        spinnerPanel.setBackground(mediumBlue);

        minusQuantityButton = createSmallButton("-");
        minusQuantityButton.addActionListener(e -> adjustQuantity(-1));

        quantityDisplayField = new JTextField(String.valueOf(currentQuantity), 4);
        quantityDisplayField.setEditable(false);
        quantityDisplayField.setFocusable(false);
        quantityDisplayField.setHorizontalAlignment(JTextField.CENTER);
        quantityDisplayField.setBackground(darkBlue);
        quantityDisplayField.setForeground(textWhite);
        quantityDisplayField.setFont(new Font("Arial", Font.PLAIN, 14));
        quantityDisplayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lightBlue),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        plusQuantityButton = createSmallButton("+");
        plusQuantityButton.addActionListener(e -> adjustQuantity(1));

        spinnerPanel.add(minusQuantityButton);
        spinnerPanel.add(quantityDisplayField);
        spinnerPanel.add(plusQuantityButton);

        mainPanel.add(spinnerPanel, BorderLayout.CENTER);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        errorLabel.setForeground(errorRed);
        errorLabel.setVisible(false);
        errorLabelMap.put(quantityDisplayField, errorLabel);
        JPanel errorWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        errorWrapperPanel.setBackground(mediumBlue);
        errorWrapperPanel.add(errorLabel);
        mainPanel.add(errorWrapperPanel, BorderLayout.SOUTH);

        mainPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, mainPanel.getPreferredSize().height + 10));


        return mainPanel;
    }

    private void adjustQuantity(int delta) {
        int newQuantity = currentQuantity + delta;
        if (newQuantity >= 1) {
            currentQuantity = newQuantity;
            updateQuantityDisplay();
            calculateAndDisplayNetIncome();
            clearFieldError(quantityDisplayField);
        } else {
            showFieldError(quantityDisplayField, "Min quantity is 1.");
        }
    }

    private void updateQuantityDisplay() {
        if (quantityDisplayField != null) {
            quantityDisplayField.setText(String.valueOf(currentQuantity));
        }
    }

    private void setQuantityButtonsEnabled(boolean enabled) {
        if (plusQuantityButton != null) plusQuantityButton.setEnabled(enabled);
        if (minusQuantityButton != null) minusQuantityButton.setEnabled(enabled);
    }

    private void updateUnitPriceDisplay() {
        if (itemComboBox == null || unitPriceDisplayField == null || itemUnitPrices == null) return;

        String selectedItemString = (String) itemComboBox.getSelectedItem();
        if (selectedItemString != null && !selectedItemString.equals("-- Select Item --")) {
            Double unitPrice = itemUnitPrices.get(selectedItemString);
            if (unitPrice != null) {
                setFieldValue(unitPriceDisplayField, priceDf.format(unitPrice), UNIT_PRICE_PLACEHOLDER);
            } else {
                setFieldValue(unitPriceDisplayField, "N/A", UNIT_PRICE_PLACEHOLDER);
            }
        } else {
            setFieldValue(unitPriceDisplayField, "", UNIT_PRICE_PLACEHOLDER);
        }
    }


    private JPanel createSalesDatePanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datePanel.setBackground(mediumBlue);

        JLabel dateLabel = new JLabel("Sales Date (D/M/Y):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setForeground(textWhite);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
        datePanel.add(dateLabel);

        salesDayComboBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) salesDayComboBox.addItem(String.format("%02d", i));
        styleDateComboBox(salesDayComboBox);
        datePanel.add(salesDayComboBox);
        datePanel.add(Box.createRigidArea(new Dimension(5,0)));

        salesMonthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) salesMonthComboBox.addItem(String.format("%02d", i));
        styleDateComboBox(salesMonthComboBox);
        datePanel.add(salesMonthComboBox);
        datePanel.add(Box.createRigidArea(new Dimension(5,0)));

        salesYearComboBox = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) salesYearComboBox.addItem(String.valueOf(i));
        salesYearComboBox.setSelectedItem(String.valueOf(currentYear));
        salesMonthComboBox.setSelectedItem(String.format("%02d", currentMonth));
        salesDayComboBox.setSelectedItem(String.format("%02d", currentDay));
        styleDateComboBox(salesYearComboBox);
        datePanel.add(salesYearComboBox);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(mediumBlue);
        container.add(datePanel, BorderLayout.WEST);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, datePanel.getPreferredSize().height + 10));

        return container;
    }

    private void styleDateComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(darkBlue);
        comboBox.setForeground(textWhite);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(70, comboBox.getPreferredSize().height));
    }

    private void loadItems() {
        if (this.itemUnitPrices == null) this.itemUnitPrices = new HashMap<>();
        else this.itemUnitPrices.clear();

        if (this.itemFullData == null) this.itemFullData = new HashMap<>();
        else this.itemFullData.clear();

        ItemController itemController = new ItemController();
        List<String> itemsData = itemController.getAll();

        if (itemComboBox == null) {
            System.err.println("itemComboBox is null in loadItems. Cannot populate.");
            return;
        }
        itemComboBox.removeAllItems();
        itemComboBox.addItem("-- Select Item --");

        if (itemsData != null) {
            for (String itemLine : itemsData) {
                String[] parts = itemLine.split(",");
                if (parts.length >= 5) {
                    String itemCode = parts[1];
                    String itemName = parts[2];
                    String displayString = itemCode + " - " + itemName;
                    itemComboBox.addItem(displayString);
                    try {
                        double unitPrice = Double.parseDouble(parts[4]);
                        itemUnitPrices.put(displayString, unitPrice);
                        itemFullData.put(displayString, itemLine);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse unit price for item: " + displayString + " (" + parts[4] + ")");
                    }
                }
            }
        }
    }

    private void calculateAndDisplayNetIncome() {
        if (itemComboBox == null || quantityDisplayField == null || netIncomeField == null || itemUnitPrices == null) {
            return;
        }

        String selectedItemString = (String) itemComboBox.getSelectedItem();

        if (selectedItemString == null || selectedItemString.equals("-- Select Item --") || currentQuantity <= 0) {
            netIncomeField.setText("");
            setFieldValue(netIncomeField, "", NET_INCOME_PLACEHOLDER);
            return;
        }

        Double unitPrice = itemUnitPrices.get(selectedItemString);
        if (unitPrice == null) {
            netIncomeField.setText("Error: Price N/A");
            setFieldValue(netIncomeField, "Error: Price N/A", NET_INCOME_PLACEHOLDER);
            return;
        }

        double netIncome = currentQuantity * unitPrice;
        setFieldValue(netIncomeField, df.format(netIncome), NET_INCOME_PLACEHOLDER);
    }

    private void loadSalesEntryData() {
        if (itemComboBox == null || quantityDisplayField == null || salesDayComboBox == null || unitPriceDisplayField == null) {
            System.err.println("loadSalesEntryData called before UI components are fully initialized.");
            return;
        }
        DailySalesEntryController salesController = new DailySalesEntryController();
        String salesDataString = salesController.getOneWithId(editSalesEntryId);

        if (salesDataString != null) {
            String[] parts = salesDataString.split(",");
            if (parts.length >= 6) {
                String itemCode = parts[1];
                String itemName = parts[2];
                String itemDisplayString = itemCode + " - " + itemName;

                itemComboBox.setSelectedItem(itemDisplayString);

                String salesDateStr = parts[3];
                try {
                    String[] dateParts = salesDateStr.split("-");
                    if (dateParts.length == 3) {
                        salesYearComboBox.setSelectedItem(dateParts[0]);
                        salesMonthComboBox.setSelectedItem(dateParts[1]);
                        salesDayComboBox.setSelectedItem(dateParts[2]);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing sales date: " + salesDateStr + " - " + e.getMessage());
                }

                try {
                    currentQuantity = Integer.parseInt(parts[4]);
                    updateQuantityDisplay();
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing quantity from sales entry: " + parts[5]);
                    currentQuantity = 1;
                    updateQuantityDisplay();
                }
                SwingUtilities.invokeLater(() -> {
                    updateUnitPriceDisplay();
                    calculateAndDisplayNetIncome();
                });

            } else {
                showError("Corrupted sales entry data for ID: " + editSalesEntryId);
            }
        } else {
            showError("Could not load sales entry data for ID: " + editSalesEntryId);
        }
    }

    @Override
    protected void saveAction() {
        clearAllFieldErrors();
        generalErrorLabel.setText(" ");
        generalErrorLabel.setVisible(false);

        boolean isValid = true;

        String selectedItemDisplay = (String) itemComboBox.getSelectedItem();
        if (selectedItemDisplay == null || selectedItemDisplay.equals("-- Select Item --")) {
            generalErrorLabel.setText("Please select an item.");
            generalErrorLabel.setVisible(true);
            isValid = false;
        }

        if (currentQuantity <= 0) {
            showFieldError(quantityDisplayField, "Quantity must be greater than 0.");
            isValid = false;
        }

        if (salesDayComboBox.getSelectedIndex() == -1 ||
                salesMonthComboBox.getSelectedIndex() == -1 ||
                salesYearComboBox.getSelectedIndex() == -1 ||
                salesDayComboBox.getSelectedItem() == null ||
                salesMonthComboBox.getSelectedItem() == null ||
                salesYearComboBox.getSelectedItem() == null ) {
            generalErrorLabel.setText("Please select a valid sales date.");
            generalErrorLabel.setVisible(true);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        String fullItemDataLine = itemFullData.get(selectedItemDisplay);
        if (fullItemDataLine == null) {
            showError("Critical error: Selected item data not found. Cannot save.");
            return;
        }
        String[] itemParts = fullItemDataLine.split(",");
        String itemId = itemParts[0];
        String itemCode = itemParts[1];
        String itemName = itemParts[2];

        String day = (String) salesDayComboBox.getSelectedItem();
        String month = (String) salesMonthComboBox.getSelectedItem();
        String year = (String) salesYearComboBox.getSelectedItem();
        String salesDate = year + "-" + month + "-" + day;

        double netIncomeVal = 0;
        Double unitPrice = itemUnitPrices.get(selectedItemDisplay);
        if (unitPrice != null) {
            netIncomeVal = currentQuantity * unitPrice;
        } else {
            showError("Critical error: Unit price for selected item not found. Cannot save.");
            return;
        }

        DailySalesEntryController salesController = new DailySalesEntryController();
        try {
            if (editSalesEntryId == null) {
                // Parameters for controller: itemID, itemCode, itemName, salesDate, quantity, netIncome
                salesController.addDailySalesEntry(itemCode, itemName, salesDate, currentQuantity, netIncomeVal);
                showSuccess("Daily sales entry added successfully!");
            } else {
                salesController.updateDailySalesEntry(editSalesEntryId, itemCode, itemName, salesDate, currentQuantity, netIncomeVal);
                showSuccess("Daily sales entry updated successfully!");
            }
            dispose();
            refreshParentView();
        } catch (Exception e) {
            showError("Error saving sales entry: " + e.getMessage());
        }
    }

    private void refreshParentView() {
        if (parentFrame instanceof DailySalesEntryView) {
            ((DailySalesEntryView) parentFrame).refreshTable();
        } else {
            System.err.println("Parent frame is not DailySalesEntryView or null.");
        }
    }

    private JTextField findTextFieldInPanel(JPanel containerPanel) {
        if (containerPanel == null) return null;
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout)containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JTextField) return (JTextField) centerComponent;
            if (centerComponent instanceof JPanel) {
                for(Component subComp : ((JPanel)centerComponent).getComponents()){
                    if(subComp instanceof JTextField) return (JTextField) subComp;
                }
            }
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JTextField) return (JTextField) comp;
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JTextField) return (JTextField) subComp;
                }
            }
        }
        System.err.println("Warning: JTextField not found as expected in panel: " + (containerPanel.getName() != null ? containerPanel.getName() : "Unnamed Panel"));
        return null;
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setForeground(textWhite);
        button.setBackground(lightBlue);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(45, 30));
        return button;
    }
}