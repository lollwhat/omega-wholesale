package controller;

import model.EntityType;
import model.PurchaseOrder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseOrderController extends CRUDController<PurchaseOrder> {
    public PurchaseOrderController() {
        super("data/purchase_order.txt", EntityType.PURCHASE_ORDER);
    }

    @Override
    public void add(PurchaseOrder purchaseOrder) {
        try {
            String data = purchaseOrder.toCSV();
            FileController.appendFile(data);
            System.out.println("Purchase order added successfully");
        } catch (Exception e) {
            System.out.println("Error adding purchase order to file: " + e.getMessage());
        }
    }

    public void addPurchaseOrder(String prId, String itemId, String itemName, String notes, int price, int quantity, int totalPrice, String supplierId, String supplierName, String supplierCompanyName, String supplierAddress, int status) {
        int tempItemId = FileController.getFile().size()+1;
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();
        PurchaseOrder purchaseOrder = new PurchaseOrder("PO" + String.format("%03d", tempItemId), prId, itemId, itemName, notes, price, quantity, totalPrice, supplierId, supplierName, supplierCompanyName, supplierAddress, status, createdAt, createdBy, createdAt, createdBy, null, null);
        add(purchaseOrder);
        String data = purchaseOrder.toCSV();
        FileController.appendFile(data);
        System.out.println("Purchase order added successfully");
    }

    @Override
    public void update(PurchaseOrder purchaseOrder) {
        try {
            String data = purchaseOrder.toCSV();
            fileController.updateFile(data);
            System.out.println("Purchase order updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating purchase order in file: " + e.getMessage());
        }
    }

    public void updateItem(String poId, String prId, String itemId, String itemName, String notes, int price, int quantity, int totalPrice, String supplierId, String supplierName, String supplierCompanyName, String supplierAddress, int status) {
        try {
            String[] existingPurchaseOrderDetails = fileController.getLine(0, prId);
            if (existingPurchaseOrderDetails == null) {
                System.out.println("Purchase order not found");
                return;
            }
            PurchaseOrder purchaseOrder = new PurchaseOrder(poId, existingPurchaseOrderDetails[1], existingPurchaseOrderDetails[2], existingPurchaseOrderDetails[3], existingPurchaseOrderDetails[4], Integer.parseInt(existingPurchaseOrderDetails[5]), Integer.parseInt(existingPurchaseOrderDetails[6]), Integer.parseInt(existingPurchaseOrderDetails[7]), existingPurchaseOrderDetails[8], existingPurchaseOrderDetails[9], existingPurchaseOrderDetails[10], existingPurchaseOrderDetails[11], Integer.parseInt(existingPurchaseOrderDetails[12]), existingPurchaseOrderDetails[13], existingPurchaseOrderDetails[14], existingPurchaseOrderDetails[15], existingPurchaseOrderDetails[16], existingPurchaseOrderDetails[17], existingPurchaseOrderDetails[18]);
            purchaseOrder.setPrId((prId != null) ? prId : existingPurchaseOrderDetails[0]);
            purchaseOrder.setItemId((itemId != null) ? itemId : existingPurchaseOrderDetails[1]);
            purchaseOrder.setItemName((itemName != null) ? itemName : existingPurchaseOrderDetails[2]);
            purchaseOrder.setNotes((notes != null) ? notes : existingPurchaseOrderDetails[3]);
            purchaseOrder.setPrice((price != 0) ? price : Integer.parseInt(existingPurchaseOrderDetails[4]));
            purchaseOrder.setQuantity((quantity != 0) ? quantity : Integer.parseInt(existingPurchaseOrderDetails[5]));
            purchaseOrder.setTotalPrice((totalPrice != 0) ? totalPrice : Integer.parseInt(existingPurchaseOrderDetails[6]));
            purchaseOrder.setSupplierId((supplierId != null) ? supplierId : existingPurchaseOrderDetails[7]);
            purchaseOrder.setSupplierName((supplierName != null) ? supplierName : existingPurchaseOrderDetails[8]);
            purchaseOrder.setSupplierCompanyName((supplierCompanyName != null) ? supplierCompanyName : existingPurchaseOrderDetails[9]);
            purchaseOrder.setSupplierAddress((supplierAddress != null) ? supplierAddress : existingPurchaseOrderDetails[10]);
            if (status != Integer.parseInt(existingPurchaseOrderDetails[11])) {
                purchaseOrder.setStatus(status);
                purchaseOrder.setReceivedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                purchaseOrder.setReceivedBy(SessionController.getInstance().getUserId());
            }
            purchaseOrder.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            purchaseOrder.setUpdatedBy(SessionController.getInstance().getUserId());
            update(purchaseOrder);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
