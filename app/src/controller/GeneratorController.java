package controller;

import java.io.*;

public class GeneratorController {
    private static final String user_details = "";

    public GeneratorController(String filePath) {
        this.filePath = filePath;
    }

    public Long getId(){
        Long newId = null;
        try {
            newId = Long.valueOf(getLastId());
        } catch (IOException ex) {
            Logger.getLogger(IdGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newId;
    }

    private String getLastId() throws IOException { // get last id for id generate
        String lastLine = null;
        String lastId = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;

            // Read through all lines to find the last one
            while ((currentLine = reader.readLine()) != null) {
                lastLine = currentLine;
            }

            if (lastLine != null) {
                String[] data = lastLine.split("\\|");
                lastId = data[0];
            } else {
                lastId = "0"; // Default if file is empty
            }
        }

        return lastId;
    }

    private String getLastId() throws IOException { // get last id for id generate
        String lastLine = null;
        String lastId = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;

            // Read through all lines to find the last one
            while ((currentLine = reader.readLine()) != null) {
                lastLine = currentLine;
            }

            if (lastLine != null) {
                String[] data = lastLine.split("\\|");
                lastId = data[0];
            } else {
                lastId = "0"; // Default if file is empty
            }
        }

        return lastId;
    }

    public Map<String, String> getLastUserIdByRole() throws IOException {
        Map<String, String> lastIdByRole = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    String userId = parts[0].trim();
                    if (userId.length() >= 4) { // Ensure userId has format XX000...
                        String rolePrefix = userId.substring(0, 2);
                        String numberPart = userId.substring(2);

                        // Check if this is a valid role ID format
                        if (numberPart.matches("\\d+")) {
                            // Update if this is the first or a higher ID for this role
                            if (!lastIdByRole.containsKey(rolePrefix) ||
                                    Integer.parseInt(numberPart) > Integer.parseInt(lastIdByRole.get(rolePrefix).substring(2))) {
                                lastIdByRole.put(rolePrefix, userId);
                            }
                        }
                    }
                }
            }
        }

        return lastIdByRole;
    }
}