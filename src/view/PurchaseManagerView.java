package view;

import model.PurchaseManager;

import javax.swing.*;

public class PurchaseManagerView extends DashboardView {
    private final String[] quickOptions = {"View Item", "View Supplier", "PR Overview & Create Purchase Order"};

    public PurchaseManagerView(PurchaseManager purchaseManager , String [] menuItems, String[] quickOptions) {
        super(purchaseManager, menuItems, quickOptions);
    }

    @Override
    protected JPanel createContentPanel(String contentType) {
        switch (contentType) {
            case "View Item":
                ItemManagementView itemManagementPanel = new ItemManagementView();
                return itemManagementPanel.createViewItemPanel();
            case "View Supplier":
                SupplierManagementView supplierManagementView = new SupplierManagementView();
                return supplierManagementView.createViewSupplierPanel();
            case "PR Overview & Create Purchase Order":
                PMPurchaseRequisitionView pmPurchaseRequisitionView = new PMPurchaseRequisitionView();
                return pmPurchaseRequisitionView.createPMPurchaseRequisitionPanel();
            case "List of Purchase Orders":
                PurchaseOrderView purchaseOrderView = new PurchaseOrderView();
                return purchaseOrderView.createPurchaseOrderPanel();
            default:
                return createDashboardContentPanel(quickOptions);
        }
    }
}