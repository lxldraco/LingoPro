package org.example.ui;

import java.util.Map;

public class ComponentsConfig {
    private Map<String, ComponentDefinition> components;

    public Map<String, ComponentDefinition> getComponents() {
        return components;
    }

    public static class ComponentDefinition {
        private String html;
        private Map<String, Map<String, String>> css;

        public String getHtml() {
            return html;
        }

        public Map<String, Map<String, String>> getCss() {
            return css;
        }
    }
}