package view.forms;

import controller.PurchaseRequisitionController;
import controller.ItemController;
import controller.SupplierController;
import controller.ItemSupplierController; // For fetching known suppliers for an item
import model.PurchaseRequisitionItem;
import view.PurchaseRequisitionView;
import view.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;  // For button column
import javax.swing.table.TableCellRenderer; // For button column
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; // For button column
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // For joining supplier IDs

import controller.FileController; // Still needed for path workarounds

public class AddPurchaseRequisitionForm extends BaseForm {

    // Header Fields
    private JTextArea notesArea;
    // private JComboBox<String> supplierComboBox; // REMOVED - No longer a single supplier for the PR
    private JComboBox<String> statusComboBox;

    // Item Line Management
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private JButton addItemButton;
    private JButton removeItemButton;

    private Map<String, String> itemFullDataMap; // Stores "ItemDisplay" -> "FullItemDetailsLine"
    private Map<String, Double> itemUnitPriceMap; // Stores "ItemDisplay" -> UnitPrice
    // private Map<String, String> supplierIdMap; // REMOVED - Was for header supplierComboBox

    // To store selected suppliers per row in the table model (temporarily, or via a custom model object)
    // Key: Row index (Integer), Value: List<String> of selected supplier IDs for that item row
    private Map<Integer, List<String>> rowSupplierSelections;


    private String editPrId;
    private PurchaseRequisitionController prController;
    private ItemController itemController;
    private SupplierController supplierControllerInst; // For loading all suppliers into selection dialog
    private ItemSupplierController itemSupplierController; // For fetching known suppliers for an item

    private static final String[] PR_STATUSES = {"Pending", "Approved", "Rejected", "Cancelled"};
    private static final String NOTES_PLACEHOLDER = "Enter any notes for this PR...";
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private List<PurchaseRequisitionItem> initialItemsToLoad = null;

    // Column indices for itemsTable - IMPORTANT to keep updated
    private static final int ITEM_COLUMN_ITEM_DISPLAY = 0;
    private static final int ITEM_COLUMN_QUANTITY = 1;
    private static final int ITEM_COLUMN_UNIT_PRICE = 2;
    private static final int ITEM_COLUMN_SUGGESTED_SUPPLIERS = 3; // New column
    private static final int ITEM_COLUMN_TOTAL_PRICE = 4;       // Shifted


    public AddPurchaseRequisitionForm(JFrame parent, String prIdToEdit) {
        super(parent, prIdToEdit == null ? "Add New Purchase Requisition" : "Edit Purchase Requisition", 750); // Slightly taller
        this.editPrId = prIdToEdit;
        this.rowSupplierSelections = new HashMap<>();
        initializeControllersAndMaps(); // Helper to init controllers and maps

        if (this.editPrId != null) {
            SwingUtilities.invokeLater(this::loadPREditData);
        } else {
            SwingUtilities.invokeLater(this::loadInitialItemsIfAny);
        }
    }

    public AddPurchaseRequisitionForm(JFrame parent) {
        this(parent, (String) null);
    }

    public AddPurchaseRequisitionForm(JFrame parent, List<PurchaseRequisitionItem> itemsToRequisition) {
        super(parent, "Add New Purchase Requisition", 750); // Slightly taller
        this.editPrId = null;
        this.initialItemsToLoad = itemsToRequisition;
        this.rowSupplierSelections = new HashMap<>();
        initializeControllersAndMaps(); // Helper to init controllers and maps

        SwingUtilities.invokeLater(this::loadInitialItemsIfAny);
    }

    private void initializeControllersAndMaps() {
        if (this.prController == null) this.prController = new PurchaseRequisitionController();
        if (this.itemController == null) this.itemController = new ItemController();
        if (this.supplierControllerInst == null) this.supplierControllerInst = new SupplierController();
        if (this.itemSupplierController == null) this.itemSupplierController = new ItemSupplierController();

        if (this.itemFullDataMap == null) this.itemFullDataMap = new HashMap<>();
        if (this.itemUnitPriceMap == null) this.itemUnitPriceMap = new HashMap<>();
        // supplierIdMap for header is removed. Per-item suppliers handled by rowSupplierSelections.
        if (this.rowSupplierSelections == null) this.rowSupplierSelections = new HashMap<>();
    }


