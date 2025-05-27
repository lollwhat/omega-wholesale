package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseOrderSeeder {
    final String PO_HEADER_FILE = "data/purchase_order.txt";

    public PurchaseOrderSeeder() {
        // Default constructor
    }

    public void seeding() {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            Files.createDirectories(Paths.get("data"));

            BufferedWriter writer = new BufferedWriter(new FileWriter(PO_HEADER_FILE, false));

            writer.write("PO001,PR001,Cancelled due to budget constraints,SUP001,2,"+currentTime+",system_seeder,"+currentTime+",system_seeder,null,null\n");
            writer.write("PO002,PR002,Urgent delivery received,SUP001,1,"+currentTime+",system_seeder,"+currentTime+",system_seeder,null,null\n");
            writer.write("PO003,PR003,Standard order,SUP002,0,"+currentTime+",system_seeder,"+currentTime+",system_seeder,null,null\n");
            writer.write("PO004,PR005,Items for project Alpha,SUP001,0,"+currentTime+",system_seeder,"+currentTime+",system_seeder,null,null\n");
            writer.write("PO005,PR004,Items for project Beta,SUP002,0,"+currentTime+",system_seeder,"+currentTime+",system_seeder,null,null\n");


            System.out.println("Successfully seeded data to " + PO_HEADER_FILE);
            writer.close();
        } catch (Exception e) {
            System.err.println("Error seeding data to " + PO_HEADER_FILE + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PurchaseOrderSeeder seeder = new PurchaseOrderSeeder();
        seeder.seeding();
    }
}