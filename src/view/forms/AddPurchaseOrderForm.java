package view.forms;

import controller.*;
import model.PurchaseOrder;
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;
import model.PurchaseOrderItem;
import view.PMPurchaseOrderView; // For refreshing after edit
import view.PMPurchaseRequisitionView; // For refreshing after add from PR
import view.PurchaseOrderView; // For refreshing if this is the parent
import view.UITheme;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AddPurchaseOrderForm extends BaseForm {
    private String editModePoId;
    private PurchaseOrder existingPOForEdit;
    private boolean isEditMode = false;
    private PurchaseRequisition sourcePR;

    private JTextField prIdFieldDisplay;
    private JTextArea poGeneralNotesArea;
    private JTable poItemsTable;
    private DefaultTableModel poItemsTableModel;

    private SupplierController supplierController;
    private PurchaseOrderController purchaseOrderController;
    private ItemController itemController; // Needed for item details if creating directly

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final String PO_GENERAL_NOTES_PLACEHOLDER = "Enter general notes for this PO (optional)...";

    private static final int COL_ITEM_CODE = 0;
    private static final int COL_ITEM_NAME = 1;
    private static final int COL_REQ_QTY = 2; // Relevant for "Add from PR" mode
    private static final int COL_PO_QTY = 3;
    private static final int COL_UNIT_PRICE = 4;
    private static final int COL_CHOSEN_SUPPLIER_PO = 5; // Supplier per item
    private static final int COL_TOTAL_PRICE = 6;
    private static final int COL_SUGGESTED_SUPPLIERS_PR = 7; // Display only from PR (if applicable)
    private static final int COL_ITEM_ENTRY_ID_HIDDEN = 8;

    private List<SupplierDisplayWrapper> allSuppliersForComboBoxCache;

    private static class SupplierDisplayWrapper {
        String id; String display;
        SupplierDisplayWrapper(String id, String display) { this.id = id; this.display = display; }
        public String getId() { return id; }
        @Override public String toString() { return display; }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SupplierDisplayWrapper that = (SupplierDisplayWrapper) obj;
            return Objects.equals(id, that.id); // Use Objects.equals for null safety
        }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    // Constructor for creating PO from a Purchase Requisition
    public AddPurchaseOrderForm(JFrame parent, PurchaseRequisition prData) {
        super(parent, "Create Purchase Order from PR: " + (prData != null ? prData.getPrId() : "N/A"), 780);
        this.sourcePR = prData;
        this.isEditMode = false;
        this.editModePoId = null;
        initializeControllers();
        this.allSuppliersForComboBoxCache = new ArrayList<>();

        SwingUtilities.invokeLater(() -> {
            loadAllSuppliersForComboBoxCache();
            loadPRDataIntoForm();
            setupTableEditor();
        });
    }

    // Constructor for editing an existing Purchase Order
    public AddPurchaseOrderForm(JFrame parent, String poIdToEdit) {
        super(parent, "Edit Purchase Order: " + poIdToEdit, 780);
        this.editModePoId = poIdToEdit;
        this.isEditMode = true;
        this.sourcePR = null;
        initializeControllers();
        this.allSuppliersForComboBoxCache = new ArrayList<>();

        SwingUtilities.invokeLater(() -> {
            loadAllSuppliersForComboBoxCache();
            loadExistingPODataForEdit();
            setupTableEditor();
        });
    }

    private void initializeControllers() {
        this.supplierController = new SupplierController();
        this.purchaseOrderController = new PurchaseOrderController();
        this.itemController = new ItemController();
    }

    private void setupTableEditor() {
        if (poItemsTable != null && poItemsTable.getColumnModel().getColumnCount() > COL_CHOSEN_SUPPLIER_PO) {
            TableColumn supplierColumn = poItemsTable.getColumnModel().getColumn(COL_CHOSEN_SUPPLIER_PO);
            if (allSuppliersForComboBoxCache != null && !allSuppliersForComboBoxCache.isEmpty()) {
                supplierColumn.setCellEditor(new SupplierCellEditorForRow(allSuppliersForComboBoxCache));
            } else {
                System.err.println("AddPurchaseOrderForm: Supplier cache not ready for cell editor setup.");
            }
        }
    }


    private void loadAllSuppliersForComboBoxCache() {
        allSuppliersForComboBoxCache.clear();
        allSuppliersForComboBoxCache.add(new SupplierDisplayWrapper(null, "-- Choose Supplier --"));
        // Ensure FileController path is correctly set if SupplierController relies on it.
        // new FileController("data/supplier_details.txt"); // May not be needed if controller handles its path
        List<String> supplierLines = supplierController.getAll();
        if (supplierLines != null) {
            for (String line : supplierLines) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    allSuppliersForComboBoxCache.add(new SupplierDisplayWrapper(id, id + " - " + name));
                }
            }
        }
    }

    private void loadPRDataIntoForm() {
        if (sourcePR == null) {
            showError("Source Purchase Requisition data is missing.");
            dispose(); return;
        }
        setFieldValue(prIdFieldDisplay, sourcePR.getPrId(), "");
        if (prIdFieldDisplay != null) {
            prIdFieldDisplay.setEditable(false);
            prIdFieldDisplay.setBackground(darkBlue);
        }
        setTextAreaValue(poGeneralNotesArea, sourcePR.getNotes(), PO_GENERAL_NOTES_PLACEHOLDER);
        populatePOItemsTableFromPR();
    }

    private void populatePOItemsTableFromPR() {
        poItemsTableModel.setRowCount(0);
        if (sourcePR != null && sourcePR.getItems() != null && !allSuppliersForComboBoxCache.isEmpty()) {
            for (PurchaseRequisitionItem prItem : sourcePR.getItems()) {
                if (prItem == null) continue;
                double unitPrice = prItem.getPrice() / 100.0;
                int requestedQty = prItem.getQuantity();
                String suggestedSuppliersDisplay = prItem.getSuggestedSupplierIds().isEmpty() ?
                        "None (Choose below)" :
                        prItem.getSuggestedSupplierIds().stream().collect(Collectors.joining(", "));

                SupplierDisplayWrapper initialSupplierForPO = allSuppliersForComboBoxCache.get(0); // Default to "-- Choose --"
                if (!prItem.getSuggestedSupplierIds().isEmpty()) {
                    String firstSuggestedId = prItem.getSuggestedSupplierIds().get(0);
                    for (SupplierDisplayWrapper sdw : allSuppliersForComboBoxCache) {
                        if (sdw.getId() != null && sdw.getId().equals(firstSuggestedId)) {
                            initialSupplierForPO = sdw;
                            break;
                        }
                    }
                }

                poItemsTableModel.addRow(new Object[]{
                        prItem.getItemCode(), prItem.getItemName(), requestedQty, requestedQty,
                        CURRENCY_FORMAT.format(unitPrice),
                        initialSupplierForPO, // Let user confirm or change this
                        CURRENCY_FORMAT.format(unitPrice * requestedQty),
                        suggestedSuppliersDisplay, prItem.getItemId()
                });
            }
        }
    }

    private void loadExistingPODataForEdit() {
        if (!this.isEditMode || this.editModePoId == null) return;

        // Ensure FileController paths for controller operations if they use static paths
        // new FileController(purchaseOrderController.getPoHeaderFilePath());
        // new FileController(purchaseOrderController.getPoItemsFilePath());
        this.existingPOForEdit = purchaseOrderController.getFullPurchaseOrderById(this.editModePoId);

        if (this.existingPOForEdit == null) {
            showError("Could not load Purchase Order details for ID: " + this.editModePoId);
            dispose(); return;
        }

        setTextAreaValue(poGeneralNotesArea, existingPOForEdit.getNotes(), PO_GENERAL_NOTES_PLACEHOLDER);
        if (prIdFieldDisplay != null) {
            setFieldValue(prIdFieldDisplay, existingPOForEdit.getPrId(), "N/A");
            prIdFieldDisplay.setEditable(false);
            prIdFieldDisplay.setBackground(darkBlue);
        }

        poItemsTableModel.setRowCount(0);
        if (existingPOForEdit.getItems() != null) {
            for (PurchaseOrderItem item : existingPOForEdit.getItems()) {
                SupplierDisplayWrapper itemSupplierWrapper = null;
                if (item.getSelectedSupplierId() != null && !item.getSelectedSupplierId().trim().isEmpty()) {
                    for (SupplierDisplayWrapper sdw : allSuppliersForComboBoxCache) {
                        if (sdw.getId() != null && sdw.getId().equals(item.getSelectedSupplierId())) {
                            itemSupplierWrapper = sdw;
                            break;
                        }
                    }
                    if (itemSupplierWrapper == null) { // Supplier not in cache, show ID
                        itemSupplierWrapper = new SupplierDisplayWrapper(item.getSelectedSupplierId(), item.getSelectedSupplierId() + " (Details N/A)");
                    }
                } else { // Fallback if item has no supplier ID (should not happen for valid item)
                    itemSupplierWrapper = allSuppliersForComboBoxCache.isEmpty() ? null : allSuppliersForComboBoxCache.get(0);
                }

                double unitPrice = item.getPrice() / 100.0;
                int poQty = item.getQuantity();
                poItemsTableModel.addRow(new Object[]{
                        item.getItemCode(),
                        item.getItemName(),
                        poQty, // Req Qty (can be same as PO Qty for simplicity in edit mode)
                        poQty, // PO Qty
                        CURRENCY_FORMAT.format(unitPrice),
                        itemSupplierWrapper, // The item's specific supplier
                        CURRENCY_FORMAT.format(item.getTotalPrice() / 100.0), // Use item's total price
                        "N/A", // Suggested from PR not directly applicable here
                        item.getItemId()
                });
            }
        }
    }


    private JTextField findTextFieldInPanel(JPanel containerPanel) {
        if (containerPanel == null) return null;
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout) containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JTextField) return (JTextField) centerComponent;
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JTextField) return (JTextField) comp;
            if (comp instanceof JPanel) {
                // Basic recursive search, careful with complex nested panels
                for(Component sub : ((JPanel) comp).getComponents()){
                    if(sub instanceof JTextField) return (JTextField) sub;
                }
            }
        }
        System.err.println("AddPurchaseOrderForm Warning: JTextField not found in panel: " + containerPanel.getName());
        return null;
    }

    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel prIdPanel = createFormFieldPanel("Associated PR ID (if any):", "N/A");
        prIdFieldDisplay = findTextFieldInPanel(prIdPanel);
        prIdPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(prIdPanel);

        JPanel poNotesPanelContainer = createFormTextAreaPanel("Purchase Order Notes:", PO_GENERAL_NOTES_PLACEHOLDER, 3);
        poGeneralNotesArea = findTextAreaInPanel(poNotesPanelContainer);
        poNotesPanelContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(poNotesPanelContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel itemsLabel = new JLabel("Purchase Order Items:");
        itemsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        itemsLabel.setForeground(textWhite);
        itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(itemsLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        String[] poItemTableColumns = {
                "Item Code", "Name", "Req.Qty", "PO Qty", "Unit Price",
                "Choose Supplier", "Total", "Suggested (PR)", "ItemEntryId" // "Choose Supplier" replaces "Choose PO Supplier"
        };
        poItemsTableModel = new DefaultTableModel(poItemTableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing PO Qty, Unit Price, and Chosen Supplier
                return column == COL_PO_QTY || column == COL_UNIT_PRICE || column == COL_CHOSEN_SUPPLIER_PO;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_CHOSEN_SUPPLIER_PO) return SupplierDisplayWrapper.class;
                return super.getColumnClass(columnIndex);
            }
        };
        poItemsTable = new JTable(poItemsTableModel);
        setupPOItemsTable(); // Includes setting column widths and TableModelListener

        JScrollPane itemsScrollPane = new JScrollPane(poItemsTable);
        itemsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemsScrollPane.setPreferredSize(new Dimension(itemsScrollPane.getPreferredSize().width, 250));
        itemsScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        formPanel.add(itemsScrollPane);
        formPanel.add(Box.createRigidArea(new Dimension(0,10)));
        formPanel.add(Box.createVerticalGlue());
        return formPanel;
    }

    private void setupPOItemsTable() {
        poItemsTable.setRowHeight(28);
        poItemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        // Corrected widths based on column purpose
        poItemsTable.getColumnModel().getColumn(COL_ITEM_CODE).setPreferredWidth(80);
        poItemsTable.getColumnModel().getColumn(COL_ITEM_NAME).setPreferredWidth(180);
        poItemsTable.getColumnModel().getColumn(COL_REQ_QTY).setPreferredWidth(60);
        poItemsTable.getColumnModel().getColumn(COL_PO_QTY).setPreferredWidth(60);
        poItemsTable.getColumnModel().getColumn(COL_UNIT_PRICE).setPreferredWidth(90); // Adjusted for currency
        poItemsTable.getColumnModel().getColumn(COL_CHOSEN_SUPPLIER_PO).setPreferredWidth(200); // For supplier selection
        poItemsTable.getColumnModel().getColumn(COL_TOTAL_PRICE).setPreferredWidth(100); // Adjusted for currency
        poItemsTable.getColumnModel().getColumn(COL_SUGGESTED_SUPPLIERS_PR).setPreferredWidth(150); // If displayed

        TableColumn hiddenColumn = poItemsTable.getColumnModel().getColumn(COL_ITEM_ENTRY_ID_HIDDEN);
        hiddenColumn.setMinWidth(0); hiddenColumn.setMaxWidth(0); hiddenColumn.setWidth(0);

        poItemsTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() != TableModelEvent.ALL_COLUMNS) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if ((column == COL_PO_QTY || column == COL_UNIT_PRICE) && row < poItemsTableModel.getRowCount() && row >=0 ) {
                    updateRowTotalPrice(row);
                }
            }
        });
        // Cell editor for supplier selection is set up in constructor/invokeLater after cache is ready
    }

    class SupplierCellEditorForRow extends DefaultCellEditor {
        private JComboBox<SupplierDisplayWrapper> comboBox;
        private List<SupplierDisplayWrapper> allSuppliersForEditor;

        public SupplierCellEditorForRow(List<SupplierDisplayWrapper> allSuppliers) {
            super(new JComboBox<>());
            this.comboBox = (JComboBox<SupplierDisplayWrapper>) getComponent();
            this.allSuppliersForEditor = allSuppliers;
            this.comboBox.setFont(new Font("Arial", Font.PLAIN, 11));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            comboBox.removeAllItems();
            if (allSuppliersForEditor != null && !allSuppliersForEditor.isEmpty()) {
                allSuppliersForEditor.forEach(comboBox::addItem); // Add all suppliers from cache
            } else {
                comboBox.addItem(new SupplierDisplayWrapper(null, "-- No Suppliers Available --"));
            }

            if (value instanceof SupplierDisplayWrapper) {
                comboBox.setSelectedItem(value);
            } else { // Default to the first option (e.g., "-- Choose Supplier --")
                if (comboBox.getItemCount() > 0) {
                    comboBox.setSelectedIndex(0);
                }
            }
            return comboBox;
        }
    }

    private void updateRowTotalPrice(int row) {
        if (row < 0 || row >= poItemsTableModel.getRowCount()) return;
        try {
            Object poQtyObj = poItemsTableModel.getValueAt(row, COL_PO_QTY);
            Object unitPriceObj = poItemsTableModel.getValueAt(row, COL_UNIT_PRICE);
            String poQtyStr = (poQtyObj != null) ? poQtyObj.toString().trim() : "0";
            String unitPriceStr = (unitPriceObj != null) ? unitPriceObj.toString().replace(CURRENCY_FORMAT.getCurrency().getSymbol(), "").replace(",", "").trim() : "0.00";

            if (poQtyStr.isEmpty()) poQtyStr = "0";
            if (unitPriceStr.isEmpty() || unitPriceStr.equalsIgnoreCase("N/A")) unitPriceStr = "0.00";

            int poQty = Integer.parseInt(poQtyStr);
            double unitPrice = Double.parseDouble(unitPriceStr);

            if (poQty < 0) {
                poQty = 0;
                final int finalPoQty = poQty; // Need final variable for lambda
                SwingUtilities.invokeLater(() -> poItemsTableModel.setValueAt(String.valueOf(finalPoQty), row, COL_PO_QTY));
            }
            poItemsTableModel.setValueAt(CURRENCY_FORMAT.format(unitPrice * poQty), row, COL_TOTAL_PRICE);
        } catch (NumberFormatException ex) {
            poItemsTableModel.setValueAt("Error", row, COL_TOTAL_PRICE);
            System.err.println("Error updating total price for row " + row + ": " + ex.getMessage());
        }
    }

    @Override
    protected void saveAction() {
        String generalPONotes = getTextAreaValue(poGeneralNotesArea, PO_GENERAL_NOTES_PLACEHOLDER);
        if (generalPONotes == null || generalPONotes.equals(PO_GENERAL_NOTES_PLACEHOLDER)) {
            generalPONotes = (sourcePR != null && !isEditMode) ? (sourcePR.getNotes() != null ? sourcePR.getNotes() : "") : (existingPOForEdit != null ? (existingPOForEdit.getNotes() != null ? existingPOForEdit.getNotes() : "") : "");
        }


        List<PurchaseOrderItem> collectedPOItems = new ArrayList<>();
        boolean validationErrorOccurred = false;

        for (int i = 0; i < poItemsTableModel.getRowCount(); i++) {
            int poQty;
            try {
                poQty = Integer.parseInt(poItemsTableModel.getValueAt(i, COL_PO_QTY).toString().trim());
            } catch (NumberFormatException | NullPointerException ex) {
                showError("Row " + (i + 1) + ": PO Quantity must be a valid whole number.");
                validationErrorOccurred = true;
                break;
            }

            Object supplierCellValue = poItemsTableModel.getValueAt(i, COL_CHOSEN_SUPPLIER_PO);
            if (!(supplierCellValue instanceof SupplierDisplayWrapper) || ((SupplierDisplayWrapper) supplierCellValue).getId() == null) {
                showError("Row " + (i + 1) + ": Please choose a supplier for item '" + poItemsTableModel.getValueAt(i, COL_ITEM_NAME) + "'.");
                validationErrorOccurred = true;
                break;
            }
            String chosenSupplierId = ((SupplierDisplayWrapper) supplierCellValue).getId();

            if (poQty <= 0) {
                if (!isEditMode) { // For new POs, qty must be > 0
                    showError("Row " + (i + 1) + ": PO Quantity must be positive for new Purchase Orders.");
                    validationErrorOccurred = true;
                    break;
                }
                // If editing and qty is 0, this item will be effectively removed/skipped.
                continue;
            }

            String itemEntryId = poItemsTableModel.getValueAt(i, COL_ITEM_ENTRY_ID_HIDDEN).toString();
            String itemCode = poItemsTableModel.getValueAt(i, COL_ITEM_CODE).toString();
            String itemName = poItemsTableModel.getValueAt(i, COL_ITEM_NAME).toString();
            String unitPriceStr = poItemsTableModel.getValueAt(i, COL_UNIT_PRICE).toString()
                    .replace(CURRENCY_FORMAT.getCurrency().getSymbol(), "").replace(",", "");
            double unitPriceDouble;
            try {
                unitPriceDouble = Double.parseDouble(unitPriceStr.trim());
                if (unitPriceDouble < 0) {
                    showError("Row " + (i + 1) + ": Unit price for '" + itemName + "' cannot be negative.");
                    validationErrorOccurred = true;
                    break;
                }
            } catch (NumberFormatException ex) {
                showError("Row " + (i + 1) + ": Invalid Unit Price for '" + itemName + "'.");
                validationErrorOccurred = true;
                break;
            }
            int priceInCents = (int) Math.round(unitPriceDouble * 100);

            PurchaseOrderItem poItem = new PurchaseOrderItem(
                    this.editModePoId, // Null for new PO, set by controller; actual ID for edit
                    itemEntryId, itemCode, itemName, poQty, priceInCents, chosenSupplierId // Add supplierId here
            );
            collectedPOItems.add(poItem);
        }

        if (validationErrorOccurred) return;

        if (collectedPOItems.isEmpty() && poItemsTableModel.getRowCount() > 0 && isEditMode) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "All items have zero quantity or were removed. This will result in a PO with no items (effectively cancelling/deleting it). Continue?",
                    "Confirm Empty PO", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.NO_OPTION || confirm == JOptionPane.CLOSED_OPTION) return;
        } else if (collectedPOItems.isEmpty() && !isEditMode){
            showError("Cannot create a Purchase Order with no items.");
            return;
        }


        if (!isEditMode) { // ADD MODE (Creating a single PO with potentially multiple suppliers at item level)
            String prIdForPO = (sourcePR != null) ? sourcePR.getPrId() : "DIRECT_PO"; // Use source PR or placeholder

            // Since a PO can now have items from different suppliers, we create ONE PO header.
            // The items in collectedPOItems already have their individual supplier IDs.
            try {
                // new FileController(purchaseOrderController.getPoHeaderFilePath()); // Path setting for controller if needed
                // new FileController(purchaseOrderController.getPoItemsFilePath());

                // The controller's createPurchaseOrderFromPR method needs to be adapted:
                // It should no longer take a header-level supplierId.
                // It creates one PO and adds all items from collectedPOItems to it.
                PurchaseOrder createdPO = purchaseOrderController.createPurchaseOrderFromPR(
                        prIdForPO,
                        generalPONotes,
                        // supplierIdForPO, // REMOVE THIS PARAMETER FROM CONTROLLER METHOD
                        collectedPOItems); // Pass the list of items, each with its supplierId

                if (createdPO != null) {
                    showSuccess("Purchase Order " + createdPO.getPoId() + " created successfully!");
                    // Refresh the appropriate parent view
                    if (parentFrame instanceof PMPurchaseRequisitionView) {
                        ((PMPurchaseRequisitionView) parentFrame).refreshTable();
                    } else if (parentFrame instanceof PMPurchaseOrderView) {
                        ((PMPurchaseOrderView) parentFrame).refreshTable();
                    } else if (parentFrame instanceof PurchaseOrderView) { // General PO View
                        ((PurchaseOrderView) parentFrame).refreshTable();
                    }
                    dispose();
                } else {
                    showError("Failed to create Purchase Order.");
                }
            } catch (Exception e) {
                showError("Error creating Purchase Order: " + e.getMessage());
                e.printStackTrace();
            }
        } else { // EDIT MODE
            if (existingPOForEdit == null) {
                showError("Cannot save. Original Purchase Order data not loaded for editing.");
                return;
            }

            PurchaseOrder poToUpdate = new PurchaseOrder(
                    this.editModePoId,
                    existingPOForEdit.getPrId(),
                    generalPONotes,
                    // existingPOForEdit.getSelectedSupplierId(), // REMOVE: PO Header no longer has supplierId
                    existingPOForEdit.getStatus(),
                    existingPOForEdit.getCreatedAt(),
                    existingPOForEdit.getCreatedBy(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    SessionController.getInstance().getUserId(),
                    existingPOForEdit.getReceivedAt(),
                    existingPOForEdit.getReceivedBy(),
                    collectedPOItems
            );

            try {
                // new FileController(purchaseOrderController.getPoHeaderFilePath());
                // new FileController(purchaseOrderController.getPoItemsFilePath());
                purchaseOrderController.update(poToUpdate);
                showSuccess("Purchase Order " + this.editModePoId + " updated successfully!");
                dispose();
                if (parentFrame instanceof PMPurchaseOrderView) {
                    ((PMPurchaseOrderView) parentFrame).refreshTable();
                } else if (parentFrame instanceof PurchaseOrderView) {
                    ((PurchaseOrderView) parentFrame).refreshTable();
                }
            } catch (Exception e) {
                showError("Error updating Purchase Order " + this.editModePoId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private JTextArea findTextAreaInPanel(JPanel containerPanel) {
        if (containerPanel == null) return null;
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout)containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JScrollPane) {
                Component view = ((JScrollPane) centerComponent).getViewport().getView();
                if (view instanceof JTextArea) return (JTextArea) view;
            } else if (centerComponent instanceof JTextArea) { // Should not happen with BaseForm structure
                return (JTextArea) centerComponent;
            }
        }
        System.err.println("AddPurchaseOrderForm Warning: JTextArea not found as expected in panel: " + containerPanel.getName());
        return null; // Fallback
    }
}