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

            writer.write("PO001,PR001,Standard order from PR001,1,2025-05-30 09:00:00,PM001,2025-05-30 09:05:00,PM001,,\n" +
                    "PO002,PR002,Order fulfilled for PR002,2,2025-05-30 09:10:00,PM001,2025-05-31 10:00:00,FM001,2025-05-31 10:00:00,FM001\n" +
                    "PO003,PR003,Items for new office setup from PR003,0,2025-05-30 09:15:00,PM001,2025-05-30 09:15:00,PM001,,\n" +
                    "PO004,PR001,Partial order for Laptops - PR001,0,2025-05-30 09:20:00,PM001,2025-05-30 09:20:00,PM001,,");


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