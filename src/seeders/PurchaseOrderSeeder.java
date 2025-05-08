package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PurchaseOrderSeeder {
    final String PRDetailsFile = "data/purchase_order.txt";

    public PurchaseOrderSeeder() {
        // Default constructor
    }

    public void seeding() {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(PRDetailsFile, false));
            writer.write("PO001,PR001,Seeder Item ID A,Seeder Item Name A,Seeder Notes A,20,10,200,SUP001,Seeder Supplier Name A,Seeder Supplier Company Name A,Seeder Supplier Address A,cancelled"+createdAt+",sales1,"+createdAt+",sales1,cancelled,cancelled\n");
            writer.write("PO002,PR002,Seeder Item ID B,Seeder Item Name B,Seeder Notes B,40,5,200,SUP001,Seeder Supplier Name A,Seeder Supplier Company Name A,Seeder Supplier Address A,received"+createdAt+",sales1,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("PO005,PR005,Seeder Item ID E,Seeder Item Name E,Seeder Notes E,30,3,60,SUP001,Seeder Supplier Name A,Seeder Supplier Company Name A,Seeder Supplier Address A,processing"+createdAt+",sales1,"+createdAt+",sales1,pending,pending\n");
            System.out.println("Successfully seeded data to purchase_order.txt");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error seeding data: " + e.getMessage());
        }
    }
}
