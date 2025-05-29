package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ItemSupplierSeeder {
    private String itemSupplierFile = "data/item_supplier.txt";

    public ItemSupplierSeeder() {
        // Default constructor
    }

    public void seeding() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(itemSupplierFile, false));
            // IM001 (HP Laptop) can be supplied by SUP001 and SUP004
            writer.write("IM001,SUP001\n");
            writer.write("IM001,SUP004\n");
            // IM002 (Logitech Mouse) by SUP002 and SUP001
            writer.write("IM002,SUP002\n");
            writer.write("IM002,SUP001\n");
            // IM003 (Dell Monitor) by SUP001 and SUP004
            writer.write("IM003,SUP001\n");
            writer.write("IM003,SUP004\n");
            // IM004 (Samsung SSD) by SUP001 and SUP003
            writer.write("IM004,SUP001\n");
            writer.write("IM004,SUP003\n");
            // IM005 (Keychron Keyboard) by SUP002
            writer.write("IM005,SUP002\n");
            // IM006 (Corsair RAM) by SUP003
            writer.write("IM006,SUP003\n");
            // IM007 (Logitech Webcam) by SUP001 and SUP002
            writer.write("IM007,SUP001\n");
            writer.write("IM007,SUP002\n");
            // IM008 (Canon Printer) by SUP005
            writer.write("IM008,SUP005\n");
            // IM009 (Seagate HDD) by SUP003 and SUP001
            writer.write("IM009,SUP003\n");
            writer.write("IM009,SUP001\n");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error seeding data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ItemSupplierSeeder seeder = new ItemSupplierSeeder();
        seeder.seeding();
    }
}
