package org.example.ui.layout;

import org.example.ui.ComponentFactory;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {

    public HelpPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JEditorPane helpContentPane = createHelpContentPane();
        JScrollPane scrollPane = createHelpScrollPane(helpContentPane);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JEditorPane createHelpContentPane() {
        JEditorPane pane = new JTextPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setOpaque(false);

        JComponent helpComponent = ComponentFactory.createComponent("helpContent");
        if (helpComponent instanceof JEditorPane) {
            pane.setText(((JEditorPane) helpComponent).getText());
        }

        return pane;
    }

    private JScrollPane createHelpScrollPane(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        SwingUtilities.invokeLater(() -> {
            scroll.getViewport().setViewPosition(new Point(0, 0));
            scroll.getVerticalScrollBar().setValue(0);
        });

        return scroll;
    }
}