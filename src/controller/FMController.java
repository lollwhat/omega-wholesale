package controller;

import java.io.*;

import model.FinanceManager;
import view.FinanceManagerView;

import javax.swing.*;

public class FMController{

    private FinanceManager model;
    private FinanceManagerView view;
    private static final String PurchaseOrder = "data/po_details.txt";

    public FMController (FinanceManager model, FinanceManagerView view){
        this.model = model;
        this.view = view;
    }

    public String getPO() {
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(PurchaseOrder));
            String line;

            while((line = reader.readLine()) != null) {
                String[] details =  line.split(",");
                if(details.length == 9){
                    String POid = details[0];
                    String POdate = details[1];
                    String POreq = details[2];
                    String POitem = details[3];
                    String POquantity = details[4];
                    String POsupplier = details[5];
                    String POstatus = details[6];
                    String POcreatedBy = details[7];
                    String POapprovedBy = details[8];

                    builder.append(line).append("\n");
                } else System.out.println("Details does not fit required length");
            }
            reader.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        return builder.toString();
    }

//    public String approvePO() {
//        String approve = "Approved";
//
//        File original =  new File(PurchaseOrder);
//        BufferedReader reader = new BufferedReader(new FileReader(PurchaseOrder));
//
//        File temp = new File("temp_details.txt");
//        PrintWriter writer = new PrintWriter(new FileWriter(temp));
//
//        String line = null;
//
//        while ((line = reader.readLine()) !=null) {
//            int i = 1;
//            //if() {
//                String[] details =  line.split(",");
//                String POstatus = details[6];
//
//                String newStatus = String.valueOf(details[6] = approve).trim();
//
//            //}
//        }
//        return null;
//    }


}