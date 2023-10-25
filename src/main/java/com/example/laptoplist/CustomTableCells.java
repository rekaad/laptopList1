package com.example.laptoplist;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class CustomTableCells extends DefaultTableCellRenderer
{

    private List<Integer> changedRows;
    private List<Integer> editedRows;
    private int editedRow = 0;

    public CustomTableCells(List<Integer> changedRows, List<Integer> editedRows) {
        this.changedRows = changedRows;
        this.editedRows = editedRows;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (changedRows.contains(row)) {
            c.setBackground(Color.RED);
        } else {
            c.setBackground(Color.GRAY);
        }
        if (editedRows.contains(row)) {
            c.setBackground(Color.WHITE);
        }

        if (editedRows.size() > 0 && editedRows.contains(row)) {
            c.setBackground(Color.WHITE);
        }

        return c;
    }

}