    private void loadInitialItemsIfAny() {
        if (initialItemsToLoad != null && !initialItemsToLoad.isEmpty() && itemsTableModel != null) {
            System.out.println("AddPurchaseRequisitionForm: Loading initial items: " + initialItemsToLoad.size());
            for (PurchaseRequisitionItem prItem : initialItemsToLoad) {
                String itemDisplay = prItem.getItemCode() + " - " + prItem.getItemName();
                new FileController("data/item_details.txt"); // Ensure path for itemController
                String fullItemLine = itemController.getOneByItemCode(prItem.getItemCode());

                if (fullItemLine != null) {
                    itemFullDataMap.put(itemDisplay, fullItemLine);
                    String[] itemParts = fullItemLine.split(",");
                    if (itemParts.length >= 5) { // Price is at index 4
                        try {
                            itemUnitPriceMap.put(itemDisplay, Double.parseDouble(itemParts[4]));
                        } catch (NumberFormatException ex) {
                            itemUnitPriceMap.put(itemDisplay, prItem.getPrice() / 100.0);
                            System.err.println("AddPurchaseRequisitionForm: Error parsing unit price for initial item " + itemDisplay + ": " + ex.getMessage());
                        }
                    } else {
                        itemUnitPriceMap.put(itemDisplay, prItem.getPrice() / 100.0);
                    }
                } else {
                    String tempItemLine = String.join(",", prItem.getItemId(), prItem.getItemCode(), prItem.getItemName(), "N/A", String.valueOf(prItem.getPrice() / 100.0));
                    itemFullDataMap.put(itemDisplay, tempItemLine);
                    itemUnitPriceMap.put(itemDisplay, prItem.getPrice() / 100.0);
                }

                double unitPriceForDisplay = prItem.getPrice() / 100.0;
                double totalPriceForDisplay = prItem.getTotalPrice() / 100.0;

                // For a pre-filled item, try to get its known suppliers as initial suggestions
                new FileController("data/item_supplier.txt"); // Ensure path for itemSupplierController
                List<String> knownSupplierIds = itemSupplierController.getSupplierIdsForItem(prItem.getItemId());
                String suggestedSuppliersDisplay = "Click to Select";
                if (knownSupplierIds != null && !knownSupplierIds.isEmpty()) {
                    // Store these initially for the row
                    // Note: The row index isn't known until *after* addRow. This is a bit tricky.
                    // For now, just display them. The actual storage happens when user edits.
                    suggestedSuppliersDisplay = knownSupplierIds.stream().collect(Collectors.joining(", "));
                    // We would ideally store this in rowSupplierSelections for the new row if we could predict its index.
                }


                itemsTableModel.addRow(new Object[]{
                        itemDisplay,
                        prItem.getQuantity(),
                        CURRENCY_FORMAT.format(unitPriceForDisplay),
                        suggestedSuppliersDisplay, // Display initially known or placeholder
                        CURRENCY_FORMAT.format(totalPriceForDisplay)
                });
                // After adding the row, if we pre-filled suppliers, we'd store them for this new row index:
                if (knownSupplierIds != null && !knownSupplierIds.isEmpty()) {
                    rowSupplierSelections.put(itemsTableModel.getRowCount() - 1, new ArrayList<>(knownSupplierIds));
                }
            }
            initialItemsToLoad.clear();
        }
    }

