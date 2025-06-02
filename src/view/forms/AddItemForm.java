package view.forms;

import controller.FileController; // Ensure FileController is imported
import controller.ItemController;
import controller.ItemSupplierController;
import controller.SupplierController;
import view.ItemManagementView;
import view.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // For potential use if needed, not strictly in this version

public class AddItemForm extends BaseForm {
    private JTextField itemCodeField;
    private JTextField itemNameField;
    private JTextField unitField;
    private JTextField unitPriceField;
    private JList<String> supplierList;
    private DefaultListModel<String> supplierListModel;
    private JLabel generalErrorLabel;

    private final String editItemId;

    private final String CODE_PLACEHOLDER = "Enter unique item code";
    private final String NAME_PLACEHOLDER = "Enter item name";
    private final String UNIT_PLACEHOLDER = "Enter unit (e.g., kg, pcs)";
    private final String PRICE_PLACEHOLDER = "Enter unit price";

    // Map to store "Display String" -> "Supplier ID"
    private Map<String, String> supplierDisplayToIdMap;
    // Initialized in loadSuppliers or createFormPanel to handle constructor call order

    public AddItemForm(JFrame parent) {
        super(parent, "Add New Item", 600); // Increased height for JList
        this.editItemId = null;
        // supplierDisplayToIdMap will be initialized by loadSuppliers called via createFormPanel
    }

    public AddItemForm(JFrame parent, String dbItemId) {
        super(parent, "Edit Item", 600); // Increased height for JList
        this.editItemId = dbItemId;
        if (this.editItemId != null) {
            // loadEditData is called after the form is constructed and UI elements are available.
            SwingUtilities.invokeLater(this::loadEditData);
        }
    }

    @Override
    protected JPanel createFormFieldPanel(String labelText, String placeholder) {
        JPanel panel = super.createFormFieldPanel(labelText, placeholder);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void loadEditData() {
        System.out.println("AddItemForm: Loading edit data for item ID: " + editItemId);
        if (editItemId != null) {
            // Assumes loadSuppliers() has run via createFormPanel and populated the JList model
            // and supplierDisplayToIdMap.
            loadItemData();
        }
    }

    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        JPanel itemCodeContainer = createFormFieldPanel("Item Code:", CODE_PLACEHOLDER);
        itemCodeField = findTextFieldInPanel(itemCodeContainer);
        formPanel.add(itemCodeContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel itemNameContainer = createFormFieldPanel("Item Name:", NAME_PLACEHOLDER);
        itemNameField = findTextFieldInPanel(itemNameContainer);
        formPanel.add(itemNameContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel unitContainer = createFormFieldPanel("Unit:", UNIT_PLACEHOLDER);
        unitField = findTextFieldInPanel(unitContainer);
        formPanel.add(unitContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel unitPriceContainer = createFormFieldPanel("Unit Price:", PRICE_PLACEHOLDER);
        unitPriceField = findTextFieldInPanel(unitPriceContainer);
        formPanel.add(unitPriceContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel supplierLabel = new JLabel("Suppliers (Ctrl+Click for multiple):");
        supplierLabel.setFont(new Font("Arial", Font.BOLD, 14));
        supplierLabel.setForeground(textWhite);
        supplierLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(supplierLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        // Initialize models and components here before calling loadSuppliers
        supplierListModel = new DefaultListModel<>();
        supplierList = new JList<>(supplierListModel);
        supplierList.setBackground(darkBlue);
        supplierList.setForeground(textWhite);
        supplierList.setSelectionBackground(highlightBlue);
        supplierList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        supplierList.setFont(new Font("Arial", Font.PLAIN, 14));

        // Initialize map here IF loadSuppliers doesn't already do it robustly
        if (this.supplierDisplayToIdMap == null) {
            this.supplierDisplayToIdMap = new HashMap<>();
        }


        JScrollPane supplierScrollPane = new JScrollPane(supplierList);
        supplierScrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));
        supplierScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        supplierScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        supplierScrollPane.getViewport().setBackground(darkBlue);
        supplierScrollPane.setBorder(BorderFactory.createLineBorder(lightBlue));

        formPanel.add(supplierScrollPane);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        loadSuppliers(); // Populate the list and map

        generalErrorLabel = new JLabel(" ");
        generalErrorLabel.setForeground(UITheme.ERROR_RED);
        generalErrorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        generalErrorLabel.setVisible(false);
        generalErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel generalErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        generalErrorPanel.setBackground(mediumBlue);
        generalErrorPanel.add(generalErrorLabel);
        generalErrorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        generalErrorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, generalErrorLabel.getPreferredSize().height + 5));
        formPanel.add(generalErrorPanel);

        formPanel.add(Box.createVerticalGlue());
        return formPanel;
    }

    private JTextField findTextFieldInPanel(JPanel containerPanel) {
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout) containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JTextField) {
                return (JTextField) centerComponent;
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
        System.err.println("AddItemForm Warning: JTextField not found in panel as expected. Review panel structure from BaseForm.createFormFieldPanel.");
        return null;
    }

