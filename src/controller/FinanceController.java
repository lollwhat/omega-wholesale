package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FinanceController {
    private static final String FINANCE_DETAILS_FILE = "data/finance_details.txt";
    private static final String FINANCE_DETAILS_HEADER = "TransactionID,PO_ID,ItemID,ItemName,SupplierID,SupplierName,AmountPaid,PaymentDate,ProcessedByUser";

    private String processedByUser;

    public FinanceController() {
        try {
            Path path = Paths.get(FINANCE_DETAILS_FILE);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.writeString(path, FINANCE_DETAILS_HEADER + System.lineSeparator(), StandardOpenOption.CREATE_NEW);
                System.out.println(FINANCE_DETAILS_FILE + " created with header.");
            } else if (Files.size(path) == 0) { // File exists but is empty
                Files.writeString(path, FINANCE_DETAILS_HEADER + System.lineSeparator(), StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println(FINANCE_DETAILS_FILE + " header written to empty file.");
            }
        } catch (IOException e) {
            System.err.println("Error initializing finance details file: " + e.getMessage());
        }
    }

    public boolean recordTransactions(List<Map<String, String>> transactions) {
         SessionController session = SessionController.getInstance();
         if (session.isLoggedIn()) {
             processedByUser = session.getUserId();
         }


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FINANCE_DETAILS_FILE, true))) {
            for (Map<String, String> transaction : transactions) {
                long transactionId = System.currentTimeMillis(); // Simple unique ID
                String paymentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                String line = String.join(",",
                        "TXN" + transactionId,
                        transaction.getOrDefault("PO_ID", "N/A"),
                        transaction.getOrDefault("ItemID", "N/A"),
                        transaction.getOrDefault("ItemName", "N/A").replace(",", ";"),
                        transaction.getOrDefault("SupplierID", "N/A"),
                        transaction.getOrDefault("SupplierName", "N/A").replace(",", ";"),
                        transaction.getOrDefault("AmountPaid", "0"),
                        paymentDate,
                        processedByUser
                );
                writer.write(line);
                writer.newLine();
            }
            System.out.println(transactions.size() + " financial transactions recorded successfully.");
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to finance_details.txt: " + e.getMessage());
            return false;
        }
    }

    public void generateFinancialTransactionsReport(String savePath) throws IOException {
        Path stockFilePath = Paths.get(FINANCE_DETAILS_FILE);

        if (!Files.exists(stockFilePath)) {
            throw new IOException("Stock file not found.");
        }

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Stock ID,Item Code, Item Name,Current Stock,Min Stock,Max Stock,Status,Date\n");
        for (String line : Files.readAllLines(stockFilePath)) {
            reportContent.append(line).append("\n");
        }

        try (FileWriter writer = new FileWriter(savePath)) {
            writer.write(reportContent.toString());
        }
    }
}