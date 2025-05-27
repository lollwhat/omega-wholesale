package util.table;

@FunctionalInterface
public interface RowDataMapper {
    Object[] mapFieldsToRow(String[] dataFields, int expectedColumnCount);
}
