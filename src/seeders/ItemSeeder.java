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
            writer.write("IM001,A12321,Seeder Item A,100,kg,100,SUP001,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM002,B12923,Seeder Item B,200,l,20,SUP002,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM003,C01028,Seeder Item C,100,kg,200,SUP001,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM004,D81891,Seeder Item D,100,kg,15,SUP001,"+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("IM005,E45642,Seeder Item E,100,kg,25.5,SUP001,"+createdAt+","+createdAt+",sales1,sales1\n");
            System.out.println("Successfully Seeded data to item_details.txt");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error seeding data: " + e.getMessage());
        }
    }
}