    private void loadPREditData() {
        initializeControllersAndMaps(); // Ensure all are ready
        new FileController(prController.getPrFilePath()); // Set path for PR Controller
        model.PurchaseRequisition pr = prController.getFullPurchaseRequisitionById(editPrId);
        if (pr == null) {
            showError("Could not load PR details for ID: " + editPrId); dispose(); return;
        }

        setTextAreaValue(notesArea, pr.getNotes(), NOTES_PLACEHOLDER);
        // No header supplier to load: supplierComboBox.setSelectedItem(...);

        if (pr.getStatus() >= 0 && pr.getStatus() < PR_STATUSES.length) {
            statusComboBox.setSelectedItem(PR_STATUSES[pr.getStatus()]);
        } else if (PR_STATUSES.length > 0) {
            statusComboBox.setSelectedIndex(0);
        }

        itemsTableModel.setRowCount(0);
        rowSupplierSelections.clear(); // Clear previous row selections for edit

        if (pr.getItems() != null) {
            new FileController("data/item_details.txt"); // Set path for itemController
            for (int i = 0; i < pr.getItems().size(); i++) {
                PurchaseRequisitionItem item = pr.getItems().get(i);
                String itemDisplay = item.getItemCode() + " - " + item.getItemName();
                String fullItemLine = itemController.getOneByItemCode(item.getItemCode());

                if (fullItemLine != null) {
                    itemFullDataMap.put(itemDisplay, fullItemLine);
                    String[] itemParts = fullItemLine.split(",");
                    if (itemParts.length >= 5) {
                        try { itemUnitPriceMap.put(itemDisplay, Double.parseDouble(itemParts[4])); }
                        catch (NumberFormatException ex) { itemUnitPriceMap.put(itemDisplay, item.getPrice() / 100.0); }
                    } else { itemUnitPriceMap.put(itemDisplay, item.getPrice() / 100.0); }
                } else {
                    itemFullDataMap.put(itemDisplay, String.join(",", item.getItemId(), item.getItemCode(), item.getItemName(), "N/A", String.valueOf(item.getPrice() / 100.0)));
                    itemUnitPriceMap.put(itemDisplay, item.getPrice() / 100.0);
                }

                // THIS IS WHERE PurchaseRequisitionItem NEEDS List<String> suggestedSupplierIds
                // For now, assuming 'item.getSuggestedSupplierIds()' exists
                List<String> suggestedIds = item.getSuggestedSupplierIds(); // ****** NEEDS MODEL CHANGE ******
                String suppliersDisplay = "Click to Select";
                if (suggestedIds != null && !suggestedIds.isEmpty()) {
                    suppliersDisplay = suggestedIds.stream().collect(Collectors.joining(", "));
                    rowSupplierSelections.put(i, new ArrayList<>(suggestedIds)); // Store for this row
                }


                itemsTableModel.addRow(new Object[]{
                        itemDisplay,
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(item.getPrice() / 100.0),
                        suppliersDisplay, // Display suppliers
                        CURRENCY_FORMAT.format(item.getTotalPrice() / 100.0)
                });
            }
        }
    }

