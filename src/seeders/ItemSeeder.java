package seeders;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class ItemSeeder {
    final String itemDetailsFile = "data/item_details.txt";

    public ItemSeeder() {
        // Default constructor
    }

    public void seeding() {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(itemDetailsFile, false));
            writer.write("IM001,LAP001,HP EliteBook G10,piece,1250.00,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM002,MSE001,Logitech MX Master 3S,piece,95.50,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM003,MON001,Dell UltraSharp 27 4K,unit,450.00,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM004,SSD001,Samsung 1TB NVMe SSD,piece,85.00,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM005,KBD002,Keychron K2 Mechanical Keyboard,piece,160.75,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM006,RAM001,Corsair Vengeance LPX 16GB DDR4,kit,70.00,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM007,WEBCAM01,Logitech C920 HD Pro Webcam,piece,60.00,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM008,PRN001,Canon PIXMA G3000 Printer,unit,180.00,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM009,HDD001,Seagate BarraCuda 2TB HDD,piece,55.00,"+createdAt+","+createdAt+",sales1,sales1\n");
            System.out.println("Successfully Seeded data to item_details.txt");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error seeding data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ItemSeeder seeder = new ItemSeeder();
        seeder.seeding();
    }
}
