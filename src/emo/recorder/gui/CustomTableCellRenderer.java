package emo.recorder.gui;
import java.awt.Component;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof String) {
            String amount = (String) value;
            if (column == 5 || column==6) {
                if (amount.trim().startsWith("A")) {
                    cell.setBackground(Color.pink);
                    // you can also customize the Font and Foreground this way
                    // cell.setForeground();
                    // cell.setFont();
                }

                else {
                    cell.setBackground(Color.white);
                }
            } else {
                cell.setBackground(Color.white);
            }

        }

        return cell;

    }
}
