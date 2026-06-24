package org.example.ui.layout;

import org.example.process.SearchCondition;

import javax.swing.*;
import java.awt.*;

public class SearchConditionPanel extends JPanel {
    JTextField exactWordInputField = new JTextField();
    JTextField wordLemmaInputField = new JTextField();
    JComboBox<String> posTagComboBox;
    JTextField regexInputField = new JTextField();
    JCheckBox caseSensitiveCheckBox = new JCheckBox();
    JSpinner leftSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
    JSpinner rightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));

    public SearchConditionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // Initialize POS tag dropdown
        String[] posOptions = {"", "ADJ", "ADP", "ADV", "AUX", "CCONJ", "DET", "INTJ", "NOUN", "NUM", "PART", "PRON", "PROPN", "PUNCT", "SCONJ", "SYM", "VERB", "X"};
        posTagComboBox = new JComboBox<>(posOptions);

        // Add components to panel
        add(createInputRow("Exact Word:", exactWordInputField));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(createInputRow("Word's Lemma:", wordLemmaInputField));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(createInputRow("POS Tag:", posTagComboBox));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(createInputRow("Regex:", regexInputField));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(createInputRow("Left Neighbor:", leftSpinner));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(createInputRow("Right Neighbor:", rightSpinner));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(createInputRow("Case Sensitive:", caseSensitiveCheckBox));

        caseSensitiveCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 186));
    }

    // Create labeled input row
    private JPanel createInputRow(String label, JComponent component) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        row.add(new JLabel(label), BorderLayout.WEST);

        if (component != null) {
            if (component instanceof JTextField) {
                ((JTextField) component).setPreferredSize(new Dimension(200, 25));
            } else if (component instanceof JComboBox) {
                ((JComboBox<?>) component).setPreferredSize(new Dimension(200, 25));
            } else if (component instanceof JSpinner) {
                ((JSpinner) component).setPreferredSize(new Dimension(200, 25));
            } else if (component instanceof JCheckBox) {
                // Checkbox doesn't need size adjustment
            }

            row.add(component, BorderLayout.EAST);
        }

        return row;
    }

    public SearchCondition getSearchCondition() {
        SearchCondition condition = new SearchCondition();

        // Set values based on input validity
        if (!exactWordInputField.getText().trim().isEmpty()) {
            condition.setExactWord(exactWordInputField.getText().trim());
        }
        if (!wordLemmaInputField.getText().trim().isEmpty()) {
            condition.setWordLemma(wordLemmaInputField.getText().trim());
        }
        if (posTagComboBox.getSelectedItem() != null &&
                !((String)posTagComboBox.getSelectedItem()).trim().isEmpty()) {
            condition.setPosTag((String) posTagComboBox.getSelectedItem());
        }
        if (!regexInputField.getText().trim().isEmpty()) {
            condition.setRegex(regexInputField.getText().trim());
        }

        // Neighbor settings
        int left = (int) leftSpinner.getValue();
        int right = (int) rightSpinner.getValue();
        condition.setNeighbored(left > 0 || right > 0);
        condition.setLeftNeighbor(left);
        condition.setRightNeighbor(right);

        // Preserve case sensitive setting
        condition.setCaseSensitive(caseSensitiveCheckBox.isSelected());

        return condition;
    }
}