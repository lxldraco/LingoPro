package org.example.ui.layout;

import org.example.io.SaveHistory;
import org.example.process.LinguisticAnalysis;
import org.example.process.SearchCondition;
import org.example.service.ContentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Map;

import static org.example.ui.ComponentFactory.getComponentCss;

public class MainPanel {

    public static JEditorPane originalContentPane;
    public static JEditorPane processedContentPane;
    public static JEditorPane statisticInfoPane;


    private static JScrollPane originalScroll;
    private static JScrollPane processedScroll;
    private static JScrollPane historyScroll;
    private static JScrollPane statisticScroll;


    public static JTabbedPane contentTabs;


    private static JPanel historyPanel;

    public static JPanel create() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));

        // Body part
        JPanel bodyPart = new JPanel();
        bodyPart.setLayout(new BoxLayout(bodyPart, BoxLayout.Y_AXIS));
        mainPanel.add(bodyPart, BorderLayout.CENTER);

        // Address bar
        AddressBar addressBar = new AddressBar(
                "Enter URL / Local Path, or Press the File Button",
                "GET",
                JFileChooser.FILES_ONLY,
                "Text Files (*.txt)",
                "txt",
                path -> {

                    ContentService.processContentRequest(path);
                }
        );
        addressBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        addressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        addressBar.setMinimumSize(new Dimension(Integer.MAX_VALUE, 100));
        bodyPart.add(addressBar);

        // Content tabs
        contentTabs = new JTabbedPane();


        originalContentPane = new JTextPane();
        originalContentPane.setContentType("text/html");
        originalContentPane.setEditable(false);
        originalContentPane.setOpaque(false);


        applyOriginalContentStyles();

        originalScroll = createContentScrollPane(originalContentPane);
        contentTabs.addTab("Original", originalScroll);

        processedContentPane = new JTextPane();
        processedContentPane.setContentType("text/html");
        processedContentPane.setEditable(false);
        processedContentPane.setOpaque(false);


        applyProcessedContentStyles();


        processedScroll = createContentScrollPane(processedContentPane);
        contentTabs.addTab("Processed", processedScroll);


        statisticInfoPane = new JTextPane();
        statisticInfoPane.setContentType("text/html");
        statisticInfoPane.setEditable(false);
        statisticInfoPane.setOpaque(false);


        applyStatisticInfoStyles();


        statisticScroll = createContentScrollPane(statisticInfoPane);
        contentTabs.addTab("Statistic Info", statisticScroll);

        bodyPart.add(contentTabs);

        // Sidebar
        JPanel asidePart = new JPanel();
        asidePart.setLayout(new BoxLayout(asidePart, BoxLayout.Y_AXIS));
        asidePart.setPreferredSize(new Dimension(350, Integer.MAX_VALUE));
        asidePart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 0, 0, 0)));
        mainPanel.add(asidePart, BorderLayout.EAST);

        // Search area
        JPanel searchArea = new JPanel();
        searchArea.setLayout(new BoxLayout(searchArea, BoxLayout.Y_AXIS));
        searchArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));


        SearchConditionPanel conditionPanel = new SearchConditionPanel();
        conditionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));


        SearchButtonGroup buttonGroup = new SearchButtonGroup(conditionPanel);
        JPanel buttonGroupPanel = buttonGroup.createPanel();
        buttonGroupPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        buttonGroupPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        searchArea.add(conditionPanel);
        searchArea.add(Box.createVerticalStrut(10));
        searchArea.add(buttonGroupPanel);
        asidePart.add(searchArea);

        // History area
        JPanel historyArea = new JPanel(new BorderLayout());
        JLabel historyLabel = new JLabel("Search History");
        historyLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        historyArea.add(historyLabel, BorderLayout.NORTH);

        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(new Color(0x2c3e50));

        historyScroll = new JScrollPane(historyPanel);
        historyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension historySize = new Dimension(Integer.MAX_VALUE, 600);
        historyScroll.setPreferredSize(historySize);
        historyScroll.setMaximumSize(historySize);
        historyScroll.getVerticalScrollBar().setUnitIncrement(16);

        applyHistoryStyles();
        historyArea.add(historyScroll, BorderLayout.CENTER);

        // Save Area
        JPanel saveArea = new JPanel();
        JButton saveButton = new JButton("Save Search History");
        saveButton.setBackground(new Color(66, 135, 245));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> SaveHistory.saveHistoryToFile());
        saveArea.add(saveButton);
        historyArea.add(saveArea, BorderLayout.SOUTH);

        asidePart.add(historyArea);

        resetAllScrollPositions();


