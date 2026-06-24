package org.example.service;

import org.example.process.SearchCondition;
import org.example.process.LinguisticAnalysis;
import org.example.ui.MainFrame;
import org.example.ui.layout.MainPanel;

import java.util.List;
import java.util.Map;

public class AnalysisService {
    private static LinguisticAnalysis analyzer;

    // Initialize NLP analyzer
    public static void initialize() {
        try {
            analyzer = new LinguisticAnalysis();
            System.out.println("NLP analyzer initialized successfully");
            MainFrame.updateStatus("✅ NLP models loaded successfully");
        } catch (Exception e) {
            System.err.println("NLP initialization failed: " + e.getMessage());
            e.printStackTrace();
            analyzer = null;
            MainFrame.updateStatus("❌❌ Failed to load NLP models: " + e.getMessage());
        }
    }

    // Perform basic analysis - reverted to single parameter version
    public static void performBasicAnalysis(String content) {
        if (analyzer == null) {
            System.err.println("NLP analyzer not initialized");
            return;
        }

        try {
            System.out.println("\n===== BASIC ANALYSIS STARTED =====");

            // 1. Sentence detection
            analyzer.getSentDetected(content.substring(0, Math.min(500, content.length())));

            // 2. Word frequency statistics
            Map<String, Integer> wordFreq = analyzer.getWordFrequency(content);
            System.out.println("Top 10 frequent words:");
            wordFreq.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

            System.out.println("===== BASIC ANALYSIS COMPLETED =====");
        } catch (Exception e) {
            System.err.println("Basic analysis failed: " + e.getMessage());
        }
    }

    // Execute search
    public static int performSearch(String content, SearchCondition condition) {
        int matchCount = 0;
        if (analyzer == null) {
            System.err.println("NLP analyzer not initialized");
            MainFrame.updateStatus("❌❌ NLP analyzer not initialized");
            return matchCount;
        }

        System.out.println("\n===== SEARCH STARTED =====");
        System.out.println("Search Condition: " + condition);

        try {
            // 1. Exact word search
            if (condition.getExactWord() != null && !condition.getExactWord().isEmpty()) {
                System.out.println("\n--- Exact Word Search ---");
                List<String> exactWordResults = analyzer.searchKeywordInContext(
                        content,
                        condition.getExactWord(),
                        condition.isCaseSensitive(),
                        false
                );
                System.out.println("Found " + exactWordResults.size() + " matches");
            }

            // 2. Lemma search
            if (condition.getWordLemma() != null && !condition.getWordLemma().isEmpty()) {
                System.out.println("\n--- Lemma Search ---");
                List<String> lemmaResults = analyzer.searchKeywordInContext(
                        content,
                        condition.getWordLemma(),
                        condition.isCaseSensitive(),
                        false
                );
                System.out.println("Found " + lemmaResults.size() + " matches");
            }

            // 3. Regex search
            if (condition.getRegex() != null && !condition.getRegex().isEmpty()) {
                System.out.println("\n--- Regex Search ---");
                List<String> regexResults = analyzer.searchKeywordInContext(
                        content,
                        condition.getRegex(),
                        condition.isCaseSensitive(),
                        true
                );
                System.out.println("Found " + regexResults.size() + " matches");
            }

            // 4. POS tag search
            if (condition.getPosTag() != null && !condition.getPosTag().trim().isEmpty()) {
                System.out.println("\n--- POS Tag Search ---");
                List<String> posResults = analyzer.searchByPosTag(
                        content,
                        condition.getPosTag().trim()
                );
                System.out.println("Found " + posResults.size() + " matches");
            }

            // 5. Neighbor word search
            if (condition.isNeighbored() &&
                    (condition.getExactWord() != null && !condition.getExactWord().isEmpty())) {

                System.out.println("\n--- Neighbor Search ---");
                analyzer.findNeighbors(
                        content,
                        condition.getExactWord(),
                        condition.getLeftNeighbor(),
                        condition.getRightNeighbor()
                );
            }

            // Generate processed HTML
            String processedHtml = analyzer.generateProcessedHtml(content, condition);
            MainPanel.updateProcessedContent(processedHtml);

            // Calculate the number of matches
            if (processedHtml.contains("No matching results")) {
                matchCount = 0;
            } else {
                // Calculate the number of highlight markers
                matchCount = processedHtml.split("highlight", -1).length - 1;
                matchCount += processedHtml.split("left-highlight", -1).length - 1;
                matchCount += processedHtml.split("right-highlight", -1).length - 1;
            }

            // Ensure switch to Processed tab
            MainPanel.switchToTab("Processed");

            // ... [original scroll position reset code]
        } catch (Exception e) {
            MainFrame.updateStatus("❌❌ Search failed: " + e.getMessage());
            System.err.println("Search failed: " + e.getMessage());
        }
        return matchCount;
    }

    // Add analyzer getter
    public static LinguisticAnalysis getAnalyzer() {
        return analyzer;
    }

    // Add text statistics getter
    public static LinguisticAnalysis.TextStatistics getTextStatistics(String content) {
        if (analyzer == null) {
            System.err.println("NLP analyzer not initialized");
            return null;
        }

        try {
            return analyzer.getTextStatistics(content);
        } catch (Exception e) {
            System.err.println("Failed to get text statistics: " + e.getMessage());
            return null;
        }
    }
}