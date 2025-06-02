package view;

import model.*;

import javax.swing.*;

public class FMView extends DashboardView{
    private final String[] quickOptions = {"View Requisitions", "View Purchase Orders", "Financial Management"};

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
            case "Financial Management":
//                FinancialManagementView financialManagementView = new FinancialManagementView();
//                return financialManagementView.createFinancialManagementPanel();
            default:
                return createDashboardContentPanel(quickOptions);
        }
    }
}