//        loadHistoryItems();

        return mainPanel;
    }


    public static void switchToTab(String tabTitle) {
        if (contentTabs == null) return;

        for (int i = 0; i < contentTabs.getTabCount(); i++) {
            if (tabTitle.equals(contentTabs.getTitleAt(i))) {
                contentTabs.setSelectedIndex(i);
                break;
            }
        }
    }


    private static void applyOriginalContentStyles() {

        Map<String, Map<String, String>> cssMap = getComponentCss("contentOriginalPanel");


        StringBuilder cssBuilder = new StringBuilder();
        if (cssMap != null) {
            for (Map.Entry<String, Map<String, String>> entry : cssMap.entrySet()) {
                cssBuilder.append(entry.getKey()).append(" { ");
                for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                    cssBuilder.append(prop.getKey()).append(":").append(prop.getValue()).append("; ");
                }
                cssBuilder.append("} ");
            }
        }


        String html = "<html><head><style>" + cssBuilder.toString() + "</style></head><body></body></html>";
        originalContentPane.setText(html);
    }


    public static void updateOriginalContent(String content) {
        if (originalContentPane != null) {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><style>");

            Map<String, Map<String, String>> cssMap = getComponentCss("contentOriginalPanel");
            if (cssMap != null) {
                for (Map.Entry<String, Map<String, String>> entry : cssMap.entrySet()) {
                    htmlBuilder.append(entry.getKey()).append(" { ");
                    for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                        htmlBuilder.append(prop.getKey()).append(":")
                                .append(prop.getValue()).append("; ");
                    }
                    htmlBuilder.append("} ");
                }
            }

            htmlBuilder.append("</style></head><body>");
            htmlBuilder.append(content);
            htmlBuilder.append("</body></html>");

            originalContentPane.setText(htmlBuilder.toString());
            resetScrollPosition(originalScroll);
        }
    }


    private static void applyProcessedContentStyles() {
        Map<String, Map<String, String>> cssMap = getComponentCss("contentProcessedPanel");
        StringBuilder cssBuilder = new StringBuilder();

        if (cssMap != null) {
            for (Map.Entry<String, Map<String, String>> entry : cssMap.entrySet()) {
                cssBuilder.append(entry.getKey()).append(" { ");
                for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                    cssBuilder.append(prop.getKey()).append(":").append(prop.getValue()).append("; ");
                }
                cssBuilder.append("} ");
            }
        }

        String html = "<html><head><style>" + cssBuilder.toString() + "</style></head><body></body></html>";
        processedContentPane.setText(html);
    }


    public static void updateProcessedContent(String content) {
        if (processedContentPane != null) {

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><style>");


            Map<String, Map<String, String>> cssMap = getComponentCss("contentProcessedPanel");
            if (cssMap != null) {
                for (Map.Entry<String, Map<String, String>> entry : cssMap.entrySet()) {
                    htmlBuilder.append(entry.getKey()).append(" { ");
                    for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                        htmlBuilder.append(prop.getKey()).append(":")
                                .append(prop.getValue()).append("; ");
                    }
                    htmlBuilder.append("} ");
                }
            }

            htmlBuilder.append("</style></head><body>");
            htmlBuilder.append(content);
            htmlBuilder.append("</body></html>");

            processedContentPane.setText(htmlBuilder.toString());
            resetScrollPosition(processedScroll);


            SwingUtilities.invokeLater(() -> {

                processedScroll.getViewport().setViewPosition(new Point(0, 0));
                processedScroll.getVerticalScrollBar().setValue(0);
            });
        }
    }


    private static void applyStatisticInfoStyles() {
        Map<String, Map<String, String>> cssMap = getComponentCss("statisticInfo");
        StringBuilder cssBuilder = new StringBuilder();

        if (cssMap != null) {
            for (Map.Entry<String, Map<String, String>> entry : cssMap.entrySet()) {
                cssBuilder.append(entry.getKey()).append(" { ");
                for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                    cssBuilder.append(prop.getKey())
                            .append(":")
                            .append(prop.getValue())
                            .append("; ");
                }
                cssBuilder.append("} ");
            }
        }

        String html = "<html><head><style>" + cssBuilder.toString() + "</style></head><body></body></html>";
        statisticInfoPane.setText(html);
    }


    public static void updateStatisticInfo(LinguisticAnalysis.TextStatistics stats) {
        if (statisticInfoPane == null || stats == null) return;

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><style>");

        Map<String, Map<String, String>> cssMap = getComponentCss("statisticInfo");
        if (cssMap != null) {
            for (Map.Entry<String, Map<String, String>> entry : cssMap.entrySet()) {
                htmlBuilder.append(entry.getKey()).append(" { ");
                for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                    htmlBuilder.append(prop.getKey()).append(":")
                            .append(prop.getValue()).append("; ");
                }
                htmlBuilder.append("} ");
            }
        }

        htmlBuilder.append("</style></head><body>");
        htmlBuilder.append("<div class='statistic-container'>");

        htmlBuilder.append("<div class='statistic-card'>");
        htmlBuilder.append("<div class='statistic-title'>Text Statistics</div>");

        htmlBuilder.append("<div class='statistic-item'>")
                .append("<span class='stat-label'>Total Words:</span>")
                .append("<span class='stat-value'>").append(stats.totalWords).append("</span>")
                .append("</div>");

        htmlBuilder.append("<div class='statistic-item'>")
                .append("<span class='stat-label'>Unique Words:</span>")
                .append("<span class='stat-value'>").append(stats.uniqueWords).append("</span>")
                .append("</div>");

        htmlBuilder.append("<div class='statistic-item'>")
                .append("<span class='stat-label'>Avg. Word Length:</span>")
                .append("<span class='stat-value'>").append(String.format("%.1f", stats.avgWordLength)).append("</span>")
                .append("</div>");

        htmlBuilder.append("<div class='statistic-item'>")
                .append("<span class='stat-label'>Sentences:</span>")
                .append("<span class='stat-value'>").append(stats.sentenceCount).append("</span>")
                .append("</div>");

        htmlBuilder.append("</div>");

        htmlBuilder.append("<div class='statistic-card'>");
        htmlBuilder.append("<div class='statistic-title'>Top 5 Frequent Words</div>");

        if (stats.topFrequentWords != null && !stats.topFrequentWords.isEmpty()) {
            for (Map.Entry<String, Integer> entry : stats.topFrequentWords) {
                htmlBuilder.append("<div class='statistic-item'>")
                        .append("<span class='stat-label'>").append(entry.getKey()).append("</span>")
                        .append("<span class='stat-value'>").append(entry.getValue()).append("</span>")
                        .append("</div>");
            }
        } else {
            htmlBuilder.append("<div class='statistic-item'>No frequent words found</div>");
        }

        htmlBuilder.append("<div class='statistic-card'>");
        htmlBuilder.append("<div class='statistic-title'>POS Distribution</div>");

        if (stats.sortedPosDistribution != null && !stats.sortedPosDistribution.isEmpty()) {
            for (Map.Entry<String, Double> entry : stats.sortedPosDistribution) {
                double percentage = entry.getValue();

                htmlBuilder.append("<div class='statistic-item'>")
                        .append("<span class='stat-label'>").append(entry.getKey()).append(":</span>")
                        .append("<span class='stat-value'>").append(String.format("%.1f", percentage)).append("%</span>")
                        .append("<div class='stat-bar'>")
                        .append("<div class='bar-fill' style='width: ")
                        .append(percentage).append("%'></div>")
                        .append("</div>")
                        .append("</div>");
            }
        } else {
            htmlBuilder.append("<div class='statistic-item'>No POS distribution available</div>");
        }

        htmlBuilder.append("</div>");
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body></html>");

        statisticInfoPane.setText(htmlBuilder.toString());

        SwingUtilities.invokeLater(() -> {
            resetScrollPosition(statisticScroll);
        });
    }

    private static void applyHistoryStyles() {
        historyPanel.setBackground(new Color(0x2c3e50));
        historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    }


    // Method to add a history item to the panel
    public static void addHistoryItem(String source, SearchCondition condition, int matchCount) {
        // Add the history item to SaveHistory (will be automatically added to the beginning of the list)
        SaveHistory.addHistoryItem(source, condition, matchCount);

        // Get the newly added item (the first item in the list)
        SaveHistory.HistoryItem item = SaveHistory.getHistoryItems().get(0);

        // Create a UI panel item
        JPanel itemPanel = createHistoryItemPanel(item);

        // Add the new item to the top of the panel (position 0)
        historyPanel.add(itemPanel, 0);

        // Re-validate and repaint
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private static JPanel createHistoryItemPanel(SaveHistory.HistoryItem item) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(new Color(0x34495e));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x3e4a59)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel timeLabel = new JLabel(item.getTime());
        timeLabel.setForeground(new Color(0x61afef));
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 10f));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel docLabel = new JLabel(item.getDocumentName());
        docLabel.setForeground(Color.WHITE);
        docLabel.setFont(docLabel.getFont().deriveFont(Font.PLAIN, 12f));
        docLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel conditionLabel = new JLabel(item.getFormattedCondition());
        conditionLabel.setForeground(new Color(0xe5c07b));
        conditionLabel.setFont(conditionLabel.getFont().deriveFont(Font.PLAIN, 11f));
        conditionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel countLabel = new JLabel("Matches: " + item.getMatchCount());
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 11f));
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        itemPanel.add(timeLabel);
        itemPanel.add(Box.createVerticalStrut(2));
        itemPanel.add(docLabel);
        itemPanel.add(Box.createVerticalStrut(2));
        itemPanel.add(conditionLabel);
        itemPanel.add(Box.createVerticalStrut(2));
        itemPanel.add(countLabel);

        return itemPanel;
    }

    private static class HistoryItem {
        String time;
        String documentName;
        String searchCondition;
        File file;
    }

    private static JScrollPane createContentScrollPane(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension size = new Dimension(Integer.MAX_VALUE, 600);
        scroll.setPreferredSize(size);
        scroll.setMaximumSize(size);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }


    private static JScrollPane createErrorScrollPane(String message) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(message));
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension size = new Dimension(Integer.MAX_VALUE, 600);
        scroll.setPreferredSize(size);
        scroll.setMaximumSize(size);
        return scroll;
    }

    public static void resetAllScrollPositions() {
        SwingUtilities.invokeLater(() -> {
            resetScrollPosition(originalScroll);
            resetScrollPosition(processedScroll);
            resetScrollPosition(historyScroll);
        });
    }

    public static void resetScrollPosition(JScrollPane scrollPane) {
        if (scrollPane != null) {
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
            scrollPane.getVerticalScrollBar().setValue(0);
        }
    }



    public static String getProcessedContent() {
        if (processedContentPane != null) {
            return processedContentPane.getText();
        }
        return "";
    }
}