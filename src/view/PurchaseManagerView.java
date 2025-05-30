package view;

import model.PurchaseManager;

import javax.swing.*;

public class PurchaseManagerView extends DashboardView {
    private final String[] quickOptions = {"View Item", "View Supplier", "View Purchase Requisition"};

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
            case "View Purchase Requisition":
                PMPurchaseRequisitionView pmPurchaseRequisitionView = new PMPurchaseRequisitionView();
                return pmPurchaseRequisitionView.createPMPurchaseRequisitionPanel();
            case "Purchase Order Management":
                PurchaseOrderView purchaseOrderView = new PurchaseOrderView();
                return purchaseOrderView.createPurchaseOrderPanel();
            default:
                return createDashboardContentPanel(quickOptions);
        }
    }
}