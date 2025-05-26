package util.table;

@FunctionalInterface
public interface EditableCellChecker {
    boolean isCellEditable(int row, int column, int totalColumns);
}
