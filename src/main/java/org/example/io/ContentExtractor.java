package org.example.io;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileNotFoundException;

public class ContentExtractor {

    // Extract content fetching logic as independent method
    public static String fetchContent(String input) throws Exception {
        Document doc;

        if (input.startsWith("https://en.wikipedia.org/wiki/")) {
            doc = Jsoup.connect(input).get();
        } else {
            File file = new File(input);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + input);
            }

            // Check file extension
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                throw new IllegalArgumentException("Only .txt files are supported");
            }

            // Read text content directly
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }

        Element content = doc.selectFirst("#mw-content-text .mw-parser-output");
        if (content == null) {
            content = doc.body();
        }

        return content.text();
    }
}