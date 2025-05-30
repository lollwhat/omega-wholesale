package view.forms;

import controller.DailySalesEntryController;
import controller.StockController;
import controller.ItemController; // Required for unit prices
import view.DailySalesEntryView;
import view.UITheme;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.FocusAdapter; // Not strictly needed if unit price is non-editable and auto-filled
// import java.awt.event.FocusEvent;   // Not strictly needed
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.ArrayList; // For safety if getAllStocks might return null
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDailySalesForm extends BaseForm {
    private JComboBox<String> itemComboBox;
    private JComboBox<String> salesDayComboBox;
    private JComboBox<String> salesMonthComboBox;
    private JComboBox<String> salesYearComboBox;
    private JTextField unitPriceDisplayField; // Displays price from item_details.txt
    private JTextField quantityDisplayField;
    private JButton minusQuantityButton;
    private JButton plusQuantityButton;
    private int currentQuantity = 0;

    private JTextField netIncomeField;
    private JLabel generalErrorLabel;

    private final String editSalesEntryId;

    private final String NET_INCOME_PLACEHOLDER = "Calculated net income";
    private final String UNIT_PRICE_PLACEHOLDER = "0.00"; // For displaying fetched price
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final DecimalFormat priceDf = new DecimalFormat("0.00");

    // Declare maps; will be initialized in createFormPanel before first use
    private Map<String, String> itemCodeMap;
    private Map<String, String> itemNameMap;
    private Map<String, Double> itemUnitPrices; // For storing unit prices fetched via ItemController

    public AddDailySalesForm(JFrame parent) {
        super(parent, "Add New Daily Sales Entry", 580);
        this.editSalesEntryId = null;
    }

    public AddDailySalesForm(JFrame parent, String dbSalesEntryId) {
        super(parent, "Edit Daily Sales Entry", 580);
        this.editSalesEntryId = dbSalesEntryId;
        if (this.editSalesEntryId != null) {
            SwingUtilities.invokeLater(this::loadSalesEntryData);
        }
    }

    @Override
    protected JPanel createFormPanel() {
        // Initialize maps HERE, before they are used by loadItems()
        this.itemCodeMap = new HashMap<>();
        this.itemNameMap = new HashMap<>();
        this.itemUnitPrices = new HashMap<>();

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        itemComboBox = new JComboBox<>();
        JPanel itemPanel = createComboBoxPanel("Select Item:", itemComboBox);

        loadItems(); // Populates itemComboBox and maps

        itemComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedItemString = (String) itemComboBox.getSelectedItem();
                if (selectedItemString != null && !selectedItemString.equals("-- Select Item --")) {
                    if (currentQuantity == 0) currentQuantity = 1;
                    setQuantityButtonsEnabled(true);
                    updateUnitPriceDisplay(); // Update price based on selection
                } else {
                    currentQuantity = 0;
                    setQuantityButtonsEnabled(false);
                    if (unitPriceDisplayField != null) {
                        setFieldValue(unitPriceDisplayField, "", UNIT_PRICE_PLACEHOLDER);
                    }
                }
                updateQuantityDisplay();
                calculateAndDisplayNetIncome();
            }
        });
        formPanel.add(itemPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel datePanelContainer = createSalesDatePanel();
        formPanel.add(datePanelContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel unitPricePanel = createFormFieldPanel("Unit Price:", UNIT_PRICE_PLACEHOLDER);
        unitPriceDisplayField = findTextFieldInPanel(unitPricePanel);
        if (unitPriceDisplayField != null) {
            unitPriceDisplayField.setEditable(false); // Unit price is fetched, not edited by user
            unitPriceDisplayField.setFocusable(false);
            unitPriceDisplayField.setBackground(UITheme.DARK_BLUE); // Display-only style
            unitPriceDisplayField.setForeground(UITheme.TEXT_WHITE);
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

        // Set initial states after all components are initialized
        currentQuantity = 0;
        updateQuantityDisplay();
        setQuantityButtonsEnabled(false);
        if (unitPriceDisplayField != null) {
            setFieldValue(unitPriceDisplayField, "", UNIT_PRICE_PLACEHOLDER);
        }

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
        errorLabel.setForeground(errorRed); // Ensure errorRed is defined in BaseForm or UITheme
        errorLabel.setVisible(false);
        if (errorLabelMap == null) errorLabelMap = new HashMap<>(); // Defensive init
        errorLabelMap.put(quantityDisplayField, errorLabel);
        JPanel errorWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        errorWrapperPanel.setBackground(mediumBlue);
        errorWrapperPanel.add(errorLabel);
        mainPanel.add(errorWrapperPanel, BorderLayout.SOUTH);

        mainPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, mainPanel.getPreferredSize().height + 10));
        return mainPanel;
    }

    private void adjustQuantity(int delta) {
        String selectedItemString = (String) itemComboBox.getSelectedItem();
        if (selectedItemString == null || selectedItemString.equals("-- Select Item --")) {
            currentQuantity = 0;
            updateQuantityDisplay();
            setQuantityButtonsEnabled(false);
            calculateAndDisplayNetIncome();
            return;
        }

        int newQuantity = currentQuantity + delta;
        if (newQuantity >= 1) {
            currentQuantity = newQuantity;
            clearFieldError(quantityDisplayField);
        } else {
            if (currentQuantity > 0) { // Only show error if trying to go below 1 from a valid quantity
                showFieldError(quantityDisplayField, "Min quantity is 1.");
            }
            // Do not change currentQuantity if it would go below 1
            updateQuantityDisplay(); // Refresh display even if quantity not changed
            calculateAndDisplayNetIncome();
            return;
        }
        updateQuantityDisplay();
        calculateAndDisplayNetIncome();
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

    private JPanel createSalesDatePanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datePanel.setBackground(mediumBlue);

        JLabel dateLabel = new JLabel("Sales Date (D/M/Y):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setForeground(textWhite);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        datePanel.add(dateLabel);

        salesDayComboBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) salesDayComboBox.addItem(String.format("%02d", i));
        styleDateComboBox(salesDayComboBox);
        datePanel.add(salesDayComboBox);
        datePanel.add(Box.createRigidArea(new Dimension(5, 0)));

        salesMonthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) salesMonthComboBox.addItem(String.format("%02d", i));
        styleDateComboBox(salesMonthComboBox);
        datePanel.add(salesMonthComboBox);
        datePanel.add(Box.createRigidArea(new Dimension(5, 0)));

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
        if (comboBox == salesYearComboBox) {
            comboBox.setPreferredSize(new Dimension(80, comboBox.getPreferredSize().height));
        } else {
            comboBox.setPreferredSize(new Dimension(60, comboBox.getPreferredSize().height));
        }
    }

    private void loadItems() {
        itemCodeMap.clear();
        itemNameMap.clear();
        itemUnitPrices.clear();

        ItemController itemController = new ItemController();
        StockController stockController = new StockController();
        List<String> stockLines = stockController.getAllStocks();

        if (itemComboBox == null) {
            System.err.println("itemComboBox is null in loadItems. Cannot populate.");
            return;
        }
        itemComboBox.removeAllItems();
        itemComboBox.addItem("-- Select Item --");

        if (stockLines != null) {
            for (String stockLine : stockLines) {
                if (stockLine == null || stockLine.trim().isEmpty()) continue;
                String[] stockParts = stockLine.split(",");
                // **IMPORTANT**: This assumes stock_details.txt format:
                // parts[0]: StockID (e.g., ST001)
                // parts[1]: ItemCode (e.g., LAP001)
                // parts[2]: ItemName (e.g., HP EliteBook G10)
                // parts[3]: ActualNumericStockQuantity (e.g., 189)
                // parts[4]: MinStock, etc.
                // Ensure this structure and index [3] for quantity is correct for YOUR stock_details.txt
                if (stockParts.length > 3) { // Need at least StockID, ItemCode, ItemName, Quantity
                    String itemCode = stockParts[1].trim();
                    String itemName = stockParts[2].trim();
                    int currentStockQty;
                    try {
                        currentStockQty = Integer.parseInt(stockParts[3].trim()); // Read quantity from index 3
                    } catch (NumberFormatException e) {
                        System.err.println("ERROR: Could not parse stock quantity for item code '" + itemCode +
                                "' from stock_details.txt. Value was: '" + stockParts[3] + "'. Line: " + stockLine);
                        System.err.println("       Ensure stock_details.txt has a numeric quantity at the 4th column (index 3).");
                        continue; // Skip this item if quantity is not a number
                    }

                    if (currentStockQty > 0) { // Only list items with available stock
                        String itemDetailsLine = itemController.getOneByItemCode(itemCode);
                        double unitPrice = 0.0;
                        boolean priceFound = false;

                        if (itemDetailsLine != null) {
                            String[] itemDetailsParts = itemDetailsLine.split(",");
                            // Assuming item_details.txt format: ItemID,ItemCode,ItemName,Unit,UnitPrice,...
                            // So unitPrice is at index 4.
                            if (itemDetailsParts.length > 4) {
                                try {
                                    unitPrice = Double.parseDouble(itemDetailsParts[4].trim());
                                    priceFound = true;
                                } catch (NumberFormatException e) {
                                    System.err.println("Could not parse unit price for item code '" + itemCode +
                                            "' from item_details.txt. Value: '" + itemDetailsParts[4] + "'");
                                }
                            } else {
                                System.err.println("Item details for " + itemCode +
                                        " from item_details.txt has insufficient parts for unit price (expected >4 parts).");
                            }
                        } else {
                            System.err.println("No details found in item_details.txt for item code: " + itemCode +
                                    ". Unit price will be unavailable for this item.");
                        }

                        String displayString = itemCode + " - " + itemName;
                        itemComboBox.addItem(displayString);
                        itemCodeMap.put(displayString, itemCode);
                        itemNameMap.put(displayString, itemName);
                        itemUnitPrices.put(displayString, priceFound ? unitPrice : null); // Store null if price couldn't be fetched/parsed
                    }
                } else {
                    System.err.println("Skipping malformed stock line (not enough parts for ID,Code,Name,Qty): " + stockLine);
                }
            }
        }
        // Initial UI state for these fields is now set at the end of createFormPanel()
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

    private void calculateAndDisplayNetIncome() {
        if (itemComboBox == null || quantityDisplayField == null || netIncomeField == null || itemUnitPrices == null) {
            return;
        }

        String selectedItemString = (String) itemComboBox.getSelectedItem();
        if (selectedItemString == null || selectedItemString.equals("-- Select Item --") || currentQuantity <= 0) {
            setFieldValue(netIncomeField, "", NET_INCOME_PLACEHOLDER);
            return;
        }

        Double unitPrice = itemUnitPrices.get(selectedItemString);
        if (unitPrice == null) {
            setFieldValue(netIncomeField, "Price N/A", NET_INCOME_PLACEHOLDER);
            return;
        }

        double netIncome = currentQuantity * unitPrice;
        setFieldValue(netIncomeField, df.format(netIncome), NET_INCOME_PLACEHOLDER);
    }

    private void loadSalesEntryData() {
        if (editSalesEntryId == null) return;
        if (itemComboBox == null) {
            System.err.println("loadSalesEntryData called too early, itemComboBox is null.");
            return;
        }

        System.out.println("INFO: Attempting to load sales entry for editing ID: " + editSalesEntryId);
        DailySalesEntryController salesController = new DailySalesEntryController();
        ItemController itemDetailController = new ItemController();
        String salesDataString = salesController.getOneWithId(editSalesEntryId);

        if (salesDataString != null && !salesDataString.isEmpty()) {
            String[] parts = salesDataString.split(",");
            if (parts.length >= 6) { // dailySalesId,itemCode,itemName,salesDate,quantity,netIncome
                String itemCodeFromFile = parts[1].trim();
                String itemNameFromFile = parts[2].trim();
                String itemDisplayString = itemCodeFromFile + " - " + itemNameFromFile;

                if (!itemCodeMap.containsKey(itemDisplayString)) {
                    itemCodeMap.put(itemDisplayString, itemCodeFromFile);
                    itemNameMap.put(itemDisplayString, itemNameFromFile);
                    String itemDetailsLine = itemDetailController.getOneByItemCode(itemCodeFromFile);
                    if (itemDetailsLine != null) {
                        String[] itemDetailsParts = itemDetailsLine.split(",");
                        if (itemDetailsParts.length > 4) { // unitPrice at index 4
                            try {
                                double historicalPrice = Double.parseDouble(itemDetailsParts[4].trim());
                                itemUnitPrices.put(itemDisplayString, historicalPrice);
                            } catch (NumberFormatException ex) { itemUnitPrices.put(itemDisplayString, null); }
                        } else { itemUnitPrices.put(itemDisplayString, null); }
                    } else { itemUnitPrices.put(itemDisplayString, null); }

                    boolean foundInCombo = false;
                    for(int i=0; i<itemComboBox.getItemCount(); i++){
                        if(itemDisplayString.equals(itemComboBox.getItemAt(i))){
                            foundInCombo = true;
                            break;
                        }
                    }
                    if(!foundInCombo) itemComboBox.addItem(itemDisplayString);
                }
                itemComboBox.setSelectedItem(itemDisplayString);


                String salesDateStr = parts[3].trim();
                try {
                    String[] dateParts = salesDateStr.split("-");
                    if (dateParts.length == 3) {
                        salesYearComboBox.setSelectedItem(dateParts[0]);
                        salesMonthComboBox.setSelectedItem(dateParts[1]);
                        salesDayComboBox.setSelectedItem(dateParts[2]);
                    }
                } catch (Exception e) { System.err.println("Error parsing sales date for editing: " + salesDateStr + " - " + e.getMessage()); }

                try {
                    currentQuantity = Integer.parseInt(parts[4].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing quantity for editing: " + parts[4]);
                    currentQuantity = 1;
                }

                updateQuantityDisplay();
                updateUnitPriceDisplay();
                setQuantityButtonsEnabled(true);
                calculateAndDisplayNetIncome();

            } else { showError("Corrupted sales entry data for ID: " + editSalesEntryId); }
        } else { showError("Could not load sales entry data for ID: " + editSalesEntryId); }
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

        Double unitPriceVal = null;
        if (isValid) {
            unitPriceVal = itemUnitPrices.get(selectedItemDisplay);
            if (unitPriceVal == null) {
                generalErrorLabel.setText("Unit price N/A for " + selectedItemDisplay + ". Cannot save.");
                generalErrorLabel.setVisible(true);
                isValid = false;
            }
        }

        if (currentQuantity <= 0) {
            if(isValid && selectedItemDisplay != null && !selectedItemDisplay.equals("-- Select Item --")) {
                showFieldError(quantityDisplayField, "Quantity must be greater than 0.");
            }
            isValid = false;
        }

        if (salesDayComboBox.getSelectedItem() == null || salesMonthComboBox.getSelectedItem() == null || salesYearComboBox.getSelectedItem() == null ||
                ((String)salesDayComboBox.getSelectedItem()).isEmpty() || ((String)salesMonthComboBox.getSelectedItem()).isEmpty() || ((String)salesYearComboBox.getSelectedItem()).isEmpty() ) {
            if (isValid) {
                generalErrorLabel.setText("Please select a valid sales date.");
                generalErrorLabel.setVisible(true);
            }
            isValid = false;
        }

        if (!isValid) return;

        String itemCode = itemCodeMap.get(selectedItemDisplay);
        String itemName = itemNameMap.get(selectedItemDisplay);

        if (itemCode == null || itemName == null || unitPriceVal == null) {
            showError("Critical error: Item details or price not retrieved. Cannot save.");
            return;
        }

        String day = (String) salesDayComboBox.getSelectedItem();
        String month = (String) salesMonthComboBox.getSelectedItem();
        String year = (String) salesYearComboBox.getSelectedItem();
        String salesDate = year + "-" + month + "-" + day;

        double netIncomeVal = currentQuantity * unitPriceVal;

        StockController stockController = new StockController();
        try {
            if (editSalesEntryId == null) { // Only perform stock check and deduction for NEW sales entries
                String stockLine = stockController.getStockByItemCode(itemCode);
                if (stockLine == null) {
                    showError("Error: Stock details for " + itemCode + " not found (for stock check).");
                    return;
                }
                String[] stockParts = stockLine.split(",");
                // IMPORTANT: Ensure this index [3] is for QUANTITY in your stock_details.txt
                if (stockParts.length <= 3) {
                    showError("Error: Malformed stock data for " + itemCode + ". Cannot verify available stock.");
                    return;
                }
                int availableStock = Integer.parseInt(stockParts[3].trim()); // Assumes quantity is at index 3

                if (availableStock < currentQuantity) {
                    showError("Insufficient stock for " + itemName + ". Available: " + availableStock + ". Required: " + currentQuantity);
                    return;
                }
                boolean stockUpdated = stockController.subtractStockQuantity(itemCode, currentQuantity);
                if (!stockUpdated) {
                    showError("Failed to update stock for " + itemName + ". Sales entry not saved.");
                    return;
                }
            } else {
                System.out.println("INFO: Stock not re-adjusted for edited sales entry " + editSalesEntryId + ".");
            }
        } catch (NumberFormatException e) {
            showError("Error parsing available stock quantity from stock_details.txt: " + e.getMessage());
            return;
        } catch (Exception e) {
            showError("Error during stock operation: " + e.getMessage() + ". Sales entry not saved.");
            return;
        }

        DailySalesEntryController salesController = new DailySalesEntryController();
        try {
            if (editSalesEntryId == null) {
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
        } else if (parentFrame != null) {
            try {
                parentFrame.getClass().getMethod("refreshTable").invoke(parentFrame);
            } catch (Exception e) {
                System.err.println("Parent frame (" + parentFrame.getClass().getName() + ") is not DailySalesEntryView or has no public refreshTable() method.");
            }
        }
    }

    private JTextField findTextFieldInPanel(JPanel containerPanel) {
        if (containerPanel == null) return null;
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout) containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JTextField) return (JTextField) centerComponent;
            if (centerComponent instanceof JPanel) {
                for (Component subComp : ((JPanel) centerComponent).getComponents()) {
                    if (subComp instanceof JTextField) return (JTextField) subComp;
                }
            }
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JTextField) return (JTextField) comp;
            if (comp instanceof JPanel) {
                JTextField nestedField = findTextFieldInPanelRecursively((JPanel) comp);
                if (nestedField != null) return nestedField;
            }
        }
        return null;
    }

    private JTextField findTextFieldInPanelRecursively(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JTextField) {
                return (JTextField) component;
            } else if (component instanceof JPanel) {
                JTextField field = findTextFieldInPanelRecursively((JPanel) component);
                if (field != null) return field;
            }
        }
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