    private void loadSuppliers() {
        if (supplierListModel == null) {
            System.err.println("AddItemForm.loadSuppliers: supplierListModel is null. UI elements not ready.");
            supplierListModel = new DefaultListModel<>(); // Initialize if somehow still null
        }
//        supplierListModel.clear();

        // Ensure map is initialized (this is the fix for the NPE)
//        if (this.supplierDisplayToIdMap == null) {
//            this.supplierDisplayToIdMap = new HashMap<>();
//        } else {
//            this.supplierDisplayToIdMap.clear();
//        }

        try {
            SupplierController supplierController = new SupplierController();
            // SupplierController's getAll() method should internally ensure the correct
            // static FileController.filePath ("data/supplier_details.txt") is set before reading.
            List<String> suppliersData = supplierController.getAll();

            if (suppliersData != null && !suppliersData.isEmpty()) {
                for (String supplierLine : suppliersData) {
                    String[] parts = supplierLine.split(",");
                    if (parts.length >= 2) {
                        String supplierId = parts[0].trim();
                        String supplierName = parts[1].trim();
                        String displayString = supplierId + " - " + supplierName;
                        supplierListModel.addElement(displayString);
                        this.supplierDisplayToIdMap.put(displayString, supplierId);
                    }
                }
            } else {
                supplierListModel.addElement("No suppliers available");
            }
        } catch (Exception e) {
            System.err.println("Error loading suppliers into JList: " + e.getMessage());
            e.printStackTrace();
            supplierListModel.addElement("Error loading suppliers");
        }
    }

