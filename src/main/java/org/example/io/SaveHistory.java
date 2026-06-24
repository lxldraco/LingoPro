package org.example.io;

import org.example.process.SearchCondition;
import org.example.ui.MainFrame;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SaveHistory {
    // List to store history records (sorted in reverse chronological order)
    private static final List<HistoryItem> historyItems = new ArrayList<>();

    // Inner class for history record items
    public static class HistoryItem {
        private final String time;
        private final String source;
        private final String documentName;
        private final SearchCondition condition;
        private final int matchCount;

        public HistoryItem(String source, SearchCondition condition, int matchCount) {
            this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.source = source;
            this.condition = condition;
            this.matchCount = matchCount;

            // Extract the document name
            if (source.startsWith("https://en.wikipedia.org/wiki/")) {
                this.documentName = source.substring("https://en.wikipedia.org/wiki/".length());
            } else {
                File file = new File(source);
                this.documentName = file.getName();
            }
        }

        public String getTime() {
            return time;
        }

        public String getDocumentName() {
            return documentName;
        }

        public SearchCondition getCondition() {
            return condition;
        }

        public int getMatchCount() {
            return matchCount;
        }

        public String getSource() {
            return source;
        }

        public String getFormattedCondition() {
            return formatSearchCondition(condition);
        }

        private String formatSearchCondition(SearchCondition condition) {
            StringBuilder sb = new StringBuilder();
            if (condition.getExactWord() != null && !condition.getExactWord().isEmpty()) {
                sb.append("Exact: ").append(condition.getExactWord());
            }
            if (condition.getWordLemma() != null && !condition.getWordLemma().isEmpty()) {
                if (sb.length() > 0) sb.append("; ");
                sb.append("Lemma: ").append(condition.getWordLemma());
            }
            if (condition.getPosTag() != null && !condition.getPosTag().isEmpty()) {
                if (sb.length() > 0) sb.append("; ");
                sb.append("POS: ").append(condition.getPosTag());
            }
            if (condition.getRegex() != null && !condition.getRegex().isEmpty()) {
                if (sb.length() > 0) sb.append("; ");
                sb.append("Regex: ").append(condition.getRegex());
            }
            if (condition.isNeighbored()) {
                if (sb.length() > 0) sb.append("; ");
                sb.append("Neighbors: ").append(condition.getLeftNeighbor()).append("/").append(condition.getRightNeighbor());
            }
            if (condition.isCaseSensitive()) {
                if (sb.length() > 0) sb.append("; ");
                sb.append("Case Sensitive");
            }

            return sb.toString();
        }
    }

    // Add a new history record (add to the beginning of the list to achieve reverse order)
    public static void addHistoryItem(String source, SearchCondition condition, int matchCount) {
        // Add to the beginning of the list to ensure the latest record is at the top
        historyItems.add(0, new HistoryItem(source, condition, matchCount));
    }

    // Get all current history records (sorted in reverse chronological order)
    public static List<HistoryItem> getHistoryItems() {
        return new ArrayList<>(historyItems);
    }

    // Clear all history records
    public static void clearHistory() {
        historyItems.clear();
    }

    // Save history records to a file
    public static void saveHistoryToFile() {
        if (historyItems.isEmpty()) {
            MainFrame.updateStatus("No history to save");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Search History");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_history.xml";
            File outputFile = new File(selectedDir, fileName);

            try {
                saveToXml(outputFile);
                MainFrame.updateStatus("History saved to: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                MainFrame.updateStatus("Failed to save history: " + e.getMessage());
            }
        }
    }

    // Save history records as XML
    private static void saveToXml(File file) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("SearchHistory");
        doc.appendChild(rootElement);

        // Sort the records in chronological order (latest record at the end) when saving
        List<HistoryItem> reverseOrder = new ArrayList<>(historyItems);
        Collections.reverse(reverseOrder);

        for (HistoryItem item : reverseOrder) {
            Element historyElement = doc.createElement("HistoryItem");
            rootElement.appendChild(historyElement);

            addElement(doc, historyElement, "Time", item.getTime());
            addElement(doc, historyElement, "Source", item.getSource());
            addElement(doc, historyElement, "DocumentName", item.getDocumentName());
            addElement(doc, historyElement, "MatchCount", String.valueOf(item.getMatchCount()));

            // Save search conditions
            Element conditionElement = doc.createElement("SearchCondition");
            historyElement.appendChild(conditionElement);

            SearchCondition cond = item.getCondition();
            addConditionElement(doc, conditionElement, "ExactWord", cond.getExactWord());
            addConditionElement(doc, conditionElement, "WordLemma", cond.getWordLemma());
            addConditionElement(doc, conditionElement, "PosTag", cond.getPosTag());
            addConditionElement(doc, conditionElement, "Regex", cond.getRegex());
            addConditionElement(doc, conditionElement, "CaseSensitive",
                    String.valueOf(cond.isCaseSensitive()));
            addConditionElement(doc, conditionElement, "Neighbored",
                    String.valueOf(cond.isNeighbored()));
            addConditionElement(doc, conditionElement, "LeftNeighbor",
                    String.valueOf(cond.getLeftNeighbor()));
            addConditionElement(doc, conditionElement, "RightNeighbor",
                    String.valueOf(cond.getRightNeighbor()));
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    private static void addElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.appendChild(doc.createTextNode(value));
        parent.appendChild(element);
    }

    private static void addConditionElement(Document doc, Element parent, String name, String value) {
        if (value != null && !value.isEmpty()) {
            Element element = doc.createElement(name);
            element.appendChild(doc.createTextNode(value));
            parent.appendChild(element);
        }
    }
}