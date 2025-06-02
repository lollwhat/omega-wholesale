package controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import model.PurchaseOrder;
import model.PurchaseOrderItem;
import model.PurchaseRequisition;
import model.PurchaseRequisitionItem;
import model.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
            List<String> lines = Files.readAllLines(Paths.get(super.filePath));
            if(lines != null) {
                for (String line : lines) {
                    PurchaseOrder purchaseOrder = PurchaseOrder.fromCSV(line);
                    if (purchaseOrder != null) {
                        poHeader.add(purchaseOrder);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase order header file: " + super.filePath + " " + e.getMessage());
        }

        return poHeader;
    }

    public PurchaseOrder getFullPurchaseOrderDetailsById(String poId) {
        PurchaseOrder poHeader = null;

        try {
            List<String> lines = Files.readAllLines(Paths.get(super.filePath));
            for (String line : lines) {
                String[] parts = line.split(",", 2);
                if (parts.length > 0 && parts[0].equals(poId)) {
                    poHeader = PurchaseOrder.fromCSV(line);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase order details: " + super.filePath + " " + e.getMessage());
        }

        if (poHeader == null) {
            System.err.println("Purchase order with ID " + poId + " not found.");
            return null;
        }

        List<PurchaseOrderItem> poItems = new ArrayList<>();
        try {
            List<String> itemLines = Files.readAllLines(Paths.get(PO_ITEMS_FILE_PATH));
            for (String itemLine : itemLines) {
                String[] itemParts = itemLine.split(",", 2);
                if (itemParts.length > 0 && itemParts[0].equals(poId)) {
                    PurchaseOrderItem item = PurchaseOrderItem.fromCSV(itemLine);
                    if (item != null) {
                        poItems.add(item);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase order items: " + PO_ITEMS_FILE_PATH + " " + e.getMessage());
        }
        poHeader.setItems(poItems);

        return poHeader;
    }

    // Purchase Requisition Methods
    public List<PurchaseRequisition> getAllPurchaseRequisitionsHeaders() {
        List<PurchaseRequisition> prHeader = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(PR_HEADER_FILE_PATH));
            if (lines != null) {
                for (String line : lines) {
                    PurchaseRequisition purchaseRequisition = PurchaseRequisition.fromHeaderCSV(line);
                    if (purchaseRequisition != null) {
                        prHeader.add(purchaseRequisition);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase requisition header file: " + PR_HEADER_FILE_PATH + " " + e.getMessage());
        }

        return prHeader;
    }

    public PurchaseRequisition getFullPurchaseRequisitionDetailsById(String prId) {
        PurchaseRequisition prHeader = null;

        try {
            List<String> lines = Files.readAllLines(Paths.get(PR_HEADER_FILE_PATH));
            for (String line : lines) {
                String[] parts = line.split(",", 2);
                if (parts.length > 0 && parts[0].equals(prId)) {
                    prHeader = PurchaseRequisition.fromHeaderCSV(line);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase requisition details: " + PR_HEADER_FILE_PATH + " " + e.getMessage());
        }

        if (prHeader == null) {
            System.err.println("Purchase requisition with ID " + prId + " not found.");
            return null;
        }

        List<PurchaseRequisitionItem> prItems = new ArrayList<>();
        try {
            List<String> itemLines = Files.readAllLines(Paths.get(PR_ITEMS_FILE_PATH));
            for (String itemLine : itemLines) {
                String[] itemParts = itemLine.split(",", 2);
                if (itemParts.length > 0 && itemParts[0].equals(prId)) {
                    PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(itemLine);
                    if (item != null) {
                        prItems.add(item);
                    }
                }
            }
        } catch (IOException e) {
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
            List<String> itemLines = Files.readAllLines(Paths.get(PO_ITEMS_FILE_PATH));
            for (String itemLine : itemLines) {
                PurchaseOrderItem item = PurchaseOrderItem.fromCSV(itemLine);
                if (item != null && item.getPoId() != null) {
                    poItemsMap.computeIfAbsent(item.getPoId(), k -> new ArrayList<>()).add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase order items: " + PO_ITEMS_FILE_PATH + " " + e.getMessage());

            return purchaseOrders;
        }

        try {
            List<String> headerLines = Files.readAllLines(Paths.get(super.filePath));
            for (String headerLine : headerLines) {
                PurchaseOrder poHeader = PurchaseOrder.fromCSV(headerLine);
                if (poHeader != null) {
                    poHeader.setItems(poItemsMap.getOrDefault(poHeader.getPoId(), new ArrayList<>()));
                    purchaseOrders.add(poHeader);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase order header file: " + super.filePath + " " + e.getMessage());
        }
        return purchaseOrders;
    }

    public List<PurchaseRequisition> getAllPurchaseRequisitionWithDetails() {
        List<PurchaseRequisition> purchaseRequisitions = new ArrayList<>();
        Map<String, List<PurchaseRequisitionItem>> prItemsMap = new HashMap<>();

        try {
            List<String> itemLines = Files.readAllLines(Paths.get(PR_ITEMS_FILE_PATH));
            for (String itemLine : itemLines) {
                PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(itemLine);
                if (item != null && item.getPrId() != null) {
                    prItemsMap.computeIfAbsent(item.getPrId(), k -> new ArrayList<>()).add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase requisition items: " + PR_ITEMS_FILE_PATH + " " + e.getMessage());
            return purchaseRequisitions;
        }

        try {
            List<String> headerLines = Files.readAllLines(Paths.get(PR_HEADER_FILE_PATH));
            for (String headerLine : headerLines) {
                PurchaseRequisition prHeader = PurchaseRequisition.fromHeaderCSV(headerLine);
                if (prHeader != null) {
                    prHeader.setItems(prItemsMap.get(prHeader.getPrId()));
                    purchaseRequisitions.add(prHeader);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase requisition header file: " + PR_HEADER_FILE_PATH + " " + e.getMessage());
        }
        return purchaseRequisitions;
    }

//    public void approvePurchaseOrder(String poId) {
//        try {
//            List<String> lines = Files.readAllLines(Paths.get(super.filePath));
//            for (int i = 0; i < lines.size(); i++) {
//                String[] parts = lines.get(i).split(",", 2);
//                if (parts.length > 0 && parts[0].equals(poId)) {
//                    PurchaseOrder po = PurchaseOrder.fromCSV(lines.get(i));
//                    if (po != null) {
//                        po.setStatus(PurchaseOrder.Status.APPROVED);
//                        lines.set(i, po.toCSV());
//                        Files.write(Paths.get(super.filePath), lines);
//                        System.out.println("Purchase Order " + poId + " approved.");
//                    }
//                    return;
//                }
//            }
//            System.err.println("Purchase Order with ID " + poId + " not found.");
//        } catch (IOException e) {
//            System.err.println("Error approving purchase order: " + e.getMessage());
//        }
//    }

    @Override
    public void add(PurchaseOrder data) {
    }

    @Override
    public void update(PurchaseOrder purchaseOrder) {

    }



}