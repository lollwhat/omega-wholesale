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
            writer.write("PR001,IM001,LAP001,HP EliteBook G10,5,125000,625000,SUP001;SUP004\n" +
                    "PR001,IM002,MSE001,Logitech MX Master 3S,10,9550,95500,SUP001;SUP002\n" +
                    "PR002,IM003,MON001,Dell UltraSharp 27 4K,8,45000,360000,SUP001\n" +
                    "PR002,IM004,SSD001,Samsung 1TB NVMe SSD,20,8500,170000,SUP001;SUP003\n" +
                    "PR003,IM005,KBD002,Keychron K2 Mechanical Keyboard,15,16075,241125,SUP002\n" +
                    "PR003,IM002,MSE001,Logitech MX Master 3S,5,9550,47750,SUP001;SUP002");

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