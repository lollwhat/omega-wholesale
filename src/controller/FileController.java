package controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileController {
    private static String filePath;
    private static List<String> fileLines = new ArrayList<>();
    private static List<String> targetLines = new ArrayList<>();
    private List<String> updatedLines = new ArrayList<>();
    private static FileWriter fileWriter;


    public FileController(String filePath) {
        this.filePath = filePath;
    }

    // Method to read file
    public static List<String> readFile(){
        try{
            fileLines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static List<String> getFile(){
        readFile();
        return fileLines;
    }

    // Method to get a single line of data
    public String[] getLine(int index, String identifier) throws IOException {
        readFile();
        for (String line : fileLines) {
            String[] parts = line.split(",");
            if (parts[index].equals(identifier)) {
                return parts;
            }
        }
        return null;
    }

    // Method to get multiple lines of data
    public List<String> getLines(int index, String identifier) throws IOException {
        targetLines.clear();
        readFile();
        for (String line : fileLines) {
            String[] parts = line.split(",");
            if (parts[index].equals(identifier)) {
                targetLines.add(parts[index]);
            }
        }
        return targetLines;
    }

    // Method to write data into file
    public static void appendFile(String data){
        try{
            readFile();
            boolean needsNewLine = false;

            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            raf.seek(raf.length() - 1);
            char lastChar = (char) raf.read();
            if(lastChar != '\n'){
                needsNewLine = true;
            }
            raf.close();

            fileWriter = new FileWriter(filePath, true);
            if(needsNewLine){
                fileWriter.write("\n");
            }

            fileWriter.write(data);
            fileWriter.write("\n"); // next data start on new line
            System.out.println("Data appended successfully.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    System.err.println("IOException caught while closing FileWriter: " + e.getMessage());
                }
            }
        }
    }

    public void updateFile(String updatedLine) { // file update
        readFile();
        updatedLines = new ArrayList<>();
        String[] updatedData = updatedLine.split(",");
        for (String line : fileLines) {
            String[] data = line.split(",");
            if (updatedData[0].equals(data[0])) {
                updatedLines.add(updatedLine);
            } else {
                updatedLines.add(line);
            }
        }

        try {
            fileWriter = new FileWriter(filePath, false);
            for (String line : updatedLines) {
                fileWriter.write(line + "\n");
            }
            System.out.println("Data appended successfully.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e.getMessage());
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    System.err.println("IOException caught while closing FileWriter: " + e.getMessage());
                }
            }
        }
    }

    public void deleteLine(String dataId, int index) {
        readFile();
        updatedLines = new ArrayList<>();
        for (String line : fileLines) {
            String[] data = line.split(",");
            if (!dataId.equals(data[index])) {
                updatedLines.add(line);
            }
        }
        try {
            fileWriter = new FileWriter(filePath, false);
            for (String line : updatedLines) {
                fileWriter.write(line + "\n");
            }
            System.out.println("Data deleted successfully.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e.getMessage());
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    System.err.println("IOException caught while closing FileWriter: " + e.getMessage());
                }
            }
        }
    }

    // Method to get ID (no format, e.g. 001, 002, 003, etc.)
    public Long getId(){
        Long newId = null;
        try {
            newId = Long.valueOf(getLastId());
        } catch (IOException ex) {
            Logger.getLogger(FileController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newId;
    }

    // Method to get last ID (001, 002, 003, etc.)
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

    // Method to get the last user ID by role (e.g "AM", "SM", etc.)
    public static Map<String, String> getLastUserIdByRole() throws IOException {
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