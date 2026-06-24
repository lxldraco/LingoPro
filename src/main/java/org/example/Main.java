package org.example;

import javax.swing.*;

import org.example.io.SaveHistory;
import org.example.ui.MainFrame;
import org.example.service.AnalysisService;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Clear the history
                SaveHistory.clearHistory();

                // Set dark theme
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());

                // Initialize NLP analyzer
                AnalysisService.initialize();

                // Show main UI
                new MainFrame().setVisible(true);
            } catch (Exception ex) {
                System.err.println("Initialization failed: " + ex.getMessage());
            }
        });
    }
}