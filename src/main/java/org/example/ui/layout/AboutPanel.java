package org.example.ui.layout;

import org.example.ui.ComponentFactory;

import javax.swing.*;
import java.awt.*;

public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));


        JEditorPane contentPane = createContentPane();
        JScrollPane scrollPane = createScrollPane(contentPane);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JEditorPane createContentPane() {
        JEditorPane pane = new JTextPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setOpaque(false);


        JComponent content = ComponentFactory.createComponent("aboutContent");
        if (content instanceof JEditorPane) {
            pane.setText(((JEditorPane) content).getText());
        }

        return pane;
    }

    private JScrollPane createScrollPane(JComponent component) {
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