package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DailySalesEntrySeeder {
    private String DSEDetailsFile = "data/daily_sales_entry.txt";
    private String createdAt;

    public DailySalesEntrySeeder() {
        // Default constructor
    }

    public void seeding() {
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(DSEDetailsFile, false));
            writer.write("DSE001,Seeder Item ID A,Seeder Item Name A,Seeder Sales Date A,20,60,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("DSE002,Seeder Item ID B,Seeder Item Name B,Seeder Sales Date B,30,90,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("DSE003,Seeder Item ID C,Seeder Item Name C,Seeder Sales Date C,40,120,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("DSE004,Seeder Item ID D,Seeder Item Name D,Seeder Sales Date D,50,150,"+createdAt+",sales1,"+createdAt+",sales1\n");
            writer.write("DSE005,Seeder Item ID E,Seeder Item Name E,Seeder Sales Date E,60,180,"+createdAt+",sales1,"+createdAt+",sales1\n");
            System.out.println("Successfully seeded data to daily_sales_entry.txt");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
