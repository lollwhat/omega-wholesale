package util.table;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class GenericModelHelper { // Or place this in a suitable utility class

    public static DefaultTableModel createGenericTableModel(
        List<String> dataLines,
        String[] columnNames,
        RowDataMapper rowMapper,
        EditableCellChecker cellChecker
    ) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (cellChecker != null) {
                    return cellChecker.isCellEditable(row, column, columnNames.length);
                }
                return false; // Default to not editable if no checker is provided
            }
        };

        if (dataLines == null || dataLines.isEmpty()) {
            // System.out.println("No data to display for the table."); // Optional log
            return model; // Return an empty model
        }

        for (String line : dataLines) {
            if (line == null || line.trim().isEmpty()) {
                continue; // Skip empty or null lines
            }

            String[] fields = line.split(","); // Assuming CSV. This could also be made configurable.

            // The rowMapper is responsible for handling the fields and returning an Object[]
            // of the correct length (columnNames.length).
            Object[] rowData = rowMapper.mapFieldsToRow(fields, columnNames.length);

            if (rowData != null) {
                if (rowData.length != columnNames.length) {
                    System.err.println("Error: RowDataMapper returned row with " + rowData.length +
                            " columns, but table model expects " + columnNames.length +
                            ". Skipping line: " + line);
                    continue;
                }
                model.addRow(rowData);
            }
            // If rowData is null, the mapper decided to skip this line (e.g., due to malformed data).
        }
        return model;
    }

    public static final EditableCellChecker LAST_COLUMN_EDITABLE =
            (row, column, totalColumns) -> column == (totalColumns - 1);

    public static final EditableCellChecker NO_CELLS_EDITABLE =
            (row, column, totalColumns) -> false;
}
