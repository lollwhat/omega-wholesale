package view;

import controller.PurchaseRequisitionController;
import controller.SupplierController;
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;
import util.table.GenericModelHelper;
import util.table.mappers.PurchaseRequisitionRowMapper;
import view.forms.AddPurchaseRequisitionForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class PurchaseRequisitionView extends JFrame {
    private final Color darkBlue = UITheme.DARK_BLUE;
    private final Color mediumBlue = UITheme.MEDIUM_BLUE;
    private final Color lightBlue = UITheme.LIGHT_BLUE;
    protected final Color verylightBlue = UITheme.VERY_LIGHT_BLUE;
    private final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    private final Color textWhite = UITheme.TEXT_WHITE;

    private JTable prTable;
    private DefaultTableModel prTableModel;
    private PurchaseRequisitionController prController;
    private PurchaseRequisitionRowMapper prRowMapper;
    private SupplierController supplierController;

    private final String[] prColumnNames = {
            "PR ID", "Notes", "Supplier", "Status",
            "Created At", "Created By", "Updated At", "Updated By", "Actions"
    };

    public PurchaseRequisitionView() {
        prController = new PurchaseRequisitionController();
        prRowMapper = new PurchaseRequisitionRowMapper();
        supplierController = new SupplierController();

        setLayout(new BorderLayout());
        add(createPurchaseRequisitionManagementPanel(), BorderLayout.CENTER);
    }

    public JPanel createPurchaseRequisitionManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(mediumBlue);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Create Purchase Requisition");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(highlightBlue);

        JButton addPRButton = createActionButton("+ New Purchase Requisition", _ -> showAddPurchaseRequisitionForm(null));
        addPRButton.setFont(new Font("Arial", Font.BOLD, 14));
        addPRButton.setBackground(highlightBlue);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(mediumBlue);
        titlePanel.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.add(addPRButton);

        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(darkBlue);
        tablePanel.add(createPRTable(), BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        refreshTable();
        return mainPanel;
    }

    private JFrame getParentFrame() {
        Component parentComponent = this;
        while (parentComponent != null && !(parentComponent instanceof JFrame)) {
            parentComponent = parentComponent.getParent();
        }
        return (JFrame) parentComponent;
    }

    private void showAddPurchaseRequisitionForm(String prIdToEdit) {
        JFrame parentFrame = getParentFrame(); // Get the actual top-level frame
        AddPurchaseRequisitionForm addPRForm;
        if (prIdToEdit == null) {
            addPRForm = new AddPurchaseRequisitionForm(parentFrame);
        } else {
            addPRForm = new AddPurchaseRequisitionForm(parentFrame, prIdToEdit);
        }
        addPRForm.setVisible(true);
    }

    public void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            if (prTableModel == null || prController == null || prRowMapper == null) {
                System.err.println("PurchaseRequisitionManagementView.refreshTable(): Components not initialized.");
                if (prController == null) prController = new PurchaseRequisitionController();
                if (prRowMapper == null) {
                    prRowMapper = new PurchaseRequisitionRowMapper();
                    if (supplierController == null) supplierController = new SupplierController();
                }
                if (prTableModel == null && prTable != null && prTable.getModel() instanceof DefaultTableModel) {
                    prTableModel = (DefaultTableModel) prTable.getModel();
                } else if (prTableModel == null) {
                    System.err.println("PRTableModel is null. Refresh aborted."); return;
                }
            }

            prTableModel.setRowCount(0);
            List<PurchaseRequisition> prHeaders = prController.getAllPurchaseRequisitionHeaders();

            if (prHeaders != null && !prHeaders.isEmpty()) {
                for (PurchaseRequisition prHeaderObj : prHeaders) {
                    if (prHeaderObj == null) continue;
                    String headerCsvLine = prHeaderObj.toCSV();
                    String[] fields = headerCsvLine.split(",");
                    Object[] rowData = prRowMapper.mapFieldsToRow(fields, prTableModel.getColumnCount());
                    if (rowData != null) {
                        prTableModel.addRow(rowData);
                    }
                }
            } else {
                System.out.println("No Purchase Requisitions to display.");
            }
        });
    }

    private JScrollPane createPRTable() {
        List<String> initialEmptyData = new ArrayList<>();
        this.prTableModel = GenericModelHelper.createGenericTableModel(
                initialEmptyData,
                this.prColumnNames,
                this.prRowMapper,
                GenericModelHelper.LAST_COLUMN_EDITABLE
        );

        this.prTable = new JTable(this.prTableModel);
        prTable.setBackground(darkBlue);
        prTable.setForeground(textWhite);
        prTable.setGridColor(new Color(50, 60, 80));
        prTable.setRowHeight(45);
        prTable.setFont(new Font("Arial", Font.PLAIN, 14));
        prTable.getTableHeader().setBackground(new Color(150, 165, 235));
        prTable.getTableHeader().setForeground(textWhite);
        prTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        prTable.setSelectionBackground(new Color(60, 70, 90));
        prTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        prTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        prTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        prTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        prTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        prTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        prTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        prTable.getColumnModel().getColumn(6).setPreferredWidth(160);
        prTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        prTable.getColumnModel().getColumn(8).setPreferredWidth(180);

        PRActionButtonPanel actionPanel = new PRActionButtonPanel(this.prTable);
        int actionsColumnIndex = prColumnNames.length - 1;
        prTable.getColumnModel().getColumn(actionsColumnIndex).setCellRenderer(actionPanel);
        prTable.getColumnModel().getColumn(actionsColumnIndex).setCellEditor(actionPanel);

        prTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int viewRow = prTable.rowAtPoint(e.getPoint());
                    int viewColumn = prTable.columnAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewColumn >= 0 && viewColumn != actionsColumnIndex) {
                        int modelRow = prTable.convertRowIndexToModel(viewRow);
                        String prId = (String) prTable.getModel().getValueAt(modelRow, 0);
                        if (prId != null && !prId.trim().isEmpty()) {
                            showPurchaseRequisitionDetailsPopup(prId);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.prTable);
        scrollPane.getViewport().setBackground(darkBlue);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(textWhite);
        button.setBackground(lightBlue);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (listener != null) {
            button.addActionListener(listener);
        }
        return button;
    }


    class PRActionButtonPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final JTable containingTable;
        private int currentRow;

        public PRActionButtonPanel(JTable table) {
            this.containingTable = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5));
            panel.setOpaque(true);

            JButton editButton = createActionButton("Edit", _ -> {
                fireEditingStopped();
                String prId = (String) containingTable.getValueAt(currentRow, 0);
                showAddPurchaseRequisitionForm(prId);
            });

            JButton deleteButton = createActionButton("Delete", _ -> {
                fireEditingStopped();
                String prId = (String) containingTable.getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(getParentFrame(), // Use getParentFrame() for dialog parent
                        "Are you sure you want to delete Purchase Requisition ID: " + prId + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        PurchaseRequisitionView.this.prController.delete(prId);
                        PurchaseRequisitionView.this.refreshTable();
                        JOptionPane.showMessageDialog(getParentFrame(), // Use getParentFrame()
                                "PR ID " + prId + " deleted successfully.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(getParentFrame(), // Use getParentFrame()
                                "Error deleting PR: " + ex.getMessage(),
                                "Delete Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            panel.setBackground(isSel ? tbl.getSelectionBackground() : (r % 2 == 0 ? darkBlue : new Color(40, 50, 70)));
            return panel;
        }
        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object val, boolean isSel, int r, int c) {
            this.currentRow = r;
            panel.setBackground(tbl.getSelectionBackground());
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public boolean stopCellEditing() { return super.stopCellEditing(); }
    }

    private void showPurchaseRequisitionDetailsPopup(String prId) {
        JFrame parentFrame = getParentFrame();
        PurchaseRequisitionController prController = new PurchaseRequisitionController();
        PurchaseRequisition pr = prController.getFullPurchaseRequisitionById(prId);
        if (pr == null) {
            JOptionPane.showMessageDialog(parentFrame, "Could not retrieve details for PR ID: " + prId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog detailDialog = new JDialog(parentFrame, "Purchase Requisition Details: " + prId, true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.getContentPane().setBackground(mediumBlue);

        JPanel mainDetailPanel = new JPanel();
        mainDetailPanel.setLayout(new BoxLayout(mainDetailPanel, BoxLayout.Y_AXIS));
        mainDetailPanel.setBackground(mediumBlue);
        mainDetailPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel headerDetailsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        headerDetailsPanel.setBackground(mediumBlue);
        headerDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Header Information",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        addDetailRow(headerDetailsPanel, "PR ID:", pr.getPrId());
        addDetailRow(headerDetailsPanel, "Notes:", pr.getNotes());

        String supplierDisplay = "N/A";
        if (pr.getSupplierId() != null && !pr.getSupplierId().isEmpty()) {
            SupplierController supplierController = new SupplierController();
            String supData = supplierController.getOneWithId(pr.getSupplierId());
            if (supData != null) {
                String[] supParts = supData.split(",");
                supplierDisplay = supParts[0] + (supParts.length > 1 ? " - " + supParts[1] : "");
            } else {
                supplierDisplay = pr.getSupplierId() + " (Details not found)";
            }
        }
        addDetailRow(headerDetailsPanel, "Supplier:", supplierDisplay);
        addDetailRow(headerDetailsPanel, "Status:", prRowMapper.getStatusString(pr.getStatus()));
        addDetailRow(headerDetailsPanel, "Created At:", pr.getCreatedAt());
        addDetailRow(headerDetailsPanel, "Created By:", pr.getCreatedBy());
        addDetailRow(headerDetailsPanel, "Updated At:", pr.getUpdatedAt());
        addDetailRow(headerDetailsPanel, "Updated By:", pr.getUpdatedBy());

        mainDetailPanel.add(headerDetailsPanel);
        mainDetailPanel.add(Box.createRigidArea(new Dimension(0,15)));

        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(mediumBlue);
        itemsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lightBlue), "Requested Items",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), textWhite));

        String[] itemTableColumns = {"Item ID", "Item Code", "Item Name", "Qty", "Unit Price", "Total Price"};
        DefaultTableModel itemsDetailTableModel = new DefaultTableModel(itemTableColumns, 0);
        if (pr.getItems() != null) {
            for (PurchaseRequisitionItem item : pr.getItems()) {
                double displayPrice = item.getPrice() / 100.0;
                double displayTotalPrice = item.getTotalPrice() / 100.0;

                itemsDetailTableModel.addRow(new Object[]{
                        item.getItemId(),
                        item.getItemCode(),
                        item.getItemName(),
                        item.getQuantity(),
                        String.format("%.2f", displayPrice),
                        String.format("%.2f", displayTotalPrice)
                });
            }
        }
        JTable itemsDetailTable = new JTable(itemsDetailTableModel);
        itemsDetailTable.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsDetailTable.setRowHeight(25);
        JScrollPane itemsScrollPane = new JScrollPane(itemsDetailTable);
        itemsScrollPane.setPreferredSize(new Dimension(550, 150));
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);

        mainDetailPanel.add(itemsPanel);

        JScrollPane detailScrollPane = new JScrollPane(mainDetailPanel);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(mediumBlue);

        JButton closeButton = createActionButton("Close", _ -> detailDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mediumBlue);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        buttonPanel.add(closeButton);

        detailDialog.add(detailScrollPane, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);

        detailDialog.setMinimumSize(new Dimension(600, 500));
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(parentFrame); // Center relative to parent frame
        detailDialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelName = new JLabel(label);
        labelName.setForeground(textWhite);
        labelName.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(labelName);

        JLabel labelValue = new JLabel(value != null ? value : "N/A");
        labelValue.setForeground(verylightBlue);
        labelValue.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(labelValue);
    }
}