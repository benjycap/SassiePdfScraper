import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Extension of AbstractTableModel which serves as the table model for our GUI.
 */
public class ShopTableModel extends AbstractTableModel {

    private static final String[] columnNames = {"Download", "ID", "Shop Date", "Location", "Status"};

    private ArrayList<Shop> shops;

    public ShopTableModel(ArrayList<Shop> shops) {
        if (shops == null)
            shops = new ArrayList<Shop>();
        this.shops = new ArrayList<Shop>(shops);
    }

    public ArrayList<Shop> getShops() {
        return shops;
    }

    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public int getRowCount() { return shops.size();}

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            shops.get(rowIndex).setForDownload((Boolean)aValue);
    }

    // Returning Boolean class here results in a tick box implementation for the relevant column
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return Boolean.class;
        return super.getColumnClass(columnIndex);
    }

    @Override
    public Object getValueAt(int row, int col) {
        Shop currentRow = shops.get(row);
        switch (col) {
            case 0:
                return currentRow.isForDownload();
            case 1:
                return currentRow.getId();
            case 2:
                return Shop.dateToString(currentRow.getShopDate());
            case 3:
                return currentRow.getLocation();
            case 4:
                return currentRow.getStatus();
            default:
                return null;
        }
    }

}
