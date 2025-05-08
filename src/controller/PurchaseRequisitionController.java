package controller;

import model.PurchaseRequisition;
import model.EntityType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseRequisitionController extends CRUDController<PurchaseRequisition> {
    public PurchaseRequisitionController() {
        super("data/purchase_requisition.txt", EntityType.PURCHASE_REQUISITION);
    }

    @Override
    public void add(PurchaseRequisition purchaseRequisition) {
        try {
            String data = purchaseRequisition.toCSV();
            FileController.appendFile(data);
            System.out.println("Purchase requisition added successfully");
        } catch (Exception e) {
            System.out.println("Error adding purchase requisition to file: " + e.getMessage());
        }
    }

    public void addPurchaseRequisition(String itemId, String itemName, String notes, int price, int quantity, int totalPrice, String supplierId, String supplierName, String supplierCompanyName, String supplierAddress, int status) {
        int tempItemId = FileController.getFile().size()+1;
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();
        String updatedBy = SessionController.getInstance().getUserId();
        PurchaseRequisition purchaseRequisition = new PurchaseRequisition("PR" + String.format("%03d", tempItemId), itemId, itemName, notes, price, quantity, totalPrice, supplierId, supplierName, supplierCompanyName, supplierAddress, status, createdAt, createdBy, createdAt, updatedBy);
        add(purchaseRequisition);
        String data = purchaseRequisition.toCSV();
        FileController.appendFile(data);
        System.out.println("Purchase requisition added successfully");
    }

    @Override
    public void update(PurchaseRequisition purchaseRequisition) {
        try {
            String data = purchaseRequisition.toCSV();
            fileController.updateFile(data);
            System.out.println("Purchase requisition updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating purchase requisition in file: " + e.getMessage());
        }
    }

    public void updateItem(String prId, String itemId, String itemName, String notes, int price, int quantity, int totalPrice, String supplierId, String supplierName, String supplierCompanyName, String supplierAddress, int status) {
        try {
            String[] existingPurchaseRequisitionDetails = fileController.getLine(0, prId);
            if (existingPurchaseRequisitionDetails == null) {
                System.out.println("Purchase requisition not found");
                return;
            }
            PurchaseRequisition purchaseRequisition = new PurchaseRequisition(prId, existingPurchaseRequisitionDetails[1], existingPurchaseRequisitionDetails[2], existingPurchaseRequisitionDetails[3], Integer.parseInt(existingPurchaseRequisitionDetails[4]), Integer.parseInt(existingPurchaseRequisitionDetails[5]), Integer.parseInt(existingPurchaseRequisitionDetails[6]), existingPurchaseRequisitionDetails[7], existingPurchaseRequisitionDetails[8], existingPurchaseRequisitionDetails[9], existingPurchaseRequisitionDetails[10], Integer.parseInt(existingPurchaseRequisitionDetails[11]), existingPurchaseRequisitionDetails[12], existingPurchaseRequisitionDetails[13], existingPurchaseRequisitionDetails[14], existingPurchaseRequisitionDetails[15]);
            purchaseRequisition.setItemId((itemId != null) ? itemId : existingPurchaseRequisitionDetails[1]);
            purchaseRequisition.setItemName((itemName != null) ? itemName : existingPurchaseRequisitionDetails[2]);
            purchaseRequisition.setNotes((notes != null) ? notes : existingPurchaseRequisitionDetails[3]);
            purchaseRequisition.setPrice((price != 0) ? price : Integer.parseInt(existingPurchaseRequisitionDetails[4]));
            purchaseRequisition.setQuantity((quantity != 0) ? quantity : Integer.parseInt(existingPurchaseRequisitionDetails[5]));
            purchaseRequisition.setTotalPrice((totalPrice != 0) ? totalPrice : Integer.parseInt(existingPurchaseRequisitionDetails[6]));
            purchaseRequisition.setSupplierId((supplierId != null) ? supplierId : existingPurchaseRequisitionDetails[7]);
            purchaseRequisition.setSupplierName((supplierName != null) ? supplierName : existingPurchaseRequisitionDetails[8]);
            purchaseRequisition.setSupplierCompanyName((supplierCompanyName != null) ? supplierCompanyName : existingPurchaseRequisitionDetails[9]);
            purchaseRequisition.setSupplierAddress((supplierAddress != null) ? supplierAddress : existingPurchaseRequisitionDetails[10]);
            purchaseRequisition.setStatus((status != 0) ? status : Integer.parseInt(existingPurchaseRequisitionDetails[11]));
            purchaseRequisition.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            purchaseRequisition.setUpdatedBy(SessionController.getInstance().getUserId());
            update(purchaseRequisition);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
