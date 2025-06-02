package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseOrderItemSeeder {
    final String PO_ITEMS_FILE = "data/purchase_order_item.txt";

    public PurchaseOrderItemSeeder() {
        // Default constructor
    }

    public void seeding() {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            Files.createDirectories(Paths.get("data")); // Ensure directory exists

            BufferedWriter writer = new BufferedWriter(new FileWriter(PO_ITEMS_FILE, false));

            writer.write("PO001,IM001,LAP001,HP EliteBook G10,3,125000,375000,SUP004\n" +
                    "PO001,IM002,MSE001,Logitech MX Master 3S,10,9550,95500,SUP002\n" +
                    "PO002,IM003,MON001,Dell UltraSharp 27 4K,8,45000,360000,SUP001\n" +
                    "PO002,IM004,SSD001,Samsung 1TB NVMe SSD,20,8500,170000,SUP003\n" +
                    "PO003,IM005,KBD002,Keychron K2 Mechanical Keyboard,15,16075,241125,SUP002\n" +
                    "PO004,IM001,LAP001,HP EliteBook G10,2,126000,252000,SUP004");

            System.out.println("Successfully seeded data to " + PO_ITEMS_FILE);
            writer.close();
        } catch (Exception e) {
            System.err.println("Error seeding data to " + PO_ITEMS_FILE + ": " + e.getMessage());
        }

    }

    public static void main(String[] args) {
        PurchaseOrderItemSeeder seeder = new PurchaseOrderItemSeeder();
        seeder.seeding();
    }
}