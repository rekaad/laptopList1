package com.example.laptoplist;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.List;

public class DatabaseOperations {

    public static void saveData(DefaultTableModel defaultTableModel, String splitter)
            throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/integration";
        String username = "root";
        String password = "root";

        Connection connection = DriverManager.getConnection(url, username, password);

        String deleteQuery = "DELETE FROM laptop_table";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
        deleteStatement.executeUpdate(deleteQuery);
        String query = "INSERT INTO laptop_table (id, marka, ekran, rozdzielczosc, typ_matrycy, czy_dotyk, procesor, " +
                "ile_rdzeni, taktowanie, ram, pojemnosc_dysku, typ_dysku, karta_graficzna, pamiec_grafika, system_operacyjny, " +
                "naped_optyczny) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = connection.prepareStatement(query);


        for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
            statement.setInt(1, i + 1);
            for (int j = 0; j < defaultTableModel.getColumnCount(); j++) {
                Object value = defaultTableModel.getValueAt(i, j);
                if (value != null)
                    statement.setString(j + 2, value.toString());
                else
                    statement.setString(j + 2, splitter);
            }
            statement.executeUpdate();
        }

        statement.close();
        connection.close();
    }

    public static void updateData(DefaultTableModel defaultTableModel, String splitter, int id)
            throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/integration";
        String username = "root";
        String password = "root";

        Connection connection = DriverManager.getConnection(url, username, password);

        String query = "UPDATE laptop_table SET marka = ?, ekran = ?, rozdzielczosc = ?, typ_matrycy = ?, czy_dotyk = ?, " +
                "procesor = ?, ile_rdzeni = ?, taktowanie = ?, ram = ?, pojemnosc_dysku = ?, typ_dysku = ?, " +
                "karta_graficzna = ?, pamiec_grafika = ?, system_operacyjny = ?, naped_optyczny = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < defaultTableModel.getColumnCount(); i++) {
            Object newCellValue = defaultTableModel.getValueAt(id, i);
            if (newCellValue != null)
                statement.setString(i + 1, newCellValue.toString());
            else
                statement.setString(i + 1, splitter);
        }
        statement.setInt(defaultTableModel.getColumnCount() + 1, id + 1);

        statement.executeUpdate();

        statement.close();
        connection.close();
    }

    public static List<Object[]> readData(List<Object[]> computers)
            throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/integration";
        String username = "root";
        String password = "root";

        Connection connection = DriverManager.getConnection(url, username, password);

        String query = "SELECT * FROM laptop_table";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        computers.clear();

        while (resultSet.next()) {
            int i = 0;
            Object[] computer = new Object[15];
            computer[i++] = resultSet.getString("marka");
            computer[i++] = resultSet.getString("ekran");
            computer[i++] = resultSet.getString("rozdzielczosc");
            computer[i++] = resultSet.getString("typ_matrycy");
            computer[i++] = resultSet.getString("czy_dotyk");
            computer[i++] = resultSet.getString("procesor");
            computer[i++] = resultSet.getString("ile_rdzeni");
            computer[i++] = resultSet.getString("taktowanie");
            computer[i++] = resultSet.getString("ram");
            computer[i++] = resultSet.getString("pojemnosc_dysku");
            computer[i++] = resultSet.getString("typ_dysku");
            computer[i++] = resultSet.getString("karta_graficzna");
            computer[i++] = resultSet.getString("pamiec_grafika");
            computer[i++] = resultSet.getString("system_operacyjny");
            computer[i++] = resultSet.getString("naped_optyczny");

            computers.add(computer);
        }

        resultSet.close();
        statement.close();
        connection.close();

        return computers;
    }


}
