package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SupplierSeeder {
    private String itemDetailsFile = "data/supplier_details.txt";
    private String createdAt;

    public SupplierSeeder() {
        // Default constructor
    }

    public void seeding() {
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(itemDetailsFile, false));
            writer.write("SUP001,Seeder Supplier A,Seeder PIC A,Seeder Description A,Seeder Address A,08123456789,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP002,Seeder Supplier B,Seeder PIC B,Seeder Description B,Seeder Address B,08123456789,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP003,Seeder Supplier C,Seeder PIC C,Seeder Description C,Seeder Address C,08123456789,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP004,Seeder Supplier D,Seeder PIC D,Seeder Description D,Seeder Address D,08123456789,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP005,Seeder Supplier E,Seeder PIC E,Seeder Description E,Seeder Address E,08123456789,"+createdAt+","+createdAt+",sales1,sales1\n");
            System.out.println("Successfully Seeded data to supplier_details.txt");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
