package com.example.laptoplist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JFrameWindow extends JFrame {


    private final String fileName = "laptopy.txt";
    private final String splitter = ";";

    private JTable jTable;
    private DefaultTableModel defaultTableModel;
    private JPanel jPanel;
    private JButton readDataButton;
    private JButton writeDataButton;
    private JButton writeXMLButton;
    private JButton readXMLButton;
    private JButton writeDatabaseButton;
    private JButton readDatabaseButton;
    private JLabel copiesLabel;
    private JLabel newValuesLabel;

    private String[] headers = {
            "Marka", "Ekran", "Rozdzielczosc", "Typ matrycy", "Czy ekran dotykowy", "Procesor", "Liczba rdzeni fizycznych", "Taktowanie",
            "RAM", "Pojemnosc dysku", "Typ dysku", "Karta graficzna", "Pamięć karty graficznej", "System operacyjny",
            "Napęd optyczny"
    };
    List<Object[]> computers = new ArrayList<>();

    private List<Object[]> previousComputers = new ArrayList<>();
    private List<Integer> sameRows = new ArrayList<>();
    private List<Integer> editedRows = new ArrayList<>();

    private int howManyNull = 0;
    private boolean ifReadDataButtonClicked = false;

    public JFrameWindow() {
        setTitle("Integracja Systemów - Radosław Młynek");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        defaultTableModel = new DefaultTableModel(null, headers);
        jTable = new JTable(defaultTableModel);

        jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 60, 10));

        readDataButton = new JButton("Wczytaj plik txt");

        readDataButton.addActionListener(e -> {
            editedRows = new ArrayList<>();
            setPreviousComputerList();

            howManyNull = 0;
            ifReadDataButtonClicked = true;
            defaultTableModel.setRowCount(0);
            readData(headers).forEach(computer -> defaultTableModel.addRow(computer));
            ifReadDataButtonClicked = false;
            countCopies();
        });

        writeDataButton = new JButton("Zapisz plik txt");
        writeDataButton.addActionListener(e -> {
            if (howManyNull == 0)
                saveData();
            else {
                JOptionPane.showMessageDialog(
                        null,
                        "Wprowadz poprawne dane!",
                        "Alert",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        readXMLButton = new JButton("Wczytaj dane z pliku XML");
        readXMLButton.addActionListener(e -> {
            editedRows = new ArrayList<>();
            setPreviousComputerList();

            howManyNull = 0;
            ifReadDataButtonClicked = true;
            defaultTableModel.setRowCount(0);
            readDataFromXML().forEach(computer -> defaultTableModel.addRow(computer));
            ifReadDataButtonClicked = false;
            countCopies();
        });

        writeXMLButton = new JButton("Zapisz dane w pliku XML");
        writeXMLButton.addActionListener(e -> {
            if (howManyNull == 0)
                saveDataToXML();
            else {
                JOptionPane.showMessageDialog(
                        null,
                        "Wprowadz poprawne dane!",
                        "Alert",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });


        readDatabaseButton = new JButton("Wczytaj dane z bazy");
        readDatabaseButton.addActionListener(e -> {
            editedRows = new ArrayList<>();
            setPreviousComputerList();

            howManyNull = 0;
            ifReadDataButtonClicked = true;
            defaultTableModel.setRowCount(0);
            try {
                DatabaseOperations.readData(computers).forEach(computer -> defaultTableModel.addRow(computer));
            } catch (ClassNotFoundException | SQLException ex) {
                throw new RuntimeException(ex);
            }
            ifReadDataButtonClicked = false;

            countCopies();
        });

        writeDatabaseButton = new JButton("Zapisz dane do bazy");
        writeDatabaseButton.addActionListener(e -> {
            if (howManyNull == 0) {
                try {
                    if (editedRows.size() == 0)
                        DatabaseOperations.saveData(defaultTableModel, splitter);
                    else {
                        for (Integer id : editedRows)
                            DatabaseOperations.updateData(defaultTableModel, splitter, id);
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Wprowadz poprawne dane!",
                        "Alert",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        jTable.getModel().addTableModelListener(e -> {
            DefaultTableModel tableModel = (DefaultTableModel) e.getSource();
            if (tableModel.getRowCount() > 0 && !ifReadDataButtonClicked) {
                Object value = tableModel.getValueAt(e.getFirstRow(), e.getColumn());
                if (value.equals("")) {
                    howManyNull++;
                    JOptionPane.showMessageDialog(
                            null,
                            "Pole nie moze byc puste!",
                            "Alert",
                            JOptionPane.INFORMATION_MESSAGE);
                } else if (howManyNull != 0)
                    howManyNull--;
            }
        });

        jPanel.add(readDataButton);
        jPanel.add(writeDataButton);
        jPanel.add(readXMLButton);
        jPanel.add(writeXMLButton);
        jPanel.add(readDatabaseButton);
        jPanel.add(writeDatabaseButton);

        newValuesLabel = new JLabel("Nowe wartosci = 0");
        copiesLabel = new JLabel("Kopie = 0");

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.add(newValuesLabel);
        infoPanel.add(copiesLabel);

        jPanel.add(infoPanel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JScrollPane(jTable), BorderLayout.CENTER);
        contentPane.add(jPanel, BorderLayout.NORTH);

        setSize(1600, 500);
        setLocationRelativeTo(null);
        setVisible(true);


        defaultTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                Object newValue = defaultTableModel.getValueAt(row, column);
                Object oldValue = computers.get(row)[column];

                if (!newValue.equals(oldValue)) {
                    editedRows.add(row);
                    countCopies();
                }
            }
        });
    }

    public List<Object[]> readData(String[] headers) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            computers.clear();
            while ((line = bufferedReader.readLine()) != null) {
                List<String> data = new ArrayList<>(Arrays.asList(line.split(splitter)));

                int rightCounter = 0;

                if (data.size() < headers.length) {
                    for (int i = 0; i < headers.length - data.size(); i++)
                        data.add("-");
                }

                Object[] computer = new Object[15];
                for (String property : data) {
                    if (property.isBlank())
                        property = "-";
                    computer[rightCounter] = property;
                    rightCounter++;
                }
                computers.add(computer);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Nie mozna znalezc pliku!",
                    "Alert",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        return computers;
    }

    public void saveData() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
                for (int j = 0; j < defaultTableModel.getColumnCount(); j++) {
                    Object value = defaultTableModel.getValueAt(i, j);
                    if (value != null)
                        bufferedWriter.write(value.toString());

                    if (j < defaultTableModel.getColumnCount() - 1)
                        bufferedWriter.write(splitter);
                }
                if (defaultTableModel.getRowCount() - 1 != i)
                    bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToXML() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element laptops = document.createElement("laptops");

            int counter = 0;
            for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
                Element laptop = document.createElement("laptop");

                Element manufacturer = document.createElement("manufacturer");
                manufacturer.setTextContent((String) defaultTableModel.getValueAt(i, counter++));

                Element screen = document.createElement("screen");

                Element size = document.createElement("size");
                Element resolution = document.createElement("resolution");
                Element type = document.createElement("type");

                size.setTextContent((String) defaultTableModel.getValueAt(i, counter++));
                resolution.setTextContent((String) defaultTableModel.getValueAt(i, counter++));
                type.setTextContent((String) defaultTableModel.getValueAt(i, counter++));

                screen.setAttribute("touch", (String) defaultTableModel.getValueAt(i, counter++));

                Element processor = document.createElement("processor");

                Element name = document.createElement("name");
                Element physicalCores = document.createElement("physical_cores");
                Element clockSpeed = document.createElement("clock_speed");

                name.setTextContent((String) defaultTableModel.getValueAt(i, counter++));
                physicalCores.setTextContent((String) defaultTableModel.getValueAt(i, counter++));
                clockSpeed.setTextContent((String) defaultTableModel.getValueAt(i, counter++));

                Element ram = document.createElement("ram");
                ram.setTextContent((String) defaultTableModel.getValueAt(i, counter++));

                Element disc = document.createElement("disc");

                Element storage = document.createElement("storage");
                storage.setTextContent((String) defaultTableModel.getValueAt(i, counter++));

                disc.setAttribute("type", (String) defaultTableModel.getValueAt(i, counter++));

                Element graphicCard = document.createElement("graphic_card");

                Element name2 = document.createElement("name");
                Element memory = document.createElement("memory");

                name2.setTextContent((String) defaultTableModel.getValueAt(i, counter++));
                memory.setTextContent((String) defaultTableModel.getValueAt(i, counter++));

                Element os = document.createElement("os");
                os.setTextContent((String) defaultTableModel.getValueAt(i, counter++));

                Element discReader = document.createElement("disc_reader");
                discReader.setTextContent((String) defaultTableModel.getValueAt(i, counter));
                counter = 0;

                screen.appendChild(size);
                screen.appendChild(resolution);
                screen.appendChild(type);

                processor.appendChild(name);
                processor.appendChild(physicalCores);
                processor.appendChild(clockSpeed);

                disc.appendChild(storage);

                graphicCard.appendChild(name2);
                graphicCard.appendChild(memory);

                laptop.appendChild(manufacturer);
                laptop.appendChild(screen);
                laptop.appendChild(processor);
                laptop.appendChild(ram);
                laptop.appendChild(disc);
                laptop.appendChild(graphicCard);
                laptop.appendChild(os);
                laptop.appendChild(discReader);

                laptops.appendChild(laptop);
            }

            document.appendChild(laptops);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream("laptopy.xml")));
        } catch (ParserConfigurationException | TransformerException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Object[]> readDataFromXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("laptopy.xml"));
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("laptop");
            int counter = 0;

            computers.clear();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element laptopElement = (Element) nodeList.item(i);
                Element screenElement = (Element) laptopElement.getElementsByTagName("screen").item(0);
                Element processorElement = (Element) laptopElement.getElementsByTagName("processor").item(0);
                Element discElement = (Element) laptopElement.getElementsByTagName("disc").item(0);
                Element graphicCardElement = (Element) laptopElement.getElementsByTagName("graphic_card").item(0);

                Object[] computer = new Object[15];

                computer[counter++] = laptopElement.
                        getElementsByTagName("manufacturer").item(0).getTextContent();
                computer[counter++] = screenElement.getElementsByTagName("size").item(0).getTextContent();
                computer[counter++] = screenElement.getElementsByTagName("resolution").item(0).getTextContent();
                computer[counter++] = screenElement.getElementsByTagName("type").item(0).getTextContent();
                computer[counter++] = screenElement.getAttribute("touch");
                computer[counter++] = processorElement.getElementsByTagName("name").item(0).getTextContent();
                computer[counter++] = processorElement.
                        getElementsByTagName("physical_cores").item(0).getTextContent();
                computer[counter++] = processorElement.
                        getElementsByTagName("clock_speed").item(0).getTextContent();
                computer[counter++] = laptopElement.getElementsByTagName("ram").item(0).getTextContent();
                computer[counter++] = discElement.getElementsByTagName("storage").item(0).getTextContent();
                computer[counter++] = discElement.getAttribute("type");
                computer[counter++] = graphicCardElement.getElementsByTagName("name").item(0).getTextContent();
                computer[counter++] = graphicCardElement.getElementsByTagName("memory").item(0).getTextContent();
                computer[counter++] = laptopElement.getElementsByTagName("os").item(0).getTextContent();
                computer[counter] = laptopElement.getElementsByTagName("disc_reader").item(0).getTextContent();
                counter = 0;

                computer = Arrays.stream(computer).map(s -> s.equals("") ? "-" : s).toArray(Object[]::new);

                computers.add(computer);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Nie mozna znalezc pliku!",
                    "Alert",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return computers;
    }

    public void countCopies() {
        if (previousComputers.size() > 0) {
            int copiesNumber = 0;
            sameRows = new ArrayList<>();
            for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
                Object[] row = new Object[defaultTableModel.getColumnCount()];

                for (int j = 0; j < defaultTableModel.getColumnCount(); j++)
                    row[j] = defaultTableModel.getValueAt(i, j);

                int counter = 0;
                for (Object[] computer : previousComputers) {
                    if (Arrays.equals(computer, row) && counter == i) {
                        copiesNumber++;
                        sameRows.add(i);
                        break;
                    }
                    counter++;
                }
            }
            newValuesLabel.setText("Nowe wartosci = " + (defaultTableModel.getRowCount() - copiesNumber));
            copiesLabel.setText("Kopie = " + copiesNumber);

            jTable.setDefaultRenderer(Object.class, new CustomTableCells(sameRows, editedRows));
        } else {
            jTable.setDefaultRenderer(Object.class, new CustomTableCells(sameRows, editedRows));
            newValuesLabel.setText("Nowe wartosci = " + defaultTableModel.getRowCount());
            copiesLabel.setText("Kopie = " + 0);
        }
    }

    public void setPreviousComputerList() {
        previousComputers = new ArrayList<>();
        for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
            Object[] row = new Object[defaultTableModel.getColumnCount()];
            for (int j = 0; j < defaultTableModel.getColumnCount(); j++) {
                row[j] = defaultTableModel.getValueAt(i, j);
            }
            previousComputers.add(row);
        }
    }

    public static void main(String[] args) {
        new JFrameWindow();
    }
}
