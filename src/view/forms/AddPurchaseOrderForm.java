package view.forms;

import controller.*;
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;
import model.PurchaseOrderItem;
import view.PMPurchaseRequisitionView; // For refreshing parent view
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AddPurchaseOrderForm extends BaseForm {
    private PurchaseRequisition sourcePR;

    // PR Info (Read-Only)
    private JTextField prIdFieldDisplay;
    private JTextArea prNotesDisplayArea;

    // PO General Fields
    private JTextArea poGeneralNotesArea;

    // PO Items Table
    private JTable poItemsTable;
    private DefaultTableModel poItemsTableModel;

    // Controllers
    private SupplierController supplierController;
    private PurchaseOrderController purchaseOrderController;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final String PO_GENERAL_NOTES_PLACEHOLDER = "Enter general notes for this PO batch (optional)...";
//    private static final String PR_NOTES_DISPLAY_PLACEHOLDER = "PR Notes will appear here.";

    // Column indices for poItemsTable - Keep these updated!
    private static final int COL_ITEM_CODE = 0;
    private static final int COL_ITEM_NAME = 1;
    private static final int COL_REQ_QTY = 2;
    private static final int COL_PO_QTY = 3;
    private static final int COL_UNIT_PRICE = 4;
    private static final int COL_CHOSEN_SUPPLIER_PO = 5; // Editable ComboBox
    private static final int COL_TOTAL_PRICE = 6;
    private static final int COL_SUGGESTED_SUPPLIERS_PR = 7; // Display only from PR
    private static final int COL_ITEM_ENTRY_ID_HIDDEN = 8; // Hidden IMxxx ID

    private List<SupplierDisplayWrapper> allSuppliersForComboBoxCache;

    // Wrapper for JComboBox items to store ID and display text
    private static class SupplierDisplayWrapper {
        String id; String display;
        SupplierDisplayWrapper(String id, String display) { this.id = id; this.display = display; }
        public String getId() { return id; }
        @Override public String toString() { return display; }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SupplierDisplayWrapper that = (SupplierDisplayWrapper) obj;
            return id != null ? id.equals(that.id) : that.id == null;
        }
        @Override public int hashCode() { return id != null ? id.hashCode() : 0; }
    }

    public AddPurchaseOrderForm(JFrame parent, PurchaseRequisition prData) {
        super(parent, "Create Purchase Order(s) from PR: " + (prData != null ? prData.getPrId() : "N/A"), 780);
        this.sourcePR = prData; // Set immediately
        this.allSuppliersForComboBoxCache = new ArrayList<>();

        this.supplierController = new SupplierController();
        this.purchaseOrderController = new PurchaseOrderController();

        // createFormPanel() is called by super's initializeUI().
        // Data loading and cell editor setup is deferred using invokeLater.
        SwingUtilities.invokeLater(() -> {
            loadAllSuppliersForComboBoxCache();
            loadPRDataIntoForm();
            if (poItemsTable != null && poItemsTable.getColumnModel().getColumnCount() > COL_CHOSEN_SUPPLIER_PO) {
                TableColumn supplierColumn = poItemsTable.getColumnModel().getColumn(COL_CHOSEN_SUPPLIER_PO);
                if (allSuppliersForComboBoxCache != null && !allSuppliersForComboBoxCache.isEmpty()) {
                    supplierColumn.setCellEditor(new SupplierCellEditorForRow(allSuppliersForComboBoxCache));
                } else {
                    System.err.println("AddPurchaseOrderForm: allSuppliersForComboBoxCache is not ready for cell editor setup.");
                }
            }
        });
    }

    private void loadAllSuppliersForComboBoxCache() {
        allSuppliersForComboBoxCache.clear();
        allSuppliersForComboBoxCache.add(new SupplierDisplayWrapper(null, "-- Choose Supplier --"));
        new FileController("data/supplier_details.txt"); // Workaround for static FileController
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
        // PR ID display (using JTextField set to non-editable)
        setFieldValue(prIdFieldDisplay, sourcePR.getPrId(), "");
        if (prIdFieldDisplay != null) { // Check if findTextFieldInPanel worked
            prIdFieldDisplay.setEditable(false);
            prIdFieldDisplay.setFocusable(false);
            prIdFieldDisplay.setBackground(darkBlue);
        }

        // PO General Notes (editable, pre-filled with PR notes)
        setTextAreaValue(poGeneralNotesArea, sourcePR.getNotes(), PO_GENERAL_NOTES_PLACEHOLDER);


        populatePOItemsTable();
    }

    private void populatePOItemsTable() {
        poItemsTableModel.setRowCount(0);
        if (sourcePR != null && sourcePR.getItems() != null && allSuppliersForComboBoxCache != null && !allSuppliersForComboBoxCache.isEmpty()) {
            for (PurchaseRequisitionItem prItem : sourcePR.getItems()) {
                if (prItem == null) continue;
                double unitPrice = prItem.getPrice() / 100.0;
                int requestedQty = prItem.getQuantity();
                String suggestedSuppliersDisplay = prItem.getSuggestedSupplierIds().isEmpty() ?
                        "None" :
                        prItem.getSuggestedSupplierIds().stream().collect(Collectors.joining(", "));

                SupplierDisplayWrapper initialSupplierForPO = allSuppliersForComboBoxCache.get(0); // Default to "-- Choose --"
                if (!prItem.getSuggestedSupplierIds().isEmpty()) {
                    String firstSuggestedId = prItem.getSuggestedSupplierIds().get(0);
                    for (SupplierDisplayWrapper sdw : allSuppliersForComboBoxCache) {
                        if (sdw.getId() != null && sdw.getId().equals(firstSuggestedId)) {
                            initialSupplierForPO = sdw; // Pre-select first suggested supplier
                            break;
                        }
                    }
                }

                poItemsTableModel.addRow(new Object[]{
                        prItem.getItemCode(), prItem.getItemName(), requestedQty, requestedQty,
                        CURRENCY_FORMAT.format(unitPrice),
                        initialSupplierForPO,
                        CURRENCY_FORMAT.format(unitPrice * requestedQty),
                        suggestedSuppliersDisplay, prItem.getItemId()
                });
            }
        }
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

    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue); //
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel prIdPanel = createFormFieldPanel("Purchase Requisition ID:", ""); // From BaseForm
        prIdFieldDisplay = findTextFieldInPanel(prIdPanel);       // Your local helper
        prIdPanel.setAlignmentX(Component.LEFT_ALIGNMENT);      // Make the panel align left
        formPanel.add(prIdPanel);

        // --- PO General Notes Section (Editable) ---
        JPanel poNotesPanelContainer = createFormTextAreaPanel("Purchase Order Notes:", PO_GENERAL_NOTES_PLACEHOLDER, 3); // From BaseForm
        poGeneralNotesArea = findTextAreaInPanel(poNotesPanelContainer); // Your local helper
        poNotesPanelContainer.setAlignmentX(Component.LEFT_ALIGNMENT); // Make the panel align left
        formPanel.add(poNotesPanelContainer);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel prInfoLabel = new JLabel("*Please select ONE supplier for each item below");
        prInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        prInfoLabel.setForeground(UITheme.ERROR_RED); //
        prInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(prInfoLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));


        String[] poItemTableColumns = { /* ... as before ... */
                "Item Code", "Name", "Req.Qty", "PO Qty", "Unit Price",
                "Choose PO Supplier", "Total", "Suggested (PR)", "ItemEntryId"
        };
        poItemsTableModel = new DefaultTableModel(poItemTableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COL_PO_QTY || column == COL_UNIT_PRICE || column == COL_CHOSEN_SUPPLIER_PO;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_CHOSEN_SUPPLIER_PO) return SupplierDisplayWrapper.class;
                return super.getColumnClass(columnIndex);
            }
        };
        poItemsTable = new JTable(poItemsTableModel);
        setupPOItemsTable();

        JScrollPane itemsScrollPane = new JScrollPane(poItemsTable);
        itemsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Set a preferred height, and a maximum size that allows width to expand
        itemsScrollPane.setPreferredSize(new Dimension(itemsScrollPane.getPreferredSize().width, 250)); // Keep preferred height
        itemsScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE)); // Allow to expand fully horizontally and vertically if needed

        formPanel.add(itemsScrollPane);
        formPanel.add(Box.createRigidArea(new Dimension(0,10)));

        formPanel.add(Box.createVerticalGlue());
        return formPanel;
    }

    private void setupPOItemsTable() {
        poItemsTable.setRowHeight(28);
        poItemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        poItemsTable.getColumnModel().getColumn(COL_ITEM_CODE).setPreferredWidth(80);
        poItemsTable.getColumnModel().getColumn(COL_ITEM_NAME).setPreferredWidth(180); // Adjusted
        poItemsTable.getColumnModel().getColumn(COL_REQ_QTY).setPreferredWidth(60);
        poItemsTable.getColumnModel().getColumn(COL_PO_QTY).setPreferredWidth(60);
        poItemsTable.getColumnModel().getColumn(COL_UNIT_PRICE).setPreferredWidth(80);
        poItemsTable.getColumnModel().getColumn(COL_CHOSEN_SUPPLIER_PO).setPreferredWidth(220); // Wider for supplier name
        poItemsTable.getColumnModel().getColumn(COL_TOTAL_PRICE).setPreferredWidth(90);
        poItemsTable.getColumnModel().getColumn(COL_SUGGESTED_SUPPLIERS_PR).setPreferredWidth(150);

        TableColumn hiddenColumn = poItemsTable.getColumnModel().getColumn(COL_ITEM_ENTRY_ID_HIDDEN);
        hiddenColumn.setMinWidth(0); hiddenColumn.setMaxWidth(0); hiddenColumn.setWidth(0);

        // Cell editor for "Choose PO Supplier" is set in the constructor's invokeLater block
        // after allSuppliersForComboBoxCache is populated.

        poItemsTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() != TableModelEvent.ALL_COLUMNS) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if ((column == COL_PO_QTY || column == COL_UNIT_PRICE) && row < poItemsTableModel.getRowCount() && row >=0 ) {
                    updateRowTotalPrice(row);
                }
            }
        });
    }

    class SupplierCellEditorForRow extends DefaultCellEditor {
        private JComboBox<SupplierDisplayWrapper> comboBox;
        private List<SupplierDisplayWrapper> allSuppliersForEditor;

        public SupplierCellEditorForRow(List<SupplierDisplayWrapper> allSuppliers) {
            super(new JComboBox<>());
            this.comboBox = (JComboBox<SupplierDisplayWrapper>) getComponent();
            this.allSuppliersForEditor = allSuppliers; // Use the pre-loaded cache
            this.comboBox.setFont(new Font("Arial", Font.PLAIN, 11));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            comboBox.removeAllItems();

            // Add the default "-- Choose Supplier --" option (should be first in allSuppliersForEditor)
            if (allSuppliersForEditor != null && !allSuppliersForEditor.isEmpty()) {
                comboBox.addItem(allSuppliersForEditor.get(0));
            } else { // Fallback if cache is somehow not ready (should not happen)
                comboBox.addItem(new SupplierDisplayWrapper(null, "-- Choose Supplier --"));
            }

            String itemCodeForRow = (String) table.getValueAt(row, COL_ITEM_CODE);
            List<String> suggestedSupplierIdsForRow = new ArrayList<>();
            if (AddPurchaseOrderForm.this.sourcePR != null && AddPurchaseOrderForm.this.sourcePR.getItems() != null) {
                for (PurchaseRequisitionItem prItem : AddPurchaseOrderForm.this.sourcePR.getItems()) {
                    if (prItem.getItemCode().equals(itemCodeForRow) && prItem.getSuggestedSupplierIds() != null) {
                        suggestedSupplierIdsForRow.addAll(prItem.getSuggestedSupplierIds());
                        break;
                    }
                }
            }

            Set<String> addedToCombo = new HashSet<>();
            // Add suggested suppliers first (if they are in the main cache)
            if (!suggestedSupplierIdsForRow.isEmpty() && allSuppliersForEditor != null) {
                for (SupplierDisplayWrapper sdw : allSuppliersForEditor) {
                    if (sdw.getId() != null && suggestedSupplierIdsForRow.contains(sdw.getId())) {
                        comboBox.addItem(sdw);
                        addedToCombo.add(sdw.getId());
                    }
                }
            }
            // Add separator
            if (!suggestedSupplierIdsForRow.isEmpty() && comboBox.getItemCount() > 1 && addedToCombo.size() < (allSuppliersForEditor.stream().filter(s -> s.getId() != null).count()) ) {
                comboBox.addItem(new SupplierDisplayWrapper(null, "--- Other Suppliers ---"));
            }
            // Add other suppliers from cache
            if (allSuppliersForEditor != null) {
                for (SupplierDisplayWrapper sdw : allSuppliersForEditor) {
                    if (sdw.getId() != null && !addedToCombo.contains(sdw.getId())) {
                        comboBox.addItem(sdw);
                    }
                }
            }

            if (value instanceof SupplierDisplayWrapper) {
                comboBox.setSelectedItem(value);
            } else {
                if (allSuppliersForEditor != null && !allSuppliersForEditor.isEmpty()) comboBox.setSelectedItem(allSuppliersForEditor.get(0));
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
            if (unitPriceStr.isEmpty()) unitPriceStr = "0.00";
            int poQty = Integer.parseInt(poQtyStr);
            double unitPrice = Double.parseDouble(unitPriceStr);
            if (poQty < 0) { poQty = 0; final int finalPoQty = poQty; SwingUtilities.invokeLater(() -> poItemsTableModel.setValueAt(finalPoQty, row, COL_PO_QTY));}
            poItemsTableModel.setValueAt(CURRENCY_FORMAT.format(unitPrice * poQty), row, COL_TOTAL_PRICE);
        } catch (NumberFormatException ex) { poItemsTableModel.setValueAt("Price Error", row, COL_TOTAL_PRICE); }
    }

    @Override
    protected void saveAction() {
        String generalPONotes = getTextAreaValue(poGeneralNotesArea, PO_GENERAL_NOTES_PLACEHOLDER);
        if (generalPONotes == null || generalPONotes.equals(PO_GENERAL_NOTES_PLACEHOLDER)) {
            generalPONotes = ""; // Use PR notes if PO notes are empty/placeholder
        }


        Map<String, List<PurchaseOrderItem>> itemsGroupedBySupplier = new HashMap<>();
        for (int i = 0; i < poItemsTableModel.getRowCount(); i++) {
            int poQty;
            try {
                poQty = Integer.parseInt(poItemsTableModel.getValueAt(i, COL_PO_QTY).toString().trim());
            } catch (NumberFormatException | NullPointerException ex) {
                showError("Row " + (i + 1) + ": PO Quantity must be a valid whole number."); return;
            }

            if (poQty <= 0) continue;

            Object supplierCellValue = poItemsTableModel.getValueAt(i, COL_CHOSEN_SUPPLIER_PO);
            if (!(supplierCellValue instanceof SupplierDisplayWrapper) || ((SupplierDisplayWrapper) supplierCellValue).getId() == null) {
                showError("Row " + (i + 1) + ": Please choose a supplier for item '" + poItemsTableModel.getValueAt(i, COL_ITEM_NAME) + "'.");
                return;
            }
            String chosenSupplierId = ((SupplierDisplayWrapper) supplierCellValue).getId();

            try {
                String itemEntryId = poItemsTableModel.getValueAt(i, COL_ITEM_ENTRY_ID_HIDDEN).toString();
                String itemCode = poItemsTableModel.getValueAt(i, COL_ITEM_CODE).toString();
                String itemName = poItemsTableModel.getValueAt(i, COL_ITEM_NAME).toString();
                String unitPriceStr = poItemsTableModel.getValueAt(i, COL_UNIT_PRICE).toString()
                        .replace(CURRENCY_FORMAT.getCurrency().getSymbol(), "").replace(",", "");
                double unitPriceDouble = Double.parseDouble(unitPriceStr.trim());
                int priceInCents = (int) Math.round(unitPriceDouble * 100);
                if (priceInCents < 0) { showError("Row " + (i+1) + ": Unit price for '" + itemName + "' cannot be negative."); return; }

                PurchaseOrderItem poItem = new PurchaseOrderItem(null, itemEntryId, itemCode, itemName, poQty, priceInCents);
                itemsGroupedBySupplier.computeIfAbsent(chosenSupplierId, k -> new ArrayList<>()).add(poItem);
            } catch (NumberFormatException ex) {
                showError("Row " + (i+1) + ": Invalid Unit Price. It must be a valid number."); return;
            } catch (Exception ex) {
                showError("Error processing item at row " + (i+1) + ": " + ex.getMessage()); ex.printStackTrace(); return;
            }
        }

        if (itemsGroupedBySupplier.isEmpty()) {
            showError("No items with quantity > 0 and a chosen supplier. Cannot create Purchase Order(s).");
            return;
        }

        int poCreatedCount = 0; int poFailedCount = 0; StringBuilder errors = new StringBuilder();
        String prIdForPO = (sourcePR != null) ? sourcePR.getPrId() : "N/A_DirectPO";


        for (Map.Entry<String, List<PurchaseOrderItem>> entry : itemsGroupedBySupplier.entrySet()) {
            String supplierIdForPO = entry.getKey();
            List<PurchaseOrderItem> poItemsListForSupplier = entry.getValue();
            try {
                // Ensure FileController paths are set before PO controller operations
                new FileController("data/purchase_order.txt");
                new FileController("data/purchase_order_item.txt");

                model.PurchaseOrder createdPO = purchaseOrderController.createPurchaseOrderFromPR(
                        prIdForPO, generalPONotes, supplierIdForPO, poItemsListForSupplier);
                if (createdPO != null) {
                    System.out.println("Successfully created PO " + createdPO.getPoId() + " for supplier " + supplierIdForPO);
                    poCreatedCount++;
                } else {
                    poFailedCount++; errors.append("PO creation returned null for supplier ").append(supplierIdForPO).append(".\n");
                }
            } catch (Exception e) {
                poFailedCount++; errors.append("Error creating PO for supplier ").append(supplierIdForPO).append(": ").append(e.getMessage()).append("\n");
                e.printStackTrace();
            }
        }

        StringBuilder summaryMessage = new StringBuilder();
        if (poCreatedCount > 0) summaryMessage.append(poCreatedCount).append(" Purchase Order(s) created successfully!\n");
        if (poFailedCount > 0) {
            summaryMessage.append(poFailedCount).append(" Purchase Order(s) failed to create.\n");
            if (errors.length() > 0) summaryMessage.append("Details:\n").append(errors.toString());
            showError(summaryMessage.toString());
        } else if (poCreatedCount > 0) {
            showSuccess(summaryMessage.toString());
        } else {
            showError("No Purchase Orders were processed or created.");
        }

        if (poCreatedCount > 0) {
            if (parentFrame instanceof PMPurchaseRequisitionView) {
                ((PMPurchaseRequisitionView) parentFrame).refreshTable(); // Refresh the PR list
            }
            // TODO: Optionally refresh a main PurchaseOrderView if it exists and is accessible
            dispose();
        }
    }

    // Helper to find JTextArea, using logic from BaseForm if available or adapted
    private JTextArea findTextAreaInPanel(JPanel containerPanel) {
        // User's BaseForm.createFormTextAreaPanel wraps JTextArea in a JScrollPane
        // which is then added to BorderLayout.CENTER of the containerPanel.
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout)containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JScrollPane) {
                Component view = ((JScrollPane) centerComponent).getViewport().getView();
                if (view instanceof JTextArea) return (JTextArea) view;
            }
        }
        System.err.println("AddPurchaseOrderForm Warning: JTextArea not found using expected structure for panel named: " + containerPanel.getName());
        // Fallback scan if needed, but BaseForm structure should be primary target
        for (Component comp : containerPanel.getComponents()) { // Check immediate children
            if (comp instanceof JScrollPane) { Component view = ((JScrollPane) comp).getViewport().getView(); if (view instanceof JTextArea) return (JTextArea) view; }
            if (comp instanceof JTextArea) return (JTextArea) comp;
        }
        return null;
    }

    // Placeholder for setPlaceholder logic if not in BaseForm
    protected void setPlaceholder(JTextArea textArea, String placeholder) {
        if (textArea.getText().isEmpty()) {
            textArea.setText(placeholder);
            textArea.setForeground(Color.GRAY);
        }
        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textArea.getForeground() == Color.GRAY && textArea.getText().equals(placeholder)) {
                    textArea.setText("");
                    textArea.setForeground(textWhite); // Or your default text color
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textArea.getText().isEmpty()) {
                    textArea.setForeground(Color.GRAY);
                    textArea.setText(placeholder);
                }
            }
        });
    }
}