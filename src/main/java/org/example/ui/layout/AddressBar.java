package org.example.ui.layout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

public class AddressBar extends JPanel {
    private JTextField field;
    private JButton mainButton;
    private String hintText;
    private String buttonText;
    private int fileSelectionMode;
    private String fileFilterDescription;
    private String fileFilterExtension;
    private Consumer<String> actionHandler;
    private static final String IMAGE_PATH = "assets/file_icon.png";

    public AddressBar(String hintText, String buttonText,
                      int fileSelectionMode, String fileFilterDescription,
                      String fileFilterExtension, Consumer<String> actionHandler) {
        super(new BorderLayout(10, 0));
        this.hintText = hintText;
        this.buttonText = buttonText;
        this.fileSelectionMode = fileSelectionMode;
        this.fileFilterDescription = fileFilterDescription;
        this.fileFilterExtension = fileFilterExtension;
        this.actionHandler = actionHandler;

        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        initComponents();
    }

    private void initComponents() {
        // Create an input box
        field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", hintText);
        field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton browseButton = createImageButton(IMAGE_PATH, "Select local file");
        browseButton.addActionListener(this::handleBrowseAction);

        // Create the main function button
        mainButton = new JButton(buttonText);
        mainButton.setBackground(new Color(66, 135, 245));
        mainButton.setForeground(Color.WHITE);
        mainButton.addActionListener(this::handleMainButtonAction);

        // Layout
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.add(field, BorderLayout.CENTER);
        fieldPanel.add(browseButton, BorderLayout.EAST);

        add(fieldPanel, BorderLayout.CENTER);
        add(mainButton, BorderLayout.EAST);
    }

    private void handleBrowseAction(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(fileSelectionMode);

        // Set the file filter
        if (fileFilterDescription != null && fileFilterExtension != null) {
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    fileFilterDescription, fileFilterExtension
            ));
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            field.setText(selectedFile.getAbsolutePath());
        }
    }

    private void handleMainButtonAction(ActionEvent e) {
        String path = field.getText().trim();
        if (!path.isEmpty() && actionHandler != null) {
            actionHandler.accept(path);
        }
    }

    public void setPath(String path) {
        field.setText(path);
    }

    public String getPath() {
        return field.getText().trim();
    }

    /**
     * Create an image button
     *
     * @param imagePath The path to the image file
     * @param tooltip   The tooltip text
     * @return A configured image button
     */
    private JButton createImageButton(String imagePath, String tooltip) {
        // 使用类加载器获取资源
        ClassLoader classLoader = getClass().getClassLoader();
        URL imageUrl = classLoader.getResource(imagePath);

        if (imageUrl == null) {
            System.err.println("Image not found: " + imagePath);
            System.err.println("Current classpath: " + System.getProperty("java.class.path"));
            return new JButton("File");
        }

        try (InputStream inputStream = imageUrl.openStream()) {
            // 创建按钮并设置图片
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(inputStream));
            Image scaledImage = originalIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            JButton button = new JButton(scaledIcon);
            button.setToolTipText(tooltip);

            // 设置按钮样式
            button.setContentAreaFilled(false);
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusPainted(false);

            return button;
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return new JButton("File");
        }
    }
}