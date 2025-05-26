// seeders/PurchaseRequisitionSeeder.java (AMENDED)
package seeders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseRequisitionSeeder {
    final String PR_HEADER_FILE = "data/purchase_requisition.txt";

    public PurchaseRequisitionSeeder() {
        // Default constructor
    }

    private int getStatusInt(String statusString) {
        switch (statusString.toLowerCase()) {
            case "pending":
                return 0;
            case "approved":
                return 1;
            case "rejected":
                return 2;
            default:
                return 0; // Default to pending if unknown
        }
    }

    public void seeding() {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = "sales1"; // Example user

        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get("data"));

            BufferedWriter writer = new BufferedWriter(new FileWriter(PR_HEADER_FILE, false)); // false to overwrite

            writer.write("PR001,Seeder Notes A,SUP001," + getStatusInt("approved") + "," + createdAt + "," + createdBy + "," + createdAt + "," + createdBy + "\n");
            writer.write("PR002,Seeder Notes B,SUP001," + getStatusInt("approved") + "," + createdAt + "," + createdBy + "," + createdAt + "," + createdBy + "\n");
            writer.write("PR003,Seeder Notes C,SUP002," + getStatusInt("pending") + "," + createdAt + "," + createdBy + "," + createdAt + "," + createdBy + "\n");
            writer.write("PR004,Seeder Notes D,SUP003," + getStatusInt("rejected") + "," + createdAt + "," + createdBy + "," + createdAt + "," + createdBy + "\n");
            writer.write("PR0045Seeder Notes E,SUP001," + getStatusInt("approved") + "," + createdAt + "," + createdBy + "," + createdAt + "," + createdBy + "\n");

            System.out.println("Successfully seeded data to " + PR_HEADER_FILE);
            writer.close();
        } catch (Exception e) {
            System.err.println("Error seeding data to " + PR_HEADER_FILE + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

     public static void main(String[] args) {
         new PurchaseRequisitionSeeder().seeding();
     }
}