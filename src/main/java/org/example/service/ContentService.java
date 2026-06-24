package org.example.service;

import org.example.io.ContentExtractor;
import org.example.process.LinguisticAnalysis;
import org.example.ui.MainFrame;
import org.example.ui.layout.MainPanel;

import java.io.File;

public class ContentService {
    private static String lastExtractedContent;
    private static String lastSource;

    public static String getLastExtractedContent() {
        return lastExtractedContent;
    }

    public static String getLastSource() {
        return lastSource;
    }

    public static void processContentRequest(String source) {
        try {
            MainFrame.updateStatus("⌛⌛ Extracting content...");

            // Check if directory
            File file = new File(source);
            if (file.isDirectory()) {
                MainFrame.updateStatus("❌❌ Directory selected, please choose a file");
                return;
            }

            // 1. Extract content
            String content = ContentExtractor.fetchContent(source);
            int length = content.length();
            lastExtractedContent = content; // Cache content
            lastSource = source;

            // 2. Update original content panel - add sentence numbers
            String originalWithNumbers = addSentenceNumbersToOriginal(content);
            MainPanel.updateOriginalContent(originalWithNumbers);

            MainFrame.updateStatus("✅ Content extracted (" + length + " chars)");

            // 3. Output content excerpt to console
            System.out.println("\n===== CONTENT EXCERPT (first 200 chars) =====");
            System.out.println(content.substring(0, Math.min(200, length)) + (length > 200 ? "..." : ""));
            System.out.println("===========================================");

            // 4. Perform basic analysis
            AnalysisService.performBasicAnalysis(content);

            // 5. Get and update statistics
            LinguisticAnalysis.TextStatistics stats = AnalysisService.getTextStatistics(content);
            MainPanel.updateStatisticInfo(stats);

            // 6. Initialize processed content panel
            MainPanel.updateProcessedContent(""); // Clear processed content

            // 7. Reset all scroll positions
            MainPanel.resetAllScrollPositions();

            // 8. Switch to Original tab
            MainPanel.switchToTab("Original");

        } catch (Exception e) {
            MainFrame.updateStatus("❌❌ Error: " + e.getMessage());
            System.err.println("Content processing error: " + e.getMessage());
        }
    }

    // Add sentence numbers to original content
    private static String addSentenceNumbersToOriginal(String content) {
        LinguisticAnalysis analyzer = AnalysisService.getAnalyzer();
        if (analyzer == null) return escapeHtml(content);

        StringBuilder result = new StringBuilder();
        result.append("<div class='original-content'>");

        try {
            // Use new getSentences method
            String[] sentences = analyzer.getSentences(content);
            for (int i = 0; i < sentences.length; i++) {
                result.append("<div>")
                        .append("<span class='sentence-number'>")
                        .append(i + 1)
                        .append(".</span> ")
                        .append(escapeHtml(sentences[i]))
                        .append("</div>");
            }
        } catch (Exception e) {
            // Fallback if exception occurs
            result.append(escapeHtml(content));
        }

        result.append("</div>");
        return result.toString();
    }

    // HTML escape method
    private static String escapeHtml(String content) {
        return content.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }
}