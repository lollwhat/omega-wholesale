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

            writer.write("PO001,ITM001,LAP001,HP EliteBook G10 Laptop,1,1150,1150\n");

            writer.write("PO002,ITM003,MON001,Dell 27 inch 4K Monitor,1,340,340\n");
            writer.write("PO002,ITM002,MSE001,Logitech MX Master 3S Mouse,2,85,170\n");

            writer.write("PO003,ITM004,SSD001,Samsung 1TB NVMe SSD,5,75,375\n");
            writer.write("PO003,ITM005,KBD002,Mechanical Keyboard Keychron K2,1,145,145\n");

            writer.write("PO004,ITM001,LAP001,HP EliteBook G10 Laptop,1,1180,1180\n");

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