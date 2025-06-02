package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StockSeeder {
    final String stockFile = "data/stock_details.txt";

    public StockSeeder() {
        // Default constructor
    }

    public void seeding() {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(stockFile, false));
            writer.write("ST001,LAP001,HP EliteBook G10,171,20,1000,In Stock,"+createdAt+"\n" +
                    "ST002,MSE001,Logitech MX Master 3S,26,10,500,Low Stock,"+createdAt+"\n" +
                    "ST003,MON001,Dell UltraSharp 27 4K,223,20,1000,In Stock,"+createdAt+"\n" +
                    "ST004,SSD001,Samsung 1TB NVMe SSD,80,15,600,In Stock,"+createdAt+"\n" +
                    "ST005,KBD002,Keychron K2 Mechanical Keyboard,35,10,300,Low Stock,"+createdAt+"\n" +
                    "ST006,RAM001,Corsair Vengeance LPX 16GB DDR4,0,5,200,Out of Stock,"+createdAt+"\n" +
                    "ST007,WEBCAM01,Logitech C920 HD Pro Webcam,45,10,400,In Stock,"+createdAt+"\n" +
                    "ST008,PRN001,Canon PIXMA G3000 Printer,15,5,150,Low Stock,"+createdAt+"\n" +
                    "ST009,HDD001,Seagate BarraCuda 2TB HDD,120,20,800,In Stock,"+createdAt+"\n");
            System.out.println("Successfully Seeded data to stock_details.txt");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error seeding data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        StockSeeder seeder = new StockSeeder();
        seeder.seeding();
    }
}