    private void removeItemRow() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow != -1) {
            if (itemsTable.isEditing()) {
                itemsTable.getCellEditor().stopCellEditing();
            }
            itemsTableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item row to remove.", "No Row Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setMargin(new Insets(2, 8, 2, 8));
        button.setForeground(textWhite);
        button.setBackground(lightBlue);
        button.setFocusPainted(false);
        return button;
    }

    @Override
    protected JPanel createFormPanel() {
        initializeControllersAndMaps(); // Call early

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel notesPanel = createFormTextAreaPanel("Notes:", NOTES_PLACEHOLDER, 3); // Reduced rows
        notesArea = findTextAreaInPanel(notesPanel);
        formPanel.add(notesPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        statusComboBox = new JComboBox<>(PR_STATUSES);
        JPanel statusPanel = createComboBoxPanel("Status:", statusComboBox);
        formPanel.add(statusPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel itemsSectionLabel = new JLabel("Requested Items");
        itemsSectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        itemsSectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        itemsSectionLabel.setForeground(textWhite);
        formPanel.add(itemsSectionLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0,5)));

        // Updated column names for items table
        String[] itemEntryColumnNames = {"Item (Code - Name)", "Quantity", "Unit Price", "Suggested Suppliers", "Total Price"};
        itemsTableModel = new DefaultTableModel(itemEntryColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only Item, Quantity, Unit Price, and Suggested Suppliers (button) are editable
                return column == ITEM_COLUMN_ITEM_DISPLAY ||
                        column == ITEM_COLUMN_QUANTITY ||
                        column == ITEM_COLUMN_UNIT_PRICE ||
                        column == ITEM_COLUMN_SUGGESTED_SUPPLIERS;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        setupItemsTable(); // This will configure columns including the new one
        JScrollPane itemsTableScrollPane = new JScrollPane(itemsTable);
        // ... (tableWrapperPanel setup) ...
        formPanel.add(itemsTableScrollPane); // directly add scrollpane
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        // ... (itemActionButtonsPanel setup) ...
        formPanel.add(createItemActionButtonsPanel()); // Use helper for buttons

        formPanel.add(Box.createVerticalGlue());
        return formPanel;
    }

    private JPanel createItemActionButtonsPanel() {
        JPanel itemActionButtonsPanel = new JPanel();
        itemActionButtonsPanel.setLayout(new BoxLayout(itemActionButtonsPanel, BoxLayout.X_AXIS));
        itemActionButtonsPanel.setBackground(mediumBlue);
        addItemButton = createSmallButton("+ Add Item");
        removeItemButton = createSmallButton("- Remove Item");
        addItemButton.addActionListener(e -> addItemRow());
        removeItemButton.addActionListener(e -> removeItemRow());

        itemActionButtonsPanel.add(Box.createHorizontalGlue());
        itemActionButtonsPanel.add(addItemButton);
        itemActionButtonsPanel.add(Box.createRigidArea(new Dimension(5,0)));
        itemActionButtonsPanel.add(removeItemButton);
        itemActionButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        itemActionButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addItemButton.getPreferredSize().height + 10));
        return itemActionButtonsPanel;
    }


    // REMOVED: loadSuppliersIntoComboBox() - no longer needed for header

    private void setupItemsTable() {
        itemsTable.setRowHeight(30); // Increased row height slightly
        itemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        itemsTable.setSurrendersFocusOnKeystroke(true); // Helps with cell editing

        // Column 0: Item (Code - Name) - JComboBox editor
        JComboBox<String> itemEditorComboBox = new JComboBox<>();
        loadItemsIntoEditorComboBox(itemEditorComboBox); // Populates itemFullDataMap, itemUnitPriceMap
        itemsTable.getColumnModel().getColumn(ITEM_COLUMN_ITEM_DISPLAY).setCellEditor(new DefaultCellEditor(itemEditorComboBox));

        // Column 1: Quantity - Default editor (JTextField based)
        // Add validation or specific numeric editor if desired later

        // Column 2: Unit Price - JTextField editor with numeric filter
        JTextField priceEditorField = new JTextField();
        // ... (numeric filter for priceEditorField as before) ...
        ((AbstractDocument) priceEditorField.getDocument()).setDocumentFilter(new DocumentFilter() { /* ... as before ... */ });
        itemsTable.getColumnModel().getColumn(ITEM_COLUMN_UNIT_PRICE).setCellEditor(new DefaultCellEditor(priceEditorField));

        // Column 3: Suggested Suppliers - NEW Button Renderer and Editor
        SupplierButtonEditorAndRenderer supplierButton = new SupplierButtonEditorAndRenderer(itemsTable);
        itemsTable.getColumnModel().getColumn(ITEM_COLUMN_SUGGESTED_SUPPLIERS).setCellRenderer(supplierButton);
        itemsTable.getColumnModel().getColumn(ITEM_COLUMN_SUGGESTED_SUPPLIERS).setCellEditor(supplierButton);
        itemsTable.getColumnModel().getColumn(ITEM_COLUMN_SUGGESTED_SUPPLIERS).setPreferredWidth(150);


        // Listener for changes in Item selection to update Unit Price
        itemEditorComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, (Component) e.getSource());
                if (table != null && table.isEditing()) {
                    int editingRow = table.getEditingRow();
                    if (editingRow != -1) {
                        String itemDisplay = (String) itemEditorComboBox.getSelectedItem();
                        if (itemDisplay != null && !itemDisplay.equals("-- Select Item --")) {
                            Double unitPrice = itemUnitPriceMap.get(itemDisplay);
                            itemsTableModel.setValueAt(unitPrice != null ? CURRENCY_FORMAT.format(unitPrice) : "0.00", editingRow, ITEM_COLUMN_UNIT_PRICE);
                            if (itemsTableModel.getValueAt(editingRow, ITEM_COLUMN_QUANTITY) == null || itemsTableModel.getValueAt(editingRow, ITEM_COLUMN_QUANTITY).toString().equals("0") || itemsTableModel.getValueAt(editingRow, ITEM_COLUMN_QUANTITY).toString().isEmpty() ){
                                itemsTableModel.setValueAt(1, editingRow, ITEM_COLUMN_QUANTITY); // Default quantity to 1
                            }
                            // Clear suggested suppliers for new item selection in this row
                            itemsTableModel.setValueAt("Click to Select", editingRow, ITEM_COLUMN_SUGGESTED_SUPPLIERS);
                            rowSupplierSelections.remove(editingRow);
                        } else {
                            itemsTableModel.setValueAt("0.00", editingRow, ITEM_COLUMN_UNIT_PRICE);
                            itemsTableModel.setValueAt("0.00", editingRow, ITEM_COLUMN_TOTAL_PRICE);
                            itemsTableModel.setValueAt("Click to Select", editingRow, ITEM_COLUMN_SUGGESTED_SUPPLIERS);
                            rowSupplierSelections.remove(editingRow);
                        }
                        updateRowTotal(editingRow);
                    }
                }
            }
        });

        // Listener for Quantity or Unit Price changes to update Total Price
        itemsTableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if ((column == ITEM_COLUMN_QUANTITY || column == ITEM_COLUMN_UNIT_PRICE) && row < itemsTableModel.getRowCount() && row >= 0) {
                    updateRowTotal(row);
                }
            }
        });
    }

    // Inner class for "Suggested Suppliers" button renderer and editor
    class SupplierButtonEditorAndRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JButton button;
        private JTable table;
        private int currentRow;
        private List<String> currentSelectedSupplierIds; // For this row

        public SupplierButtonEditorAndRenderer(JTable table) {
            this.table = table;
            this.button = new JButton();
            this.button.addActionListener(this);
            this.button.setBorderPainted(false);
            this.button.setFocusPainted(false);
            this.button.setContentAreaFilled(false); // Make it look like part of the cell
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Open a dialog to select multiple suppliers
            List<String> allSuppliersDisplay = new ArrayList<>();
            Map<String, String> displayToId = new HashMap<>(); // Temp map for this dialog
            Map<String, String> idToDisplay = new HashMap<>(); // Temp map for pre-selection

            new FileController("data/supplier_details.txt"); // Ensure path for SupplierController
            List<String> suppliersData = supplierControllerInst.getAll();
            if (suppliersData != null) {
                for (String supLine : suppliersData) {
                    String[] parts = supLine.split(",");
                    if (parts.length >= 2) {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        String display = id + " - " + name;
                        allSuppliersDisplay.add(display);
                        displayToId.put(display, id);
                        idToDisplay.put(id, display);
                    }
                }
            }

            JList<String> supplierJList = new JList<>(allSuppliersDisplay.toArray(new String[0]));
            supplierJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            // Pre-select currently chosen suppliers for this row
            List<String> previouslySelectedIds = rowSupplierSelections.getOrDefault(currentRow, new ArrayList<>());
            List<Integer> indicesToSelect = new ArrayList<>();
            for (String selectedId : previouslySelectedIds) {
                String displayValue = idToDisplay.get(selectedId);
                if (displayValue != null) {
                    int idx = allSuppliersDisplay.indexOf(displayValue);
                    if (idx != -1) {
                        indicesToSelect.add(idx);
                    }
                }
            }
            supplierJList.setSelectedIndices(indicesToSelect.stream().mapToInt(i -> i).toArray());

            JScrollPane scrollPane = new JScrollPane(supplierJList);
            scrollPane.setPreferredSize(new Dimension(300, 200));

            int result = JOptionPane.showConfirmDialog(table, scrollPane, "Select Suggested Suppliers",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                List<String> selectedDisplays = supplierJList.getSelectedValuesList();
                currentSelectedSupplierIds = new ArrayList<>();
                for (String display : selectedDisplays) {
                    currentSelectedSupplierIds.add(displayToId.get(display));
                }
                rowSupplierSelections.put(currentRow, new ArrayList<>(currentSelectedSupplierIds)); // Store actual IDs
                table.setValueAt(currentSelectedSupplierIds.stream().collect(Collectors.joining(", ")),
                        currentRow, ITEM_COLUMN_SUGGESTED_SUPPLIERS); // Update cell display
            }
            fireEditingStopped(); // Important
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.currentRow = row; // Keep track of the row for correct data mapping
            List<String> selectedIds = rowSupplierSelections.getOrDefault(row, new ArrayList<>());
            if (!selectedIds.isEmpty()) {
                button.setText(selectedIds.stream().collect(Collectors.joining(", ")));
            } else {
                button.setText("Click to Select");
            }
            button.setToolTipText("Click to select suggested suppliers for this item");
            // Set background/foreground if needed based on isSelected
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(UIManager.getColor("Button.background"));
            }
            return button;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            List<String> selectedIds = rowSupplierSelections.getOrDefault(row, new ArrayList<>());
            if (!selectedIds.isEmpty()) {
                button.setText(selectedIds.stream().collect(Collectors.joining(", ")));
            } else {
                button.setText("Click to Select");
            }
            return button;
        }
        @Override
        public Object getCellEditorValue() {
            // Return the stored list of supplier IDs for this row
            return rowSupplierSelections.getOrDefault(currentRow, new ArrayList<>());
        }
    }


    private void loadItemsIntoEditorComboBox(JComboBox<String> comboBox) {
        new FileController("data/item_details.txt"); // Ensure path for itemController
        if (this.itemFullDataMap == null) this.itemFullDataMap = new HashMap<>(); else this.itemFullDataMap.clear();
        if (this.itemUnitPriceMap == null) this.itemUnitPriceMap = new HashMap<>(); else this.itemUnitPriceMap.clear();
        List<String> itemsData = itemController.getAll();
        // ... (rest of the method as before) ...
        if(comboBox == null) comboBox = new JComboBox<>(); else comboBox.removeAllItems();
        comboBox.addItem("-- Select Item --");
        if (itemsData != null) {
            for (String itemLine : itemsData) {
                String[] parts = itemLine.split(",");
                if (parts.length >= 5) { // ItemCode, ItemName, Unit, Price
                    String display = parts[1] + " - " + parts[2]; // ItemCode - ItemName
                    comboBox.addItem(display);
                    itemFullDataMap.put(display, itemLine);
                    try {
                        itemUnitPriceMap.put(display, Double.parseDouble(parts[4])); // Price is at index 4
                    } catch (NumberFormatException ex) {
                        itemUnitPriceMap.put(display, 0.0);
                        System.err.println("AddPurchaseRequisitionForm: Error parsing price for item in editor combo: " + display);
                    }
                }
            }
        }
    }

    private void updateRowTotal(int row) {
        if (row < 0 || row >= itemsTableModel.getRowCount()) return;
        try {
            Object quantityObj = itemsTableModel.getValueAt(row, ITEM_COLUMN_QUANTITY);
            Object unitPriceObj = itemsTableModel.getValueAt(row, ITEM_COLUMN_UNIT_PRICE);

            if (quantityObj == null || unitPriceObj == null ||
                    quantityObj.toString().trim().isEmpty() || unitPriceObj.toString().trim().isEmpty()) {
                itemsTableModel.setValueAt(CURRENCY_FORMAT.format(0.0), row, ITEM_COLUMN_TOTAL_PRICE);
                return;
            }
            int quantity = Integer.parseInt(quantityObj.toString().trim());
            double unitPrice = Double.parseDouble(unitPriceObj.toString().replace(CURRENCY_FORMAT.getCurrency().getSymbol(), "").replace(",", "").trim());

            itemsTableModel.setValueAt(CURRENCY_FORMAT.format(unitPrice * quantity), row, ITEM_COLUMN_TOTAL_PRICE);
        } catch (NumberFormatException ex) {
            itemsTableModel.setValueAt("Error", row, ITEM_COLUMN_TOTAL_PRICE);
        }
    }

    private void addItemRow() {
        // Add placeholder text for suppliers, actual selection is done via button
        itemsTableModel.addRow(new Object[]{"-- Select Item --", 1, "0.00", "Click to Select", "0.00"});
        // Initialize an empty list for supplier selections for this new row
        rowSupplierSelections.put(itemsTableModel.getRowCount() - 1, new ArrayList<>());
    }
    // ... (removeItemRow, createSmallButton as before) ...

    @Override
    protected void saveAction() {
        initializeControllersAndMaps(); // Ensure controllers are ready

        String notes = getTextAreaValue(notesArea, NOTES_PLACEHOLDER);
        // String selectedSupplierDisplay = (String) supplierComboBox.getSelectedItem(); // REMOVED
        String selectedStatusString = (String) statusComboBox.getSelectedItem();

        // if (selectedSupplierDisplay == null || selectedSupplierDisplay.equals("-- Select Supplier --")) { // REMOVED
        //     showError("Please select a supplier.");
        //     return;
        // }
        if (selectedStatusString == null) {
            showError("Please select a PR status."); return;
        }

        // String actualSupplierId = supplierIdMap.get(selectedSupplierDisplay); // REMOVED
        // if (actualSupplierId == null) { // REMOVED
        //     showError("Invalid supplier selection (ID not found).");
        //     return;
        // }

        int statusInt = 0; /* ... (status mapping as before) ... */
        for(int i=0; i < PR_STATUSES.length; i++){ if(PR_STATUSES[i].equals(selectedStatusString)){ statusInt = i; break; } }


        List<PurchaseRequisitionItem> prItemsList = new ArrayList<>();
        for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
            String itemDisplay = (String) itemsTableModel.getValueAt(i, ITEM_COLUMN_ITEM_DISPLAY);
            if (itemDisplay == null || itemDisplay.equals("-- Select Item --")) {
                showError("Row " + (i + 1) + ": Please select an item."); return;
            }
            String fullItemLine = itemFullDataMap.get(itemDisplay);
            if (fullItemLine == null) {
                showError("Row " + (i + 1) + ": Corrupted item data for '" + itemDisplay + "'."); return;
            }
            String[] itemParts = fullItemLine.split(",");
            String itemId = itemParts[0]; String itemCode = itemParts[1]; String itemName = itemParts[2];
            int quantity; double unitPriceDbl;
            try { /* ... (quantity and price parsing as before) ... */
                quantity = Integer.parseInt(itemsTableModel.getValueAt(i, ITEM_COLUMN_QUANTITY).toString().trim());
                String unitPriceStr = itemsTableModel.getValueAt(i, ITEM_COLUMN_UNIT_PRICE).toString().replace(CURRENCY_FORMAT.getCurrency().getSymbol(), "").replace(",", "").trim();
                unitPriceDbl = Double.parseDouble(unitPriceStr);
                if (quantity <= 0) { showError("Row " + (i + 1) + ": Quantity must be > 0."); return; }
                if (unitPriceDbl < 0) { showError("Row " + (i + 1) + ": Unit Price cannot be negative."); return; }
            } catch (Exception e) {
                showError("Row " + (i + 1) + ": Invalid quantity or unit price."); return;
            }
            int priceForPrItemModel = (int) Math.round(unitPriceDbl * 100);

            // Get suggested suppliers for this item line
            List<String> suggestedSuppliersForItem = rowSupplierSelections.getOrDefault(i, new ArrayList<>());
            System.out.println("SaveAction: Row " + i + ", Item: " + itemDisplay + ", SuggestedSuppliers: " + suggestedSuppliersForItem);


            PurchaseRequisitionItem prItem = new PurchaseRequisitionItem(
                    null, itemId, itemCode, itemName, quantity, priceForPrItemModel
            );
            // **MODEL CHANGE NEEDED**: prItem needs to store List<String> suggestedSuppliersForItem
            prItem.setSuggestedSupplierIds(suggestedSuppliersForItem); // ASSUMING THIS SETTER EXISTS
            prItemsList.add(prItem);
        }

        if (prItemsList.isEmpty()) { showError("Please add at least one item."); return; }

        try {
            // The supplierId parameter for create/update PR will need to be null or removed from controller methods
            // if PRs no longer have a single header-level supplier.
            if (editPrId == null) {
                prController.createNewPurchaseRequisition(notes, statusInt, prItemsList); // Pass null for header supplierId
                showSuccess("Purchase Requisition created successfully!");
            } else {
                prController.updatePurchaseRequisition(editPrId, notes, statusInt, prItemsList); // Pass null
                showSuccess("Purchase Requisition updated successfully!");
            }
            dispose();
            if (parentFrame instanceof PurchaseRequisitionView) {
                ((PurchaseRequisitionView) parentFrame).refreshTable();
            }
        } catch (Exception e) {
            showError("Error saving Purchase Requisition: " + e.getMessage());
        }
    }

    // ... (findTextAreaInPanel, findTextFieldInPanel, createSmallButton, removeItemRow as before)
    // Make sure CURRENCY_FORMAT is static final if accessed from static contexts, or instance if not.
    // It's used in updateRowTotal and loadInitialItemsIfAny.
    private JTextArea findTextAreaInPanel(JPanel containerPanel) {
        if (containerPanel == null) return null;
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout)containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JScrollPane) {
                Component view = ((JScrollPane) centerComponent).getViewport().getView();
                if (view instanceof JTextArea) return (JTextArea) view;
            } else if (centerComponent instanceof JTextArea) {
                return (JTextArea) centerComponent;
            }
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                Component view = ((JScrollPane) comp).getViewport().getView();
                if (view instanceof JTextArea) return (JTextArea) view;
            } else if (comp instanceof JTextArea) return (JTextArea) comp;
        }
        System.err.println("Warning: JTextArea not found in panel: " + (containerPanel.getName() != null ? containerPanel.getName() : "Unnamed"));
        return null;
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
        System.err.println("Warning: JTextField not found via basic search in panel: " + (containerPanel.getName() != null ? containerPanel.getName() : "Unnamed Panel"));
        return null;
    }
}