    private void loadItemData() {
        if (editItemId == null || supplierListModel == null || supplierList == null || itemCodeField == null ) {
            System.err.println("AddItemForm.loadItemData: Called prematurely or with null editItemId/UI components.");
            return;
        }
        try {
            ItemController itemController = new ItemController();
            // ItemController.getOneWithId should internally ensure correct static FileController.filePath
            new FileController("data/item_details.txt"); // Be explicit before this read
            String itemDataString = itemController.getOneWithId(editItemId);

            if (itemDataString != null) {
                String[] parts = itemDataString.split(",");
                if (parts.length >= 5) { // ID,Code,Name,Unit,Price
                    setFieldValue(itemCodeField, parts[1], CODE_PLACEHOLDER);
                    setFieldValue(itemNameField, parts[2], NAME_PLACEHOLDER);
                    setFieldValue(unitField, parts[3], UNIT_PLACEHOLDER);
                    setFieldValue(unitPriceField, parts[4], PRICE_PLACEHOLDER);

                    ItemSupplierController itemSupCtrl = new ItemSupplierController();
                    // itemSupCtrl.getSupplierIdsForItem should internally ensure correct static path for item_supplier.txt
                    List<String> currentSupplierIdsForItem = itemSupCtrl.getSupplierIdsForItem(editItemId);

                    if (currentSupplierIdsForItem != null && !currentSupplierIdsForItem.isEmpty()) {
                        List<Integer> indicesToSelect = new ArrayList<>();
                        for (int i = 0; i < supplierListModel.getSize(); i++) {
                            String listEntryDisplayString = supplierListModel.getElementAt(i);
                            String listEntryId = this.supplierDisplayToIdMap.get(listEntryDisplayString); // Use the instance map
                            if (listEntryId != null && currentSupplierIdsForItem.contains(listEntryId)) {
                                indicesToSelect.add(i);
                            }
                        }
                        if (!indicesToSelect.isEmpty()) {
                            supplierList.setSelectedIndices(indicesToSelect.stream().mapToInt(Integer::intValue).toArray());
                        } else {
                            supplierList.clearSelection();
                        }
                    } else {
                        supplierList.clearSelection();
                    }
                } else {
                    showError("Corrupted item data loaded (not enough fields). ID: " + editItemId);
                }
            } else {
                showError("Could not load item data for ID: " + editItemId);
            }
        } catch (Exception e) {
            showError("Error loading item data for edit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void saveAction() {
        clearAllFieldErrors();
        generalErrorLabel.setText(" ");
        generalErrorLabel.setVisible(false);

        boolean isValid = true;
        if (!validateField(itemCodeField, CODE_PLACEHOLDER, "Item Code")) isValid = false;
        if (!validateField(itemNameField, NAME_PLACEHOLDER, "Item Name")) isValid = false;
        if (!validateField(unitField, UNIT_PLACEHOLDER, "Unit")) isValid = false;
        if (!validateNumericField(unitPriceField, PRICE_PLACEHOLDER, "Unit Price")) isValid = false;

        if (!isValid) {
            return;
        }

        String itemCode = getTextFieldValue(itemCodeField, CODE_PLACEHOLDER);
        String itemName = getTextFieldValue(itemNameField, NAME_PLACEHOLDER);
        String unit = getTextFieldValue(unitField, UNIT_PLACEHOLDER);
        String unitPrice = getTextFieldValue(unitPriceField, PRICE_PLACEHOLDER);

        List<String> selectedSupplierDisplayStrings = supplierList.getSelectedValuesList();
        System.out.println("AddItemForm DEBUG: JList getSelectedValuesList() returned: " + selectedSupplierDisplayStrings);

        System.out.println("AddItemForm DEBUG: Content of supplierDisplayToIdMap at the start of saveAction:");
        if (supplierDisplayToIdMap != null && !supplierDisplayToIdMap.isEmpty()) {
            supplierDisplayToIdMap.forEach((key, value) ->
                    System.out.println("  Map_Key: '" + key + "' ==> Map_Value: '" + value + "'")
            );
        } else if (supplierDisplayToIdMap != null && supplierDisplayToIdMap.isEmpty()){
            System.out.println("  supplierDisplayToIdMap is EMPTY.");
        } else {
            System.out.println("  supplierDisplayToIdMap is NULL (this shouldn't happen if loadSuppliers worked).");
        }

        List<String> newSelectedSupplierIds = new ArrayList<>();
        if (selectedSupplierDisplayStrings != null && this.supplierDisplayToIdMap != null) { // Check map not null
            for (String displayString : selectedSupplierDisplayStrings) {
                String id = this.supplierDisplayToIdMap.get(displayString);
                System.out.println("AddItemForm DEBUG: Mapping JList entry '" + displayString + "' to ID: '" + id + "' using supplierDisplayToIdMap");
                if (id != null) {
                    newSelectedSupplierIds.add(id);
                } else {
                    System.err.println("AddItemForm DEBUG: Warning - could not map display string '" + displayString + "' to a supplier ID.");
                }
            }
        }
        System.out.println("AddItemForm DEBUG: Final list of newSelectedSupplierIds before linking: " + newSelectedSupplierIds);

        ItemController itemController = new ItemController();
        ItemSupplierController itemSupplierController = new ItemSupplierController();
        String currentItemEntryId = this.editItemId;

        try {
            System.out.println("AddItemForm: Performing uniqueness check for item code: " + itemCode);
            new FileController("data/item_details.txt");
            String existingItemByCodeData = itemController.getOneByItemCode(itemCode);
            if (existingItemByCodeData != null) {
                String dbIdOfExistingCode = existingItemByCodeData.split(",")[0];
                if (editItemId == null || !dbIdOfExistingCode.equals(editItemId)) {
                    showError("Item code '" + itemCode + "' already exists.");
                    showFieldError(itemCodeField, "This code already exists."); return;
                }
            }

            System.out.println("AddItemForm: Performing uniqueness check for item name: " + itemName);
            new FileController("data/item_details.txt");
            String existingItemByNameData = itemController.getOneByItemName(itemName);
            if (existingItemByNameData != null) {
                String dbIdOfExistingName = existingItemByNameData.split(",")[0];
                if (editItemId == null || !dbIdOfExistingName.equals(editItemId)) {
                    int response = JOptionPane.showConfirmDialog(this, "Item name '" + itemName + "' already exists. Continue?", "Duplicate Item Name", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
                        showFieldError(itemNameField, "Save cancelled."); return;
                    }
                }
            }

            if (editItemId == null) {
                itemController.addItem(itemCode, itemName, unit, unitPrice);
                System.out.println("AddItemForm: Attempting to retrieve newly added item '" + itemCode + "' from item_details.txt");
                new FileController("data/item_details.txt");
                String newItemData = itemController.getOneByItemCode(itemCode);

                if (newItemData == null || newItemData.isEmpty()) {
                    showError("Failed to retrieve newly added item's ID from item_details.txt. Suppliers cannot be linked.");
                    System.err.println("AddItemForm: newItemData is null or empty after getOneByItemCode for itemCode: " + itemCode + ".");
                    new FileController("data/item_details.txt"); List<String> lines = FileController.getFile();
                    System.err.println("DEBUG: Content of item_details.txt (or what FileController read) during ID retrieval:");
                    if(lines != null && !lines.isEmpty()) lines.forEach(System.err::println); else System.err.println("File was empty or null.");
                    return;
                }
                String[] newItemParts = newItemData.split(",");
                if (newItemParts.length == 0 || newItemParts[0].trim().isEmpty() || !newItemParts[0].startsWith("IM")) {
                    showError("Retrieved invalid item ID for newly added item (" + (newItemParts.length > 0 ? newItemParts[0] : "empty") + "). Suppliers cannot be linked.");
                    System.err.println("AddItemForm: Invalid ID retrieved from newItemData: " + newItemData);
                    return;
                }
                currentItemEntryId = newItemParts[0].trim();
                System.out.println("AddItemForm: Successfully retrieved currentItemEntryId: " + currentItemEntryId);
                showSuccess("Item added successfully!");
            } else {
                new FileController("data/item_details.txt");
                itemController.updateItem(editItemId, itemCode, itemName, unit, unitPrice);
                currentItemEntryId = editItemId;
                showSuccess("Item updated successfully!");
            }

            if (currentItemEntryId != null && !currentItemEntryId.isEmpty()) {
                System.out.println("AddItemForm: Processing supplier links for item ID: " + currentItemEntryId + ". Selected supplier IDs for linking: " + newSelectedSupplierIds);
                List<String> previouslyLinkedSupplierIds = new ArrayList<>();
                if (editItemId != null) {
                    new FileController("data/item_supplier.txt");
                    previouslyLinkedSupplierIds = itemSupplierController.getSupplierIdsForItem(currentItemEntryId);
                    System.out.println("AddItemForm: Previously linked suppliers for editing item " + currentItemEntryId + ": " + previouslyLinkedSupplierIds);
                }

                boolean linksChanged = false;
                if (editItemId != null) {
                    for (String oldSupId : previouslyLinkedSupplierIds) {
                        if (!newSelectedSupplierIds.contains(oldSupId)) {
                            System.out.println("AddItemForm: Removing link for item " + currentItemEntryId + " to supplier " + oldSupId);
                            itemSupplierController.removeLink(currentItemEntryId, oldSupId);
                            linksChanged = true;
                        }
                    }
                }
                for (String newSupId : newSelectedSupplierIds) {
                    if (!previouslyLinkedSupplierIds.contains(newSupId)) {
                        System.out.println("AddItemForm: Adding link for item " + currentItemEntryId + " to supplier " + newSupId);
                        itemSupplierController.addLink(currentItemEntryId, newSupId);
                        linksChanged = true;
                    }
                }

                if (linksChanged) {
                    System.out.println("AddItemForm: Supplier links were updated/added for item " + currentItemEntryId);
                } else {
                    System.out.println("AddItemForm: No new supplier links were added or existing links changed for item " + currentItemEntryId + ". (New selections: " + newSelectedSupplierIds.size() + ", Previous links for edit (if any): " + previouslyLinkedSupplierIds.size() + ")");
                }
            } else {
                System.err.println("AddItemForm: currentItemEntryId is null or empty at supplier linking stage for itemCode: " + itemCode + ". Skipping supplier link processing.");
            }

            dispose();
            refreshParentView();

        } catch (IllegalArgumentException iae) {
            showError("Validation Error: " + iae.getMessage());
            iae.printStackTrace();
        } catch (Exception e) {
            showError("Error saving item and/or supplier links: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshParentView() {
        if (parentFrame instanceof ItemManagementView) {
            ((ItemManagementView) parentFrame).refreshTable();
        } else {
            System.err.println("AddItemForm: Parent frame is not ItemManagementView, cannot refresh table.");
        }
    }
}