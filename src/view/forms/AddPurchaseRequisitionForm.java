// view/forms/AddPurchaseRequisitionForm.java (User's JTable version with NPE and FileController workaround)
package view.forms;

import controller.PurchaseRequisitionController;
import controller.ItemController;
import controller.SupplierController;
import model.PurchaseRequisitionItem;
import view.PurchaseRequisitionView;
import view.UITheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import controller.FileController;

public class AddPurchaseRequisitionForm extends BaseForm {

    // Header Fields
    private JTextArea notesArea;
    private JComboBox<String> supplierComboBox;
    private JComboBox<String> statusComboBox;

    // Item Line Management
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private JButton addItemButton;
    private JButton removeItemButton;

    private Map<String, String> itemFullDataMap;
    private Map<String, Double> itemUnitPriceMap;
    private Map<String, String> supplierIdMap;

    private final String editPrId;
    private PurchaseRequisitionController prController;
    private ItemController itemController;
    private SupplierController supplierControllerInst;

    private static final String[] PR_STATUSES = {"Pending", "Approved", "Rejected", "Cancelled"};
    private static final String NOTES_PLACEHOLDER = "Enter any notes for this PR...";
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");


    public AddPurchaseRequisitionForm(JFrame parent, String prIdToEdit) {
        super(parent, prIdToEdit == null ? "Add New Purchase Requisition" : "Edit Purchase Requisition", 700);
        this.editPrId = prIdToEdit;

        if (this.editPrId != null) {
            SwingUtilities.invokeLater(this::loadPREditData);
        }
    }
    public AddPurchaseRequisitionForm(JFrame parent) {
        this(parent, null);
    }


    private void loadPREditData() {
        // Ensure controllers and maps are initialized if null
        if (this.prController == null) this.prController = new PurchaseRequisitionController();
        if (this.supplierControllerInst == null) this.supplierControllerInst = new SupplierController();
        if (this.itemController == null) this.itemController = new ItemController();
        if (this.itemFullDataMap == null) this.itemFullDataMap = new HashMap<>();
        if (this.itemUnitPriceMap == null) this.itemUnitPriceMap = new HashMap<>();
        if (this.supplierIdMap == null) this.supplierIdMap = new HashMap<>();

        // Re-initialize PR controller to set its file path for FileController
        this.prController = new PurchaseRequisitionController();
        model.PurchaseRequisition pr = prController.getFullPurchaseRequisitionById(editPrId);
        if (pr == null) {
            showError("Could not load Purchase Requisition details for ID: " + editPrId);
            dispose();
            return;
        }

        setTextAreaValue(notesArea, pr.getNotes(), NOTES_PLACEHOLDER);

        String targetSupplierDisplay = null;
        if (pr.getSupplierId() != null && !pr.getSupplierId().isEmpty()) {
            // Re-initialize Supplier controller to set its file path
            this.supplierControllerInst = new SupplierController();
            String supplierData = supplierControllerInst.getOneWithId(pr.getSupplierId());
            if (supplierData != null) {
                String[] parts = supplierData.split(",");
                if (parts.length >= 2) {
                    targetSupplierDisplay = parts[0] + " - " + parts[1];
                } else {
                    targetSupplierDisplay = parts[0];
                }
            }
        }

        if(supplierComboBox.getItemCount() <= 1 && (supplierIdMap == null || supplierIdMap.isEmpty() || (targetSupplierDisplay != null && !supplierIdMap.containsKey(targetSupplierDisplay)) ) ){
            loadSuppliersIntoComboBox(); // This will re-init supplierControllerInst
        }
        if (targetSupplierDisplay != null) {
            supplierComboBox.setSelectedItem(targetSupplierDisplay);
        } else if (supplierComboBox.getItemCount() > 0) {
            supplierComboBox.setSelectedIndex(0);
        }


        if (pr.getStatus() >= 0 && pr.getStatus() < PR_STATUSES.length) {
            statusComboBox.setSelectedItem(PR_STATUSES[pr.getStatus()]);
        } else if (PR_STATUSES.length > 0) {
            statusComboBox.setSelectedIndex(0);
        }

        itemsTableModel.setRowCount(0);
        if (pr.getItems() != null) {
            // Re-initialize Item controller once before the loop to set its file path
            this.itemController = new ItemController();
            for (PurchaseRequisitionItem item : pr.getItems()) {
                String itemDisplay = item.getItemCode() + " - " + item.getItemName();

                if (!itemFullDataMap.containsKey(itemDisplay)) {
                    // File path for itemController is already set by re-init before this loop
                    String fullItemLine = itemController.getOneByItemCode(item.getItemCode());
                    if (fullItemLine != null) {
                        String[] itemParts = fullItemLine.split(",");
                        if (itemParts.length >= 5) {
                            itemFullDataMap.put(itemDisplay, fullItemLine);
                            try {
                                itemUnitPriceMap.put(itemDisplay, Double.parseDouble(itemParts[4]));
                            } catch (NumberFormatException ex) {
                                itemUnitPriceMap.put(itemDisplay, 0.0);
                                System.err.println("Error parsing unit price for loaded item " + itemDisplay + ": " + ex.getMessage());
                            }
                        }
                    } else {
                        String reconstructedItemLine = String.join(",", item.getItemId(), item.getItemCode(), item.getItemName(), "N/A", String.valueOf(item.getPrice()));
                        itemFullDataMap.put(itemDisplay, reconstructedItemLine);
                        itemUnitPriceMap.put(itemDisplay, (double)item.getPrice());
                    }
                }

                // Assuming item.getPrice() from PRItem is the unit price (could be in cents or direct value)
                double unitPriceForDisplay = (double)item.getPrice();
                double totalPriceForDisplay = (double)item.getTotalPrice();

                itemsTableModel.addRow(new Object[]{
                        itemDisplay,
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(unitPriceForDisplay),
                        CURRENCY_FORMAT.format(totalPriceForDisplay)
                });
            }
        }
    }

