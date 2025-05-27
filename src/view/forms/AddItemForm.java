package view.forms;

import controller.SupplierController;
import controller.ItemController;

import view.ItemManagementView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import view.UITheme;

public class AddItemForm extends BaseForm {
    private JTextField itemCodeField;
    private JTextField itemNameField;
    private JTextField unitField;
    private JTextField unitPriceField;
    private JComboBox<String> supplierComboBox;
    private JLabel generalErrorLabel;

    private final String editItemId;

    private final String CODE_PLACEHOLDER = "Enter unique item code";
    private final String NAME_PLACEHOLDER = "Enter item name";
    private final String UNIT_PLACEHOLDER = "Enter unit (e.g., kg, pcs)";
    private final String PRICE_PLACEHOLDER = "Enter unit price";

    public AddItemForm(JFrame parent) {
        super(parent, "Add New Item", 550);
        this.editItemId = null;
    }

    public AddItemForm(JFrame parent, String dbItemId) {
        super(parent, "Edit Item", 550);
        this.editItemId = dbItemId;

        if (this.editItemId != null) {
            SwingUtilities.invokeLater(this::loadEditData);
        }
    }

    private void loadEditData() {
        System.out.println("Loading edit data for item ID: " + editItemId);
        if (editItemId != null) {
            loadItemData();
        }
    }


    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));

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

        supplierComboBox = new JComboBox<>();
        JPanel supplierPanel = createComboBoxPanel("Supplier:", supplierComboBox);
        loadSuppliers();
        formPanel.add(supplierPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        generalErrorLabel = new JLabel(" ");
        generalErrorLabel.setForeground(UITheme.ERROR_RED);
        generalErrorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        generalErrorLabel.setVisible(false);

        JPanel generalErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        generalErrorPanel.setBackground(mediumBlue);
        generalErrorPanel.add(generalErrorLabel);
        formPanel.add(generalErrorPanel);
        formPanel.add(Box.createVerticalGlue());

        return formPanel;
    }

    private JTextField findTextFieldInPanel(JPanel containerPanel) {
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JTextField) {
                return (JTextField) comp;
            }
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JTextField) {
                        return (JTextField) subComp;
                    }
                }
            }
        }
        throw new RuntimeException("JTextField not found in panel structure. Review findTextFieldInPanel and BaseForm.createFormFieldPanel.");
    }


    private void loadSuppliers() {
        try {
            SupplierController supplierController = new SupplierController();
            List<String> suppliers = supplierController.getAll();
            supplierComboBox.addItem("-- Select Supplier --");
            if (suppliers != null) {
                for (String supplierLine : suppliers) {
                    String[] supplierData = supplierLine.split(",");
                    if (supplierData.length > 1) {
                        supplierComboBox.addItem(supplierData[0] + " - " + supplierData[1]);
                    }
                }
            } else {
                supplierComboBox.addItem("No suppliers found");
            }
        } catch (Exception e) {
            System.err.println("Error loading suppliers: " + e.getMessage());
            supplierComboBox.removeAllItems();
            supplierComboBox.addItem("Error loading suppliers");
        }
    }

    private void loadItemData() {
        try {
            ItemController itemController = new ItemController();
            String itemData = itemController.getOneWithId(editItemId);
            if (itemData != null) {
                String[] parts = itemData.split(",");
                if (parts.length >= 6) {
                    String originalItemCodeForEdit = parts[1];
                    setFieldValue(itemCodeField, parts[1], CODE_PLACEHOLDER);
                    setFieldValue(itemNameField, parts[2], NAME_PLACEHOLDER);
                    setFieldValue(unitField, parts[3], UNIT_PLACEHOLDER);
                    setFieldValue(unitPriceField, parts[4], PRICE_PLACEHOLDER);

                    String supplierIdFromFile = parts[5];
                    for (int i = 0; i < supplierComboBox.getItemCount(); i++) {
                        String item = supplierComboBox.getItemAt(i);
                        if (item.startsWith(supplierIdFromFile + " - ")) {
                            supplierComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    showError("Corrupted item data loaded for editing.");
                }
            } else {
                showError("Could not load item data for ID: " + editItemId);
            }
        } catch (Exception e) {
            showError("Error loading item data: " + e.getMessage());
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

        String selectedSupplier = (String) supplierComboBox.getSelectedItem();
        if (selectedSupplier == null || selectedSupplier.equals("-- Select Supplier --") ||
                selectedSupplier.contains("Unable to load") || selectedSupplier.contains("No suppliers found") ||
                selectedSupplier.contains("Error loading")) {
            generalErrorLabel.setText("Please select a valid supplier.");
            generalErrorLabel.setVisible(true);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        String itemCode = getTextFieldValue(itemCodeField, CODE_PLACEHOLDER);
        String itemName = getTextFieldValue(itemNameField, NAME_PLACEHOLDER);
        String unit = getTextFieldValue(unitField, UNIT_PLACEHOLDER);
        String unitPrice = getTextFieldValue(unitPriceField, PRICE_PLACEHOLDER);
        String supplierId = selectedSupplier.split(" - ")[0].trim();

        ItemController itemController = new ItemController();

        try {
            String existingItemByCode = itemController.getOneByItemCode(itemCode);
            if (existingItemByCode != null) {
                String[] existingParts = existingItemByCode.split(",");
                String dbIdOfExistingCode = existingParts[0];
                if (!dbIdOfExistingCode.equals(editItemId)) {
                    showError("Item code '" + itemCode + "' already exists. Please use a different code.");
                    showFieldError(itemCodeField, "This code already exists.");
                    return;
                }
            }

            String existingItemByName = itemController.getOneByItemName(itemName);
            if (existingItemByName != null) {
                String[] existingParts = existingItemByName.split(",");
                String dbIdOfExistingName = existingParts[0];
                if (!dbIdOfExistingName.equals(editItemId)) {
                    String message = "Item name '" + itemName + "' already exists. Do you want to proceed with adding/updating?";
                    int response = JOptionPane.showConfirmDialog(this,
                            message,
                            "Duplicate Item Name",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
                        showFieldError(itemNameField, "Save cancelled due to duplicate name.");
                        return;
                    }
                    showFieldError(itemNameField, "Duplicate name (confirmed by user).");
                }
            }

            if (existingItemByName != null && (!existingItemByName.split(",")[0].equals(editItemId))) {
                clearFieldError(itemNameField);
            }


            if (editItemId == null) {
                itemController.addItem(itemCode, itemName, unit, unitPrice, supplierId);
                showSuccess("Item added successfully!");
            } else {
                itemController.updateItem(editItemId, itemCode, itemName, unit, unitPrice, supplierId);
                showSuccess("Item updated successfully!");
            }

            dispose();
            refreshParentView();

        } catch (IllegalArgumentException iae) {
            showError("Validation Error: " + iae.getMessage());
        }
        catch (Exception e) {
            showError("Error saving item: " + e.getMessage());
        }
    }

    private void refreshParentView() {
        if (parentFrame instanceof ItemManagementView) {
            ((ItemManagementView) parentFrame).refreshTable();
        } else {
            System.err.println("Parent frame is not ItemManagementView, cannot refresh table.");
        }
    }
}