package com.example.laptoplist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
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

    private String[] headers = {
            "Brand", "Screen", "Rozdzielczosc", "Finish", "Touchscreen", "Processor", "Processor Cores", "Speed",
            "RAM", "Memory", "Hard Drive Type", "Graphics Card", "Graphics Memory", "Operating System",
            "Optical Drive"
    };
    List<Object[]> computers = new ArrayList<>();

    private int howManyNull = 0;
    private boolean ifReadDataButtonClicked = false;

    public JFrameWindow() {
        setTitle("Integracja Systemów - Radosław Młynek");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        defaultTableModel = new DefaultTableModel(null, headers);
        jTable = new JTable(defaultTableModel);

        jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 60, 10));

        readDataButton = new JButton("Read data");

        readDataButton.addActionListener(e -> {
            howManyNull = 0;
            ifReadDataButtonClicked = true;
            defaultTableModel.setRowCount(0);
            readData(headers).forEach(computer -> defaultTableModel.addRow(computer));
            ifReadDataButtonClicked = false;
        });

        writeDataButton = new JButton("Write data");
        writeDataButton.addActionListener(e -> {
            if (howManyNull == 0)
                saveData();
            else {
                JOptionPane.showMessageDialog(
                        null,
                        "Please enter correct data!",
                        "Alert",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        readXMLButton = new JButton("Import data from XML");
        readXMLButton.addActionListener(e -> {
            howManyNull = 0;
            ifReadDataButtonClicked = true;
            defaultTableModel.setRowCount(0);
            readDataFromXML().forEach(computer -> defaultTableModel.addRow(computer));
            ifReadDataButtonClicked = false;
        });

        writeXMLButton = new JButton("Export data to XML");
        writeXMLButton.addActionListener(e -> {
            if (howManyNull == 0)
                saveDataToXML();
            else {
                JOptionPane.showMessageDialog(
                        null,
                        "Please enter correct data!",
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
                            "This cannot be null!",
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

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JScrollPane(jTable), BorderLayout.CENTER);
        contentPane.add(jPanel, BorderLayout.NORTH);

        setSize(1000, 300);
        setLocationRelativeTo(null);
        setVisible(true);
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
                    "Cannot find any file!",
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
                    "Cannot find any file!",
                    "Alert",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return computers;
    }

    public static void main(String[] args) {
        new JFrameWindow();
    }
}
