package org.example.ui;

import com.formdev.flatlaf.FlatClientProperties;
import org.example.ui.layout.AboutPanel;
import org.example.ui.layout.HelpPanel;
import org.example.ui.layout.MainPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private static JLabel statusLabel;
    // 添加标签页引用
    public static JTabbedPane menuTabs;

    public MainFrame() {
        configureFrame();
        initComponents();
    }

    private void configureFrame() {
        setTitle("LingoPro - Group 14");
        setSize(1400, 700);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // 设置全局标签页字体
        Font tabFont = new Font("Segoe UI", Font.BOLD, 14);
        UIManager.put("TabbedPane.font", tabFont);

        // 创建标签页面板
        menuTabs = new JTabbedPane();

        // 设置FlatLaf标签页样式
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS,
                new Insets(10, 10, 10, 10));
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 30);
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_MINIMUM_TAB_WIDTH, 100);
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_MAXIMUM_TAB_WIDTH, 100);
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, "center");
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE,
                FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        menuTabs.putClientProperty(FlatClientProperties.TABBED_PANE_HAS_FULL_BORDER, true);

        // 1. LinguoPro标签页 - 直接创建MainPanel实例
        JComponent linguoProPanel = MainPanel.create();
        menuTabs.addTab("LinguoPro", linguoProPanel);


        // 2. Help标签页
        JComponent helpPanel = new HelpPanel();
        menuTabs.addTab("Help", helpPanel);

        // 3. About标签页 - 替换原来的Donation
        JComponent aboutPanel = new AboutPanel();
        menuTabs.addTab("About", aboutPanel);

        add(menuTabs, BorderLayout.CENTER);

        // 添加状态栏
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    public static void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}