    @Override
    protected JPanel createFormPanel() {
        // Initialize controllers and maps if null (prevents NPE during super() call)
        if (this.prController == null) this.prController = new PurchaseRequisitionController();
        if (this.itemController == null) this.itemController = new ItemController();
        if (this.supplierControllerInst == null) this.supplierControllerInst = new SupplierController();
        if (this.itemFullDataMap == null) this.itemFullDataMap = new HashMap<>();
        if (this.itemUnitPriceMap == null) this.itemUnitPriceMap = new HashMap<>();
        if (this.supplierIdMap == null) this.supplierIdMap = new HashMap<>();

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel headerLabel = new JLabel("Requisition Details");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(highlightBlue);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(headerLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0,5)));

        supplierComboBox = new JComboBox<>();
        JPanel supplierPanel = createComboBoxPanel("Supplier:", supplierComboBox);
        loadSuppliersIntoComboBox(); // Will re-initialize supplierControllerInst
        supplierPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        supplierPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, supplierPanel.getPreferredSize().height));
        formPanel.add(supplierPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel notesPanel = createFormTextAreaPanel("Notes:", NOTES_PLACEHOLDER, 4);
        notesArea = findTextAreaInPanel(notesPanel);
        notesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        notesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, notesPanel.getPreferredSize().height));
        formPanel.add(notesPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        statusComboBox = new JComboBox<>(PR_STATUSES);
        JPanel statusPanel = createComboBoxPanel("Status:", statusComboBox);
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, statusPanel.getPreferredSize().height));
        formPanel.add(statusPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel itemsSectionLabel = new JLabel("Requested Items");
        itemsSectionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        itemsSectionLabel.setForeground(highlightBlue);
        itemsSectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(itemsSectionLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0,5)));

        String[] itemEntryColumnNames = {"Item (Code - Name)", "Quantity", "Unit Price", "Total Price"};
        itemsTableModel = new DefaultTableModel(itemEntryColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 2;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        setupItemsTable();
        JScrollPane itemsTableScrollPane = new JScrollPane(itemsTable);

        JPanel tableWrapperPanel = new JPanel(new BorderLayout());
        tableWrapperPanel.setBackground(mediumBlue);
        tableWrapperPanel.add(itemsTableScrollPane, BorderLayout.CENTER);
        tableWrapperPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableWrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        formPanel.add(tableWrapperPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel itemActionButtonsPanel = new JPanel();
        itemActionButtonsPanel.setLayout(new BoxLayout(itemActionButtonsPanel, BoxLayout.X_AXIS));
        itemActionButtonsPanel.setBackground(mediumBlue);
        addItemButton = createSmallButton("+");
        removeItemButton = createSmallButton("-");
        addItemButton.addActionListener(e -> addItemRow());
        removeItemButton.addActionListener(e -> removeItemRow());

        itemActionButtonsPanel.add(Box.createHorizontalGlue());
        itemActionButtonsPanel.add(addItemButton);
        itemActionButtonsPanel.add(Box.createRigidArea(new Dimension(5,0)));
        itemActionButtonsPanel.add(removeItemButton);

        itemActionButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        itemActionButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addItemButton.getPreferredSize().height + 10));
        formPanel.add(itemActionButtonsPanel);

        formPanel.add(Box.createVerticalGlue());
        return formPanel;
    }

    private void loadSuppliersIntoComboBox() {
        this.supplierControllerInst = new SupplierController();

        if(this.supplierIdMap == null) this.supplierIdMap = new HashMap<>(); else this.supplierIdMap.clear();

        List<String> suppliersData = supplierControllerInst.getAll();

        if(supplierComboBox == null) supplierComboBox = new JComboBox<>(); else supplierComboBox.removeAllItems();
        supplierComboBox.addItem("-- Select Supplier --");

        if (suppliersData != null) {
            for (String supLine : suppliersData) {
                String[] parts = supLine.split(",");
                if (parts.length >= 2) {
                    String display = parts[0] + " - " + parts[1];
                    supplierComboBox.addItem(display);
                    supplierIdMap.put(display, parts[0]);
                }
            }
        }
    }

    private void setupItemsTable() {
        itemsTable.setRowHeight(25);
        itemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JComboBox<String> itemEditorComboBox = new JComboBox<>();
        // itemController and maps are initialized/re-initialized in loadItemsIntoEditorComboBox
        loadItemsIntoEditorComboBox(itemEditorComboBox);
        itemsTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemEditorComboBox));

        JTextField priceEditorField = new JTextField();
        ((AbstractDocument) priceEditorField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + string + currentText.substring(offset);
                if (newText.matches("^[0-9]*\\.?([0-9]{0,2})?$")) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + (text == null ? "" : text) + currentText.substring(offset + length);
                if (newText.matches("^[0-9]*\\.?([0-9]{0,2})?$") || newText.isEmpty()) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        itemsTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(priceEditorField));


        itemEditorComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, (Component) e.getSource());
                if (table == null || !table.isEditing()) return;

                int editingRow = table.getEditingRow();
                if (editingRow != -1 && editingRow < itemsTableModel.getRowCount()) {
                    String itemDisplay = (String) itemEditorComboBox.getSelectedItem();
                    if (itemDisplay != null && !itemDisplay.equals("-- Select Item --")) {
                        Double unitPrice = itemUnitPriceMap.get(itemDisplay);
                        itemsTableModel.setValueAt(unitPrice != null ? CURRENCY_FORMAT.format(unitPrice) : "0.00", editingRow, 2);

                        Object qtyObj = itemsTableModel.getValueAt(editingRow, 1);
                        if(qtyObj == null || qtyObj.toString().isEmpty() || qtyObj.toString().equals("0") || Integer.parseInt(qtyObj.toString()) == 0){
                            itemsTableModel.setValueAt(1, editingRow, 1);
                        }
                        updateRowTotal(editingRow);
                    } else {
                        itemsTableModel.setValueAt("0.00", editingRow, 2);
                        itemsTableModel.setValueAt("0.00", editingRow, 3);
                    }
                }
            }
        });

        itemsTableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if ((column == 1 || column == 2) && row < itemsTableModel.getRowCount() && row >=0) {
                    updateRowTotal(row);
                }
            }
        });
    }

    private void loadItemsIntoEditorComboBox(JComboBox<String> comboBox) {
        // Re-initialize controller to ensure FileController.filePath is set for items
        this.itemController = new ItemController();

        // Clear maps before loading
        if (this.itemFullDataMap == null) this.itemFullDataMap = new HashMap<>(); else this.itemFullDataMap.clear();
        if (this.itemUnitPriceMap == null) this.itemUnitPriceMap = new HashMap<>(); else this.itemUnitPriceMap.clear();

        List<String> itemsData = itemController.getAll(); // Now reads from the correct item file

        if(comboBox == null) comboBox = new JComboBox<>(); else comboBox.removeAllItems();
        comboBox.addItem("-- Select Item --");
        if (itemsData != null) {
            for (String itemLine : itemsData) {
                String[] parts = itemLine.split(",");
                if (parts.length >= 5) {
                    String display = parts[1] + " - " + parts[2];
                    comboBox.addItem(display);
                    itemFullDataMap.put(display, itemLine);
                    try {
                        itemUnitPriceMap.put(display, Double.parseDouble(parts[4]));
                    } catch (NumberFormatException ex) {
                        itemUnitPriceMap.put(display, 0.0);
                        System.err.println("Error parsing price for item in editor combo: " + display);
                    }
                }
            }
        }
    }

    private void updateRowTotal(int row) {
        if (row < 0 || row >= itemsTableModel.getRowCount()) return;

        try {
            Object itemDisplayObj = itemsTableModel.getValueAt(row, 0);
            Object quantityObj = itemsTableModel.getValueAt(row, 1);
            Object unitPriceObj = itemsTableModel.getValueAt(row, 2);

            if (itemDisplayObj == null || quantityObj == null || unitPriceObj == null ||
                    itemDisplayObj.toString().equals("-- Select Item --") ||
                    quantityObj.toString().trim().isEmpty() ||
                    unitPriceObj.toString().trim().isEmpty()) {
                itemsTableModel.setValueAt(CURRENCY_FORMAT.format(0.0), row, 3);
                return;
            }

            int quantity = Integer.parseInt(quantityObj.toString().trim());
            double unitPrice = Double.parseDouble(unitPriceObj.toString().replace(",", "").trim());

            if (quantity >= 0 && unitPrice >= 0) {
                itemsTableModel.setValueAt(CURRENCY_FORMAT.format(unitPrice * quantity), row, 3);
            } else {
                itemsTableModel.setValueAt(CURRENCY_FORMAT.format(0.0), row, 3);
            }
        } catch (NumberFormatException ex) {
            itemsTableModel.setValueAt("Error", row, 3);
            System.err.println("NFE in updateRowTotal for row " + row + ": " + ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("AIOOBE in updateRowTotal, row: " + row);
        }
    }


    private void addItemRow() {
        itemsTableModel.addRow(new Object[]{"-- Select Item --", 1, "0.00", "0.00"});
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
    protected void saveAction() {
        if (this.prController == null) this.prController = new PurchaseRequisitionController();
        if (this.supplierIdMap == null) this.supplierIdMap = new HashMap<>();
        if (this.itemFullDataMap == null) this.itemFullDataMap = new HashMap<>();

        String notes = getTextAreaValue(notesArea, NOTES_PLACEHOLDER);
        String selectedSupplierDisplay = (String) supplierComboBox.getSelectedItem();
        String selectedStatusString = (String) statusComboBox.getSelectedItem();

        if (selectedSupplierDisplay == null || selectedSupplierDisplay.equals("-- Select Supplier --")) {
            showError("Please select a supplier.");
            return;
        }
        if (selectedStatusString == null) {
            showError("Please select a PR status.");
            return;
        }

        String actualSupplierId = supplierIdMap.get(selectedSupplierDisplay);
        if (actualSupplierId == null) {
            showError("Invalid supplier selection (ID not found).");
            return;
        }

        int statusInt = 0;
        for(int i=0; i < PR_STATUSES.length; i++){
            if(PR_STATUSES[i].equals(selectedStatusString)){
                statusInt = i;
                break;
            }
        }

        List<PurchaseRequisitionItem> prItemsList = new ArrayList<>();
        for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
            String itemDisplay = (String) itemsTableModel.getValueAt(i, 0);
            if (itemDisplay == null || itemDisplay.equals("-- Select Item --")) {
                showError("Row " + (i + 1) + ": Please select an item.");
                return;
            }

            String fullItemLine = itemFullDataMap.get(itemDisplay);
            if (fullItemLine == null) {
                showError("Row " + (i + 1) + ": Corrupted item data for '" + itemDisplay + "'. Please re-select.");
                return;
            }
            String[] itemParts = fullItemLine.split(",");
            String itemId = itemParts[0];
            String itemCode = itemParts[1];
            String itemName = itemParts[2];

            int quantity;
            double unitPriceDbl;
            try {
                quantity = Integer.parseInt(itemsTableModel.getValueAt(i, 1).toString().trim());
                String unitPriceStr = itemsTableModel.getValueAt(i, 2).toString().replace(",", "").trim();
                unitPriceDbl = Double.parseDouble(unitPriceStr);

                if (quantity <= 0) {
                    showError("Row " + (i + 1) + ": Quantity must be greater than 0.");
                    return;
                }
                if (unitPriceDbl < 0) {
                    showError("Row " + (i + 1) + ": Unit Price cannot be negative.");
                    return;
                }
            } catch (NumberFormatException | NullPointerException e) {
                showError("Row " + (i + 1) + ": Invalid quantity or unit price format in table for item '" + itemDisplay + "'.");
                return;
            }

            int priceForPrItemModel = (int) Math.round(unitPriceDbl * 100);
            int totalPriceForPrItemModel = quantity * priceForPrItemModel;

            prItemsList.add(new PurchaseRequisitionItem(null, itemId, itemCode, itemName, quantity, priceForPrItemModel, totalPriceForPrItemModel));
        }

        if (prItemsList.isEmpty()) {
            showError("Please add at least one item to the Purchase Requisition.");
            return;
        }

        try {
            if (editPrId == null) {
                prController.createNewPurchaseRequisition(notes, actualSupplierId, statusInt, prItemsList);
                showSuccess("Purchase Requisition created successfully!");
            } else {
                prController.updatePurchaseRequisition(editPrId, notes, actualSupplierId, statusInt, prItemsList);
                showSuccess("Purchase Requisition updated successfully!");
            }
            dispose();

            if (parentFrame instanceof PurchaseRequisitionView) {
                ((PurchaseRequisitionView) parentFrame).refreshTable();
            } else if (parentFrame instanceof view.PurchaseRequisitionView) {
                ((view.PurchaseRequisitionView) parentFrame).refreshTable();
            } else {
                System.err.println("Parent frame (" + (parentFrame != null ? parentFrame.getClass().getName() : "null") + ") is not of expected type for refresh.");
            }
        } catch (Exception e) {
            showError("Error saving Purchase Requisition: " + e.getMessage());
            e.printStackTrace();
        }
    }

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