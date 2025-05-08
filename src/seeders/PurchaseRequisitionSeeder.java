package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PurchaseRequisitionSeeder {
    final String PRDetailsFile = "data/purchase_requisition.txt";

    public PurchaseRequisitionSeeder() {
        // Default constructor
    }

    public void seeding() {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(PRDetailsFile, false));
            writer.write("PR001,Seeder Item ID A,Seeder Purchase Requisition A,Seeder Notes A,20,10,200,SUP001,Seeder Supplier Name A,Seeder Supplier Company Name A,Seeder Supplier Address A,approved,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("PR002,Seeder Item ID B,Seeder Purchase Requisition B,Seeder Notes B,40,5,200,SUP001,Seeder Supplier Name A,Seeder Supplier Company Name A,Seeder Supplier Address A,approved,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("PR003,Seeder Item ID C,Seeder Purchase Requisition C,Seeder Notes C,50,8,400,SUP002,Seeder Supplier Name B,Seeder Supplier Company Name B,Seeder Supplier Address B,pending,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("PR004,Seeder Item ID D,Seeder Purchase Requisition D,Seeder Notes D,10,20,200,SUP003,Seeder Supplier Name C,Seeder Supplier Company Name C,Seeder Supplier Address C,rejected,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("PR005,Seeder Item ID E,Seeder Purchase Requisition E,Seeder Notes E,30,3,60,SUP001,Seeder Supplier Name A,Seeder Supplier Company Name A,Seeder Supplier Address A,approved,"+createdAt+",sales1,"+createdAt+",sales1\n");
            System.out.println("Successfully seeded data to purchase_requisition.txt");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error seeding data: " + e.getMessage());
        }
    }
}
