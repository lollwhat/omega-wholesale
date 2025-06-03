package controller;

import java.util.*;

import model.PurchaseOrder;
import model.PurchaseOrderItem;
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;
import model.EntityType;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

public class FMController extends CRUDController<PurchaseOrder> {

    private FileController poItemFileController;
    private FileController prItemFileController;

    private static final String PO_ITEMS_FILE_PATH = "data/purchase_order_item.txt";
    private static final String PO_HEADER_FILE_PATH = "data/purchase_order.txt";

    private static final String PR_ITEMS_FILE_PATH = "data/purchase_requisition_item.txt";
    private static final String PR_HEADER_FILE_PATH = "data/purchase_requisition.txt";

    public FMController (){
        super(PO_HEADER_FILE_PATH, EntityType.PURCHASE_ORDER);
        this.poItemFileController = new FileController(PO_ITEMS_FILE_PATH);
        this.prItemFileController = new FileController(PR_ITEMS_FILE_PATH);


        checkFileExists(PO_ITEMS_FILE_PATH);
        checkFileExists(PO_HEADER_FILE_PATH);
        checkFileExists(PR_ITEMS_FILE_PATH);
        checkFileExists(PR_HEADER_FILE_PATH);
    }

    private void checkFileExists(String filePath){
        try{
            File file = new File(filePath);
            if(!file.exists()) {
                if(file.getParentFile() != null && !file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error ensuring file exists at " + filePath + ": " + e.getMessage());
        }
    }

    //Purchase Order Methods
    public List<PurchaseOrder> getAllPurchaseOrderHeaders() {
        List<PurchaseOrder> poHeader = new ArrayList<>();

        try{
            new FileController(super.filePath);
            List<String> lines = FileController.getFile();
            if(lines != null) {
                for (String line : lines) {
                    PurchaseOrder purchaseOrder = PurchaseOrder.fromCSV(line);
                    if (purchaseOrder != null) {
                        poHeader.add(purchaseOrder);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase order header file: " + super.filePath + " " + e.getMessage());
        }

        return poHeader;
    }

    public PurchaseOrder getFullPurchaseOrderDetailsById(String poId) {
        PurchaseOrder poHeader = null;

        try {
            new FileController(super.filePath);
            List<String> lines = FileController.getFile();
            for (String line : lines) {
                String[] parts = line.split(",", 2);
                if (parts.length > 0 && parts[0].equals(poId)) {
                    poHeader = PurchaseOrder.fromCSV(line);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase order details: " + super.filePath + " " + e.getMessage());
        }

        if (poHeader == null) {
            System.err.println("Purchase order with ID " + poId + " not found.");
            return null;
        }

        List<PurchaseOrderItem> poItems = new ArrayList<>();
        try {
            new FileController(PO_ITEMS_FILE_PATH);
            List<String> itemLines = FileController.getFile();
            for (String itemLine : itemLines) {
                String[] itemParts = itemLine.split(",", 2);
                if (itemParts.length > 0 && itemParts[0].equals(poId)) {
                    PurchaseOrderItem item = PurchaseOrderItem.fromCSV(itemLine);
                    if (item != null) {
                        poItems.add(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase order items: " + PO_ITEMS_FILE_PATH + " " + e.getMessage());
        }
        poHeader.setItems(poItems);

        return poHeader;
    }

    // Purchase Requisition Methods
    public List<PurchaseRequisition> getAllPurchaseRequisitionsHeaders() {
        List<PurchaseRequisition> prHeader = new ArrayList<>();

        try {
            new FileController(PR_HEADER_FILE_PATH);
            List<String> lines = FileController.getFile();
            if (lines != null) {
                for (String line : lines) {
                    PurchaseRequisition purchaseRequisition = PurchaseRequisition.fromHeaderCSV(line);
                    if (purchaseRequisition != null) {
                        prHeader.add(purchaseRequisition);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase requisition header file: " + PR_HEADER_FILE_PATH + " " + e.getMessage());
        }

        return prHeader;
    }

    public PurchaseRequisition getFullPurchaseRequisitionDetailsById(String prId) {
        PurchaseRequisition prHeader = null;

        try {
            new FileController(PR_HEADER_FILE_PATH);
            List<String> lines = FileController.getFile();
            for (String line : lines) {
                String[] parts = line.split(",", 2);
                if (parts.length > 0 && parts[0].equals(prId)) {
                    prHeader = PurchaseRequisition.fromHeaderCSV(line);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase requisition details: " + PR_HEADER_FILE_PATH + " " + e.getMessage());
        }

        if (prHeader == null) {
            System.err.println("Purchase requisition with ID " + prId + " not found.");
            return null;
        }

        List<PurchaseRequisitionItem> prItems = new ArrayList<>();
        try {
            new FileController(PR_ITEMS_FILE_PATH);
            List<String> itemLines = FileController.getFile();
            for (String itemLine : itemLines) {
                String[] itemParts = itemLine.split(",", 2);
                if (itemParts.length > 0 && itemParts[0].equals(prId)) {
                    PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(itemLine);
                    if (item != null) {
                        prItems.add(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase requisition items: " + PR_ITEMS_FILE_PATH + " " + e.getMessage());
        }
        prHeader.setItems(prItems);

        return prHeader;
    }

    //Get all details including items

    public List<PurchaseOrder> getAllPurchaseOrderWithDetails() {
        List<PurchaseOrder> purchaseOrders = new ArrayList<>();
        Map<String, List<PurchaseOrderItem>> poItemsMap = new HashMap<>();

        try {
            new FileController(PO_ITEMS_FILE_PATH);
            List<String> itemLines = FileController.getFile();
            for (String itemLine : itemLines) {
                PurchaseOrderItem item = PurchaseOrderItem.fromCSV(itemLine);
                if (item != null && item.getPoId() != null) {
                    poItemsMap.computeIfAbsent(item.getPoId(), k -> new ArrayList<>()).add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase order items: " + PO_ITEMS_FILE_PATH + " " + e.getMessage());

            return purchaseOrders;
        }

        try {
            new FileController(super.filePath);
            List<String> headerLines = FileController.getFile();
            for (String headerLine : headerLines) {
                PurchaseOrder poHeader = PurchaseOrder.fromCSV(headerLine);
                if (poHeader != null) {
                    poHeader.setItems(poItemsMap.getOrDefault(poHeader.getPoId(), new ArrayList<>()));
                    purchaseOrders.add(poHeader);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase order header file: " + super.filePath + " " + e.getMessage());
        }
        return purchaseOrders;
    }

    public List<PurchaseRequisition> getAllPurchaseRequisitionWithDetails() {
        List<PurchaseRequisition> purchaseRequisitions = new ArrayList<>();
        Map<String, List<PurchaseRequisitionItem>> prItemsMap = new HashMap<>();

        try {
            new FileController(PR_ITEMS_FILE_PATH);
            List<String> itemLines = FileController.getFile();
            for (String itemLine : itemLines) {
                PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(itemLine);
                if (item != null && item.getPrId() != null) {
                    prItemsMap.computeIfAbsent(item.getPrId(), k -> new ArrayList<>()).add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase requisition items: " + PR_ITEMS_FILE_PATH + " " + e.getMessage());
            return purchaseRequisitions;
        }

        try {
            new FileController(PR_HEADER_FILE_PATH);
            List<String> headerLines = FileController.getFile();
            for (String headerLine : headerLines) {
                PurchaseRequisition prHeader = PurchaseRequisition.fromHeaderCSV(headerLine);
                if (prHeader != null) {
                    prHeader.setItems(prItemsMap.get(prHeader.getPrId()));
                    purchaseRequisitions.add(prHeader);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading purchase requisition header file: " + PR_HEADER_FILE_PATH + " " + e.getMessage());
        }
        return purchaseRequisitions;
    }

    public boolean updatePurchaseOrderStatus(String poId, int newStatus, String userId) {
        PurchaseOrder poToUpdate = getFullPurchaseOrderDetailsById(poId);

        if (poToUpdate == null) {
            System.err.println("Purchase Order with ID '" + poId + "' not found for status update.");
            return false;
        }

        poToUpdate.setStatus(newStatus);
        poToUpdate.setUpdatedBy(userId);
        poToUpdate.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        try {
            this.update(poToUpdate);
            System.out.println("Purchase Order " + poId + " status updated to " + newStatus + " by user " + userId);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving Purchase Order " + poId + " after status update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void add(PurchaseOrder data) {
    }

    @Override
    public void update(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null || purchaseOrder.getPoId() == null || purchaseOrder.getPoId().trim().isEmpty()) {
            throw new IllegalArgumentException("FMController.update Purchase Order or PO ID cannot be null for update.");
        }

        String poIdToUpdate = purchaseOrder.getPoId();
        boolean headerUpdatedInList = false;

        try {
            new FileController(super.filePath);
            List<String> headerLines = new ArrayList<>(FileController.getFile());

            for (int i = 0; i < headerLines.size(); i++) {
                String line = headerLines.get(i);
                String[] parts = line.split(",", 2);
                if (parts.length > 0 && parts[0].equals(poIdToUpdate)) {
                    headerLines.set(i, purchaseOrder.toCSV()); //
                    headerUpdatedInList = true;
                    break;
                }
            }

            if (headerUpdatedInList) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(super.filePath, false))) {
                    for (String line : headerLines) {
                        writer.write(line);
                        writer.newLine();
                    }
                    System.out.println("PO Header " + poIdToUpdate + " updated successfully using BufferedWriter.");
                } catch (IOException e) {
                    System.err.println("FMController.update: IOException while writing PO header for " + poIdToUpdate + " with BufferedWriter: " + e.getMessage());
                    throw new RuntimeException("Failed to write PO header for " + poIdToUpdate, e);
                }
            } else {
                System.err.println("FMController.update: PO Header " + poIdToUpdate + " not found in " + super.filePath + ". Header not updated.");
            }
        } catch (Exception e) {
            System.err.println("FMController.update: Error preparing PO header update for " + poIdToUpdate + ": " + e.getMessage());
            throw new RuntimeException("Failed to prepare PO header update for " + poIdToUpdate, e);
        }


        try {
            new FileController(PO_ITEMS_FILE_PATH);
            List<String> allCurrentItemLines = FileController.getFile();
            List<String> newMasterItemList = new ArrayList<>();

            if (allCurrentItemLines != null) {
                for (String itemLine : allCurrentItemLines) {
                    String[] parts = itemLine.split(",", 2);
                    if (parts.length > 0 && !parts[0].equals(poIdToUpdate)) {
                        newMasterItemList.add(itemLine);
                    }
                }
            }

            if (purchaseOrder.getItems() != null) { //
                for (PurchaseOrderItem item : purchaseOrder.getItems()) { //
                    if (!item.getPoId().equals(poIdToUpdate)) {
                        item.setPoId(poIdToUpdate); //
                    }
                    newMasterItemList.add(item.toCSV()); //
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(PO_ITEMS_FILE_PATH, false))) {
                for (String itemLine : newMasterItemList) {
                    writer.write(itemLine);
                    writer.newLine();
                }
                System.out.println("FMController: PO Items for " + poIdToUpdate + " have been rewritten/updated using BufferedWriter.");
            } catch (IOException e) {
                System.err.println("FMController.update: IOException while writing PO items for " + poIdToUpdate + " with BufferedWriter: " + e.getMessage());
                throw new RuntimeException("Failed to write PO items for " + poIdToUpdate, e);
            }

        } catch (Exception e) {
            System.err.println("FMController.update: Error preparing PO items update for " + poIdToUpdate + ": " + e.getMessage());
            throw new RuntimeException("Failed to prepare PO items update for " + poIdToUpdate, e);
        }
    }



}