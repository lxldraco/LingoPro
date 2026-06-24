package org.example.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

public class ComponentFactory {
    private static final String CONFIG_PATH = "/components/components.json";
    private static Map<String, ComponentsConfig.ComponentDefinition> componentDefs;

    static {
        try (InputStream inputStream = ComponentFactory.class.getResourceAsStream(CONFIG_PATH)) {
            if (inputStream == null) {
                throw new RuntimeException("Component config not found: " + CONFIG_PATH);
            }

            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Type configType = new TypeToken<ComponentsConfig>(){}.getType();
            ComponentsConfig config = new Gson().fromJson(reader, configType);

            componentDefs = config.getComponents();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load component library", e);
        }
    }

    public static JComponent createComponent(String componentId) {
        ComponentsConfig.ComponentDefinition def = componentDefs.get(componentId);
        if (def == null) {
            throw new IllegalArgumentException("Component not found: " + componentId);
        }

        return createHtmlPanel(def);
    }

    private static JEditorPane createHtmlPanel(ComponentsConfig.ComponentDefinition def) {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setText(applyCss(def.getHtml(), def.getCss()));
        pane.setEditable(false);
        pane.setOpaque(false);
        return pane;
    }

    private static String applyCss(String html, Map<String, Map<String, String>> cssMap) {
        if (cssMap == null || cssMap.isEmpty()) {
            return "<html><body>" + html + "</body></html>";
        }

        StringBuilder cssBuilder = new StringBuilder();
        for (Map.Entry<String, Map<String, String>> selectorEntry : cssMap.entrySet()) {
            cssBuilder.append(selectorEntry.getKey()).append(" { ");
            for (Map.Entry<String, String> propertyEntry : selectorEntry.getValue().entrySet()) {
                cssBuilder.append(propertyEntry.getKey())
                        .append(":")
                        .append(propertyEntry.getValue())
                        .append("; ");
            }
            cssBuilder.append("} ");
        }

        return "<html><head><style>" + cssBuilder + "</style></head><body>" + html + "</body></html>";
    }


    public static Map<String, Map<String, String>> getComponentCss(String componentId) {
        ComponentsConfig.ComponentDefinition def = componentDefs.get(componentId);
        return (def != null) ? def.getCss() : null;
    }
}