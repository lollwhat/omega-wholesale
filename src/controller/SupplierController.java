package controller;

import model.Supplier;
import model.EntityType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SupplierController extends CRUDController<Supplier> {
    public SupplierController() {
        super("data/supplier_details.txt", EntityType.ITEM);
    }

//    public List<String> getAllSupplier() {
//        try {
//            List<String> lines = FileController.getFile();
//            if (lines == null || lines.isEmpty()) {
//                System.out.println("No supplier found");
//                return null;
//            }
//            return lines;
//        } catch (Exception e) {
//            System.out.println("Error reading all suppliers from file: " + e.getMessage());
//            return null;
//        }
//    }
//
//    public String getOneSupplierWithId(String supplierId) {
//        try {
//            String[] supplierDetails = fileController.getLine(0, supplierId);
//            if (supplierDetails != null) {
//                System.out.println("Supplier found: " + String.join(",", supplierDetails));
//                return String.join(",", supplierDetails);
//            } else {
//                System.out.println("Supplier not found");
//                return null;
//            }
//        } catch (Exception e) {
//            System.out.println("Error reading the supplier from file: " + e.getMessage());
//            return null;
//        }
//    }

    @Override
    public void add(Supplier supplier) {
        try {
            String data = supplier.toCSV();
            FileController.appendFile(data);
            System.out.println("Supplier added successfully");
        } catch (Exception e) {
            System.out.println("Error adding supplier to file: " + e.getMessage());
        }
    }

    public void addSupplier(String supplierCompany, String supplierPIC, String supplierDescription, String supplierAddress, String supplierPhone) {
        int tempSupplierId = FileController.getFile().size()+1;
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();
        String updatedBy = SessionController.getInstance().getUserId();
        Supplier supplier = new Supplier("SUP" + String.format("%03d", tempSupplierId), supplierCompany, supplierPIC, supplierDescription, supplierAddress, supplierPhone, createdAt, createdAt, createdBy, updatedBy);
        add(supplier);
    }

    @Override
    public void update(Supplier supplier) {
        try {
            String data = supplier.toCSV();
            fileController.updateFile(data); // assuming it replaces line by ID
            System.out.println("Supplier updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating supplier in file: " + e.getMessage());
        }
    }

    public void updateSupplier(String supplierId, String supplierCompany, String supplierPIC, String supplierDescription, String supplierAddress, String supplierPhone) {
        try {
            String[] existingSupplierDetails = fileController.getLine(0, supplierId);
            if (existingSupplierDetails == null) {
                System.out.println("Supplier not found");
                return;
            }
            Supplier supplier = new Supplier(supplierId, existingSupplierDetails[1], existingSupplierDetails[2], existingSupplierDetails[3], existingSupplierDetails[4], existingSupplierDetails[5], existingSupplierDetails[6], existingSupplierDetails[7], existingSupplierDetails[8], existingSupplierDetails[9]);
            supplier.setSupplierCompany((supplierCompany != null) ? supplierCompany : existingSupplierDetails[1]);
            supplier.setSupplierPIC((supplierPIC != null) ? supplierPIC : existingSupplierDetails[2]);
            supplier.setSupplierDescription((supplierDescription != null) ? supplierDescription : existingSupplierDetails[3]);
            supplier.setSupplierAddress((supplierAddress != null) ? supplierAddress : existingSupplierDetails[4]);
            supplier.setSupplierPhone((supplierPhone != null) ? supplierPhone : existingSupplierDetails[5]);
            supplier.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            supplier.setUpdatedBy(SessionController.getInstance().getUserId());

            update(supplier);
        } catch (IOException e) {
            System.out.println("Error updating supplier in file: " + e.getMessage());
        }

    }

//    public void deleteItem(String supplierId) {
//        try {
//            fileController.deleteLine(supplierId, 0);
//            System.out.println("Supplier deleted successfully");
//        } catch (Exception e) {
//            System.out.println("Error deleting supplier from file: " + e.getMessage());
//        }
//    }
}
