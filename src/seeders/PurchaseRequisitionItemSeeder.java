package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class PurchaseRequisitionItemSeeder {
    final String PR_ITEMS_FILE = "data/purchase_requisition_item.txt";

    public PurchaseRequisitionItemSeeder() {
        // Default constructor
    }

    public void seeding() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(PR_ITEMS_FILE, false)); // false to overwrite

            // Items for PR001
            writer.write("PR001,ITM001,LAP001,HP EliteBook G10 Laptop,2,1200,2400\n");
            writer.write("PR001,ITM002,MSE001,Logitech MX Master 3S Mouse,2,90,180\n");
            writer.write("PR001,ITM005,KBD002,Mechanical Keyboard Keychron K2,1,150,150\n");

            // Items for PR002
            writer.write("PR002,ITM003,MON001,Dell 27 inch 4K Monitor,1,350,350\n");

            // Items for PR003
            writer.write("PR003,ITM004,SSD001,Samsung 1TB NVMe SSD,5,80,400\n");
            writer.write("PR003,ITM002,MSE001,Logitech MX Master 3S Mouse,3,90,270\n");

            // Items for PR004 (rejected)
            writer.write("PR004,ITM006,RAM001,Corsair Vengeance LPX 16GB DDR4 RAM,2,60,120\n");

            // Items for PR005
            writer.write("PR005,ITM001,LAP001,HP EliteBook G10 Laptop,1,1200,1200\n");


            System.out.println("Successfully seeded data to " + PR_ITEMS_FILE);
            writer.close();
        } catch (Exception e) {
            System.err.println("Error seeding data to " + PR_ITEMS_FILE + ": " + e.getMessage());
        }
    }

     public static void main(String[] args) {
         new PurchaseRequisitionItemSeeder().seeding();
     }
}