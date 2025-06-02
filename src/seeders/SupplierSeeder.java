package seeders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SupplierSeeder {
    private String supplierDetailsFile = "data/supplier_details.txt";
    private String createdAt;

    public SupplierSeeder() {
        // Default constructor
    }

    public void seeding() {
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(supplierDetailsFile, false));
            writer.write("SUP001,TechGlobal Solutions,Alice Wonderland,Leading IT equipment distributor,123 Silicon Rd Tech City,15550100,sales@techglobal.com,"+1+","+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP002,Office Supplies Co.,Bob The Builder,Wholesale office stationery and peripherals,456 Paper St Commerce Town,442079460011,contact@officesupplies.co,"+1+","+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP003,Component Kings,Charlie Brown,Specialists in PC components and RAM,789 Circuit Ave Electra,61298765432,info@componentkings.au,"+0+","+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP004,Gadget Galaxy,Diana Prince,Consumer electronics and gadgets,101 Future Dr Innovation Hub,15550102,support@gadgetgalaxy.com,"+1+","+createdAt+","+createdAt+",sales1,sales1\n");
            writer.write("SUP005,PrintPerfect Ltd.,Edward Scissorhands,Printers & ink & printing consumables,222 Toner Tce Printville,4930123456,orders@printperfect.de,"+2+","+createdAt+","+createdAt+",sales1,sales1\n");
            System.out.println("Successfully Seeded data to supplier_details.txt");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error seeding data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SupplierSeeder seeder = new SupplierSeeder();
        seeder.seeding();
    }
}
