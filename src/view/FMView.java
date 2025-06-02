package view;

import model.*;

import javax.swing.*;

public class FMView extends DashboardView{
    private final String[] quickOptions = {"View Requisitions", "View Purchase Orders", "Purchase Order Management"};

    public FMView(FinanceManager financeManager, String[] menuItems, String[] quickOptions) {
        super(financeManager, menuItems, quickOptions);
    }

    @Override
    protected JPanel createContentPanel(String contentType) {
        switch (contentType) {
            case "View Requisitions":
                FinancePurchaseRequisitionView requisitionView = new FinancePurchaseRequisitionView();
                return requisitionView.createRequisitionPanel();
            case "View Purchase Orders":
                FinancePurchaseOrderView purchaseOrdersView = new FinancePurchaseOrderView();
                return purchaseOrdersView.financePurchaseOrderPanel();
            case "Purchase Order Approval":
                FMPOApprovalView financePOApprovalView = new FMPOApprovalView();
                return financePOApprovalView.createFMPOApprovalPanel();
            case "Purchase Order Management":
                FinancePOManagementView financialManagementView = new FinancePOManagementView();
                return financialManagementView.createPurchaseOrderPanel();
            default:
                return createDashboardContentPanel(quickOptions);
        }
    }
}
