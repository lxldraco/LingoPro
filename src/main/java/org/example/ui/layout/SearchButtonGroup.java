package org.example.ui.layout;

import org.example.process.SearchCondition;
import org.example.service.AnalysisService;
import org.example.service.ContentService;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SearchButtonGroup {
    private final SearchConditionPanel searchConditionPanel;

    public SearchButtonGroup(SearchConditionPanel searchConditionPanel) {
        this.searchConditionPanel = searchConditionPanel;
    }

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 10, 0, 10);

        JButton searchBtn = createPrimaryButton("Search", this::handleSearchAction);
        JButton clearBtn = createButton("Clear", this::clearSearchAction);

        gbc.gridx = 0;
        gbc.weightx = 0.75;
        panel.add(searchBtn, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.25;
        panel.add(clearBtn, gbc);

        return panel;
    }

    private JButton createPrimaryButton(String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(66, 135, 245));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 30));
        btn.addActionListener(listener);
        return btn;
    }

    private JButton createButton(String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(0, 30));
        btn.addActionListener(listener);
        return btn;
    }

    private void handleSearchAction(ActionEvent e) {
        if (searchConditionPanel == null) {
            System.out.println("❌ Search condition panel not initialized");
            return;
        }

        SearchCondition condition = searchConditionPanel.getSearchCondition();
        System.out.println("Search condition: " + condition);

        String content = ContentService.getLastExtractedContent();
        if (content == null || content.isEmpty()) {
            System.out.println("❌ No content available for search");
            return;
        }

        AnalysisService.performSearch(content, condition);


        int matchCount = AnalysisService.performSearch(content, condition);


        String source = ContentService.getLastSource();
        MainPanel.addHistoryItem(source, condition, matchCount);
    }

    private void clearSearchAction(ActionEvent e) {
        if (searchConditionPanel == null) {
            System.out.println("❌ Search condition panel not initialized");
            return;
        }

        searchConditionPanel.exactWordInputField.setText("");
        searchConditionPanel.wordLemmaInputField.setText("");
        searchConditionPanel.posTagComboBox.setSelectedIndex(0);
        searchConditionPanel.regexInputField.setText("");
        searchConditionPanel.caseSensitiveCheckBox.setSelected(false);
        searchConditionPanel.leftSpinner.setValue(0);
        searchConditionPanel.rightSpinner.setValue(0);

        System.out.println("Search Condition cleared");
    }

}