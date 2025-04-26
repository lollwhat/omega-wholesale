package controller;

import model.EntityType;

import java.util.List;

public abstract class CRUDController<T> {
    protected String filePath;
    protected EntityType entityType;
    protected FileController fileController;

    CRUDController(String filePath, EntityType entityType) {
        this.filePath = filePath;
        this.entityType = entityType;
        this.fileController = new FileController(filePath);
    }

    public List<String> getAll() {
        try {
            List<String> lines = FileController.getFile();
            if (lines == null || lines.isEmpty()) {
                System.out.println("No "+entityType.getDisplayName()+" found");
                return null;
            }
            return lines;
        } catch (Exception e) {
            System.out.println("Error reading all "+entityType.getDisplayName()+" from file: " + e.getMessage());
            return null;
        }
    }

    public String getOneWithId(String id) {
        try {
            String[] details = fileController.getLine(0, id);
            if (details != null) {
                System.out.println(entityType.getDisplayName()+" found: " + String.join(",", details));
                return String.join(",", details);
            } else {
                System.out.println(entityType.getDisplayName()+" not found");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error reading the "+entityType.getDisplayName()+" from file: " + e.getMessage());
            return null;
        }
    }

    public abstract void add(T data);

    public abstract void update(T data);

    public void delete(String id) {
        try {
            fileController.deleteLine(id, 0);
            System.out.println(entityType.getDisplayName()+" deleted successfully");
        } catch (Exception e) {
            System.out.println("Error deleting "+entityType.getDisplayName()+" from file: " + e.getMessage());
        }
    }
}
