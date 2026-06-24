package org.example.process;

import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class LinguisticAnalysis {

    // Static variable to ensure models are loaded only once
    private static volatile boolean modelsLoaded = false;
    private static SentenceDetectorME staticSentenceDetector = null;
    private static TokenizerME staticTokenizer = null;
    private static POSTaggerME staticPosTagger = null;
    private static LemmatizerME staticLemmatizer = null;

    // Instance variables pointing to static models
    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private final LemmatizerME lemmatizer;

    // Stop words list
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "for", "nor", "as", "at",
            "by", "in", "of", "on", "to", "with", "about", "above", "across",
            "after", "against", "along", "among", "around", "before", "behind",
            "below", "beneath", "beside", "between", "beyond", "during", "except",
            "inside", "into", "near", "off", "out", "outside", "over", "past",
            "since", "through", "toward", "under", "underneath", "until", "upon",
            "within", "without", "i", "me", "my", "myself", "we", "our", "ours",
            "ourselves", "you", "your", "yours", "yourself", "yourselves", "he",
            "him", "his", "himself", "she", "her", "hers", "herself", "it", "its",
            "itself", "they", "them", "their", "theirs", "themselves", "what",
            "which", "who", "whom", "this", "that", "these", "those", "am", "is",
            "are", "was", "were", "be", "been", "being", "have", "has", "had",
            "having", "do", "does", "did", "doing", "would", "should", "could",
            "ought", "may", "might", "must", "shall", "will", "can", "could",
            "just", "should", "now", "d", "ll", "m", "o", "re", "ve", "y"
    ));

    /**
     * Constructor: Use pre-loaded models
     */
    public LinguisticAnalysis() {
        // Ensure models are loaded only once
        synchronized (LinguisticAnalysis.class) {
            if (!modelsLoaded) {
                try {
                    loadModels();
                    modelsLoaded = true;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load NLP models", e);
                }
            }
        }

        // Use pre-loaded static models
        this.sentenceDetector = staticSentenceDetector;
        this.tokenizer = staticTokenizer;
        this.posTagger = staticPosTagger;
        this.lemmatizer = staticLemmatizer;
    }

    /**
     * Static method to load models (executed only once)
     */
    private static void loadModels() throws Exception {
        System.out.println("Loading OpenNLP models...");

        String resourceBase = "/";
        URL resourceUrl = LinguisticAnalysis.class.getResource(resourceBase);
        System.out.println("Resource base path: " + (resourceUrl != null ? resourceUrl.getPath() : "null"));

        // 1. Sentence Detector Model
        staticSentenceDetector = loadModel(
                SentenceModel.class,
                model -> new SentenceDetectorME((SentenceModel) model),
                new String[]{"models/opennlp-en-ud-ewt-sentence-1.3-2.5.4.bin"},
                "Sentence Detector"
        );

        // 2. Tokenizer Model
        staticTokenizer = loadModel(
                TokenizerModel.class,
                model -> new TokenizerME((TokenizerModel) model),
                new String[]{"models/opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin"},
                "Tokenizer"
        );

        // 3. POS Tagger Model
        staticPosTagger = loadModel(
                POSModel.class,
                model -> new POSTaggerME((POSModel) model),
                new String[]{"models/opennlp-en-ud-ewt-pos-1.3-2.5.4.bin"},
                "POS Tagger"
        );

        // 4. Lemmatizer Model
        staticLemmatizer = loadModel(
                LemmatizerModel.class,
                model -> new LemmatizerME((LemmatizerModel) model),
                new String[]{"models/opennlp-en-ud-ewt-lemmas-1.3-2.5.4.bin"},
                "Lemmatizer"
        );

        System.out.println("All OpenNLP models loaded successfully.");
    }

    private static <T, M> M loadModel(Class<T> modelClass,
                                      ModelCreator<T, M> creator,
                                      String[] possiblePaths,
                                      String modelName) throws IOException {
        IOException lastError = null;
        ClassLoader classLoader = LinguisticAnalysis.class.getClassLoader();

        for (String path : possiblePaths) {
            System.out.println("Trying path: " + path);

            URL resourceUrl = classLoader.getResource(path);
            if (resourceUrl != null) {
                System.out.println("Found resource: " + resourceUrl);
            } else {
                System.out.println("Resource not found: " + path);
            }

            try (InputStream modelIn = classLoader.getResourceAsStream(path)) {
                if (modelIn != null) {
                    T model = modelClass.getConstructor(InputStream.class).newInstance(modelIn);
                    M instance = creator.create(model);
                    System.out.println("✅ " + modelName + " model loaded: " + path);
                    return instance;
                } else {
                    System.out.println("⚠️ " + modelName + " model not found: " + path);
                }
            } catch (Exception e) {
                lastError = new IOException("Error loading " + modelName + " from " + path, e);
                System.err.println("⚠️ Failed to load " + modelName + " model from " + path + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

//        System.err.println("Classpath entries:");
//        String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
//        for (String entry : classpathEntries) {
//            System.err.println(" - " + entry);
//        }

        String errorMsg = modelName + " model not found. Tried paths: " + Arrays.toString(possiblePaths);
        if (lastError != null) throw new IOException(errorMsg, lastError);
        else throw new IOException(errorMsg);
    }

    // Functional interface for model creation
    @FunctionalInterface
    private interface ModelCreator<T, M> {
        M create(T model) throws IOException;
    }

    // Get text statistics
    public TextStatistics getTextStatistics(String text) {
        TextStatistics stats = new TextStatistics();

        // 1. Sentence statistics
        String[] sentences = getSentences(text);
        stats.sentenceCount = sentences.length;

        // 2. Tokenization and POS tagging
        List<String> allWords = new ArrayList<>();
        List<String> posTags = new ArrayList<>();

        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            String[] tags = posTagger.tag(tokens);

            for (int i = 0; i < tokens.length; i++) {
                if (!isPunctuation(tokens[i])) {
                    allWords.add(tokens[i]);
                    posTags.add(tags[i]);
                }
            }
        }

        // 3. Basic statistics
        stats.totalWords = allWords.size();
        stats.avgWordLength = allWords.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);

        // 4. Unique words (excluding stop words)
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : allWords) {
            String lowerWord = word.toLowerCase();
            if (!STOP_WORDS.contains(lowerWord)) {
                wordFreq.put(lowerWord, wordFreq.getOrDefault(lowerWord, 0) + 1);
            }
        }
        stats.uniqueWords = wordFreq.size();

        // 5. Frequent words (excluding stop words)
        stats.topFrequentWords = wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        // 6. POS distribution - fixed percentage calculation
        stats.posDistribution = new HashMap<>();
        for (String tag : posTags) {
            stats.posDistribution.put(tag, stats.posDistribution.getOrDefault(tag, 0) + 1);
        }

        // 7. Recalculate percentages
        Map<String, Double> percentageDistribution = new HashMap<>();
        int totalPos = posTags.size();
        if (totalPos > 0) {
            for (String tag : stats.posDistribution.keySet()) {
                int count = stats.posDistribution.get(tag);
                double percentage = (count * 100.0) / totalPos;
                if (percentage >= 0.05) {
                    percentageDistribution.put(tag, percentage);
                }
            }
        }

        // 8. Sort POS distribution by frequency
        stats.sortedPosDistribution = percentageDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        return stats;
    }

    // Modified sentence detection method
    public void getSentDetected(String text) {
        System.out.println("\n--- Detecting Sentences ---");
        String[] sentences = getSentences(text);
        for (String sentence : sentences) {
            System.out.println(sentence);
        }
    }

    public Map<String, Integer> getWordFrequency(String text) {
        System.out.println("\n--- Calculating Word Frequency ---");
        String[] tokens = tokenizer.tokenize(text);
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String token : tokens) {
            String lowerCaseToken = token.toLowerCase();
            frequencyMap.put(lowerCaseToken, frequencyMap.getOrDefault(lowerCaseToken, 0) + 1);
        }
        return frequencyMap;
    }

    // Add punctuation detection method
    private boolean isPunctuation(String token) {
        return token.matches("^[\\p{Punct}“”‘’„\"'\u2018\u2019\u201C\u201D]+$");
    }

    public void findNeighbors(String text, String keyword, int leftWindow, int rightWindow) {
//        System.out.printf("\n--- Finding Neighbors for '%s' (Left: %d, Right: %d) ---\n", keyword, leftWindow, rightWindow);
        String[] sentences = sentenceDetector.sentDetect(text);

        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equalsIgnoreCase(keyword)) {
//                    System.out.println("Found in sentence: [" + sentence + "]");

                    // Collect left neighbors (skip punctuation)
                    List<String> leftNeighbors = new ArrayList<>();
                    int j = i - 1;
                    int leftCount = 0;
                    while (j >= 0 && leftCount < leftWindow) {
                        if (!isPunctuation(tokens[j])) {
                            leftNeighbors.add(0, tokens[j]);
                            leftCount++;
                        }
                        j--;
                    }

                    // Collect right neighbors (skip punctuation)
                    List<String> rightNeighbors = new ArrayList<>();
                    j = i + 1;
                    int rightCount = 0;
                    while (j < tokens.length && rightCount < rightWindow) {
                        if (!isPunctuation(tokens[j])) {
                            rightNeighbors.add(tokens[j]);
                            rightCount++;
                        }
                        j++;
                    }

//                    System.out.printf("  Context: [ %s ] <-- %s --> [ %s ]%n",
//                            String.join(" ", leftNeighbors),
//                            tokens[i],
//                            String.join(" ", rightNeighbors));
                }
            }
        }
    }

    // Add regex matching method
    private boolean isRegexMatch(String token, String regex) {
        if (regex == null || regex.isEmpty()) return false;
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(token).find();
        } catch (PatternSyntaxException e) {
            System.err.println("Invalid regex pattern: " + regex);
            return false;
        }
    }

    public List<String> searchKeywordInContext(String text, String keyword, boolean caseSensitive, boolean isRegex) {
//        System.out.printf("\n--- Searching for '%s' (Case Sensitive: %b, Regex: %b) ---\n", keyword, caseSensitive, isRegex);
        List<String> results = new ArrayList<>();
        String[] sentences = sentenceDetector.sentDetect(text);

        Pattern pattern;
        try {
            if (isRegex) {
                pattern = Pattern.compile(keyword, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            } else {
                String exactKeyword = "\\b" + Pattern.quote(keyword) + "\\b";
                pattern = Pattern.compile(exactKeyword, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException e) {
            System.err.println("Invalid regex pattern: " + keyword);
            return results;
        }

        for (String sentence : sentences) {
            if (pattern.matcher(sentence).find()) {
//                System.out.println("Found match in sentence: " + sentence);
                results.add(sentence);
            }
        }

//        if (results.isEmpty()) System.out.println("No matches found.");
        return results;
    }

    /**
     * Search by POS tag
     * @param text Input text
     * @param posTag POS tag to search for
     * @return List of matching words
     */
    public List<String> searchByPosTag(String text, String posTag) {
//        System.out.println("\n--- Searching for POS Tag: " + posTag + " ---");
        List<String> results = new ArrayList<>();
        String[] sentences = sentenceDetector.sentDetect(text);

        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            String[] tags = posTagger.tag(tokens);
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].equals(posTag)) {
                    results.add(tokens[i]);
//                    System.out.println("Found: " + tokens[i] + " (" + posTag + ")");
                }
            }
        }

//        if (results.isEmpty()) System.out.println("No matches found for POS tag: " + posTag);
        return results;
    }

    // Add text processing cache
    private static final Map<String, CachedTextAnalysis> textAnalysisCache = new LRUCache<>(50);
    private static final Map<String, CachedSentenceAnalysis> sentenceAnalysisCache = new LRUCache<>(500);

    // LRU cache implementation
    private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        public LRUCache(int maxSize) {
            super(maxSize * 4 / 3, 0.75f, true);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }

    // Text analysis cache structure
    private static class CachedTextAnalysis {
        final String[] sentences;
        final Map<String, String[]> tokensCache;
        final Map<String, String[]> tagsCache;

        public CachedTextAnalysis(String[] sentences) {
            this.sentences = sentences;
            this.tokensCache = new HashMap<>();
            this.tagsCache = new HashMap<>();
        }
    }

    // Sentence analysis cache structure
    private static class CachedSentenceAnalysis {
        final String[] tokens;
        final String[] tags;

        public CachedSentenceAnalysis(String[] tokens, String[] tags) {
            this.tokens = tokens;
            this.tags = tags;
        }
    }

    /**
     * Get sentence array (with cache)
     */
    public String[] getSentences(String text) {
        // Generate text hash as cache key
        String textHash = Integer.toHexString(text.hashCode());

        // Try to get from cache
        CachedTextAnalysis cached = textAnalysisCache.get(textHash);
        if (cached != null) {
            return cached.sentences;
        }

        // Cache miss, process and cache result
        String[] sentences = sentenceDetector.sentDetect(text);
        textAnalysisCache.put(textHash, new CachedTextAnalysis(sentences));
        return sentences;
    }

    /**
     * Process sentence (with cache)
     */
    private CachedSentenceAnalysis processSentence(String sentence) {
        // Generate sentence hash as cache key
        String sentenceHash = Integer.toHexString(sentence.hashCode());

        // Try to get from cache
        CachedSentenceAnalysis cached = sentenceAnalysisCache.get(sentenceHash);
        if (cached != null) {
            return cached;
        }

        // Cache miss, process and cache result
        String[] tokens = tokenizer.tokenize(sentence);
        String[] tags = posTagger.tag(tokens);
        CachedSentenceAnalysis result = new CachedSentenceAnalysis(tokens, tags);
        sentenceAnalysisCache.put(sentenceHash, result);
        return result;
    }

    public String generateProcessedHtml(String text, SearchCondition condition) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<div class='processed-content'>");

        // Use cached sentences
        String[] sentences = getSentences(text);
        boolean hasMatches = false;

        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i];
            // Use cached sentence processing result
            CachedSentenceAnalysis analysis = processSentence(sentence);
            String[] tokens = analysis.tokens;
            String[] tags = analysis.tags;

            htmlBuilder.append("<div class='sentence-container'>")
                    .append("<div class='sentence-number'>").append(i + 1).append(".</div>");

            List<TokenInfo> tokenInfos = processSentenceForTable(sentence, condition);
            if (tokenInfos.stream().anyMatch(t -> t.isMatch)) {
                hasMatches = true;
            }

            // Each group has 13 columns (12 words + 1 header)
            int groupSize = 12;
            for (int start = 0; start < tokenInfos.size(); start += groupSize) {
                int end = Math.min(start + groupSize, tokenInfos.size());
                List<TokenInfo> group = tokenInfos.subList(start, end);

                // Create a three-row table
                htmlBuilder.append("<table class='word-table'>");

                // First row: Original words
                htmlBuilder.append("<tr class='word-row'>");
                // Add header cell
                htmlBuilder.append("<td class='word-header'>form</td>");
                for (TokenInfo token : group) {
                    String cellClass = "word-cell";
                    if (token.isMatch) {
                        if (token.isLeftNeighbor) {
                            cellClass = "left-highlight";
                        } else if (token.isRightNeighbor) {
                            cellClass = "right-highlight";
                        } else if (token.isExactMatch || token.isRegexMatch) {
                            cellClass = "highlight";
                        }
                    }
                    htmlBuilder.append("<td class='").append(cellClass).append("'>")
                            .append(escapeHtml(token.token)).append("</td>");
                }
                // Fill in cells if less than 12
                for (int j = group.size(); j < groupSize; j++) {
                    htmlBuilder.append("<td class='word-cell'></td>");
                }
                htmlBuilder.append("</tr>");

                // Second row: Lemmas
                htmlBuilder.append("<tr class='word-row'>");
                // Add header cell
                htmlBuilder.append("<td class='word-header'>lemma</td>");
                for (TokenInfo token : group) {
                    String cellClass = "word-cell";
                    if (token.isMatch && token.isLemmaMatch) {
                        cellClass = "highlight";
                    }
                    htmlBuilder.append("<td class='").append(cellClass).append("'>")
                            .append(escapeHtml(token.lemma)).append("</td>");
                }
                // Fill in cells if less than 12
                for (int j = group.size(); j < groupSize; j++) {
                    htmlBuilder.append("<td class='word-cell'></td>");
                }
                htmlBuilder.append("</tr>");

                // Third row: POS tags
                htmlBuilder.append("<tr class='word-row'>");
                // Add header cell
                htmlBuilder.append("<td class='word-header'>POS</td>");
                for (TokenInfo token : group) {
                    String cellClass = "word-cell";
                    if (token.isMatch && token.isPosMatch) {
                        cellClass = "highlight";
                    }
                    htmlBuilder.append("<td class='").append(cellClass).append("'>")
                            .append(escapeHtml(token.pos)).append("</td>");
                }
                // Fill in cells if less than 12
                for (int j = group.size(); j < groupSize; j++) {
                    htmlBuilder.append("<td class='word-cell'></td>");
                }
                htmlBuilder.append("</tr>");

                htmlBuilder.append("</table>"); // Close word-table
            }
            htmlBuilder.append("</div>"); // Close sentence-container
        }

        if (!hasMatches) {
            htmlBuilder.append("<div class='no-results'>No matching results found</div>");
        }
        htmlBuilder.append("</div>");
        return htmlBuilder.toString();
    }

    private class TokenInfo {
        String token;
        String lemma;
        String pos;
        boolean isExactMatch = false;
        boolean isLemmaMatch = false;
        boolean isPosMatch = false;
        boolean isRegexMatch = false;
        boolean isLeftNeighbor = false;
        boolean isRightNeighbor = false;
        boolean isMatch = false;

        public TokenInfo(String token, String lemma, String pos) {
            this.token = token;
            this.lemma = lemma;
            this.pos = pos;
        }
    }

    private List<TokenInfo> processSentenceForTable(String sentence, SearchCondition condition) {
        List<TokenInfo> tokenInfos = new ArrayList<>();
        if (sentence == null || sentence.trim().isEmpty()) return tokenInfos;

        String[] tokens = tokenizer.tokenize(sentence);
        String[] tags = posTagger.tag(tokens);

        // Mark neighbor positions
        boolean[] isLeftNeighbor = new boolean[tokens.length];
        boolean[] isRightNeighbor = new boolean[tokens.length];
        boolean[] isExactMatch = new boolean[tokens.length];
        boolean[] isRegexMatch = new boolean[tokens.length];
        boolean[] isLemmaMatch = new boolean[tokens.length];
        boolean[] isPosMatch = new boolean[tokens.length];

        if (condition != null) {
            // 1. Calculate all matching conditions
            for (int i = 0; i < tokens.length; i++) {
                // Exact match
                if (condition.getExactWord() != null && !condition.getExactWord().isEmpty()) {
                    if (condition.isCaseSensitive()) {
                        isExactMatch[i] = tokens[i].equals(condition.getExactWord());
                    } else {
                        isExactMatch[i] = tokens[i].equalsIgnoreCase(condition.getExactWord());
                    }
                }

                // Regex match
                if (condition.getRegex() != null && !condition.getRegex().isEmpty()) {
                    try {
                        Pattern pattern = Pattern.compile(condition.getRegex());
                        isRegexMatch[i] = pattern.matcher(tokens[i]).find();
                    } catch (PatternSyntaxException e) {
                        System.err.println("Invalid regex pattern: " + condition.getRegex());
                    }
                }

                // Lemma match
                String lemma = lemmatize(tokens[i]);
                if (condition.getWordLemma() != null && !condition.getWordLemma().isEmpty()) {
                    if (condition.isCaseSensitive()) {
                        isLemmaMatch[i] = lemma.equals(condition.getWordLemma());
                    } else {
                        isLemmaMatch[i] = lemma.equalsIgnoreCase(condition.getWordLemma());
                    }
                }

                // POS tag match
                if (condition.getPosTag() != null && !condition.getPosTag().trim().isEmpty()) {
                    isPosMatch[i] = tags[i].equals(condition.getPosTag().trim());
                }
            }

            // 2. Mark neighbors (based on exact match or regex match)
            if (condition.isNeighbored()) {
                for (int i = 0; i < tokens.length; i++) {
                    if (isExactMatch[i] || isRegexMatch[i]) {
                        // Mark left neighbors
                        int leftCount = 0;
                        int j = i - 1;
                        while (j >= 0 && leftCount < condition.getLeftNeighbor()) {
                            if (!isPunctuation(tokens[j])) {
                                isLeftNeighbor[j] = true;
                                leftCount++;
                            }
                            j--;
                        }

                        // Mark right neighbors
                        int rightCount = 0;
                        j = i + 1;
                        while (j < tokens.length && rightCount < condition.getRightNeighbor()) {
                            if (!isPunctuation(tokens[j])) {
                                isRightNeighbor[j] = true;
                                rightCount++;
                            }
                            j++;
                        }
                    }
                }
            }
        }

        // Process all tokens, including punctuation
        for (int i = 0; i < tokens.length; i++) {
            TokenInfo tokenInfo = new TokenInfo(
                    tokens[i],
                    lemmatize(tokens[i]),
                    tags[i]
            );

            // Check matching conditions
            if (condition != null) {
                // Calculate whether all set conditions are met
                boolean satisfiesAllConditions = true;
                List<String> activeConditions = new ArrayList<>();

                // Check each condition if set and met
                if (condition.getExactWord() != null && !condition.getExactWord().isEmpty()) {
                    if (!isExactMatch[i]) satisfiesAllConditions = false;
                    activeConditions.add("exact");
                }

                if (condition.getRegex() != null && !condition.getRegex().isEmpty()) {
                    if (!isRegexMatch[i]) satisfiesAllConditions = false;
                    activeConditions.add("regex");
                }

                if (condition.getWordLemma() != null && !condition.getWordLemma().isEmpty()) {
                    if (!isLemmaMatch[i]) satisfiesAllConditions = false;
                    activeConditions.add("lemma");
                }

                if (condition.getPosTag() != null && !condition.getPosTag().trim().isEmpty()) {
                    if (!isPosMatch[i]) satisfiesAllConditions = false;
                    activeConditions.add("pos");
                }

                // If no conditions are set, show all results
                if (activeConditions.isEmpty()) {
                    satisfiesAllConditions = true;
                }

                // Set matching flags
                tokenInfo.isExactMatch = isExactMatch[i];
                tokenInfo.isRegexMatch = isRegexMatch[i];
                tokenInfo.isLemmaMatch = isLemmaMatch[i];
                tokenInfo.isPosMatch = isPosMatch[i];
                tokenInfo.isLeftNeighbor = isLeftNeighbor[i];
                tokenInfo.isRightNeighbor = isRightNeighbor[i];
                tokenInfo.isMatch = satisfiesAllConditions ||
                        tokenInfo.isLeftNeighbor ||
                        tokenInfo.isRightNeighbor;
            }

            tokenInfos.add(tokenInfo);
        }
        return tokenInfos;
    }


    // Add method to get sentence array
//    public String[] getSentences(String text) {
//        return sentenceDetector.sentDetect(text);
//    }

    // Processed sentence result class
    private class ProcessedSentenceResult {
        private StringBuilder html = new StringBuilder();
        private boolean hasMatches = false;

        public void appendToken(String tokenHtml, boolean isMatch) {
            html.append(tokenHtml).append(" ");
            if (isMatch) hasMatches = true;
        }

        public String getHtml() {
            return html.toString();
        }

        public boolean containsMatches() {
            return hasMatches;
        }
    }

    private ProcessedSentenceResult processSentence(String sentence, SearchCondition condition) {
        ProcessedSentenceResult result = new ProcessedSentenceResult();
        if (condition == null || sentence.trim().isEmpty()) return result;

        String[] tokens = tokenizer.tokenize(sentence);
        String[] tags = posTagger.tag(tokens);
        boolean[] isLeftNeighbor = new boolean[tokens.length];
        boolean[] isRightNeighbor = new boolean[tokens.length];
        boolean[] isExactMatch = new boolean[tokens.length];

        // Mark keyword positions
        for (int i = 0; i < tokens.length; i++) {
            if (condition.getExactWord() != null &&
                    !condition.getExactWord().isEmpty() &&
                    tokens[i].equalsIgnoreCase(condition.getExactWord())) {
                isExactMatch[i] = true;
            }
        }

        // Mark neighbor words
        if (condition.isNeighbored() && condition.getExactWord() != null) {
            for (int i = 0; i < tokens.length; i++) {
                if (isExactMatch[i]) {
                    // Mark left neighbors
                    int leftCount = 0;
                    int j = i - 1;
                    while (j >= 0 && leftCount < condition.getLeftNeighbor()) {
                        if (!isPunctuation(tokens[j])) {
                            isLeftNeighbor[j] = true;
                            leftCount++;
                        }
                        j--;
                    }

                    // Mark right neighbors
                    int rightCount = 0;
                    j = i + 1;
                    while (j < tokens.length && rightCount < condition.getRightNeighbor()) {
                        if (!isPunctuation(tokens[j])) {
                            isRightNeighbor[j] = true;
                            rightCount++;
                        }
                        j++;
                    }
                }
            }
        }

        // Process each token
        for (int i = 0; i < tokens.length; i++) {
            // Skip punctuation
            if (isPunctuation(tokens[i])) {
                result.appendToken(escapeHtml(tokens[i]), false);
                continue;
            }

            String token = tokens[i];
            String tag = tags[i];
            boolean isExactMatchFlag = isExactMatch[i];
            boolean isLeftNeighborFlag = isLeftNeighbor[i];
            boolean isRightNeighborFlag = isRightNeighbor[i];
            boolean satisfiesAllConditions = true;
            List<String> annotationsList = new ArrayList<>();
            boolean isRegexMatch = false;

            // Check exact match condition
            if (condition.getExactWord() != null && !condition.getExactWord().isEmpty()) {
                if (!isExactMatchFlag) satisfiesAllConditions = false;
            }

            // Check lemma match condition
            if (condition.getWordLemma() != null && !condition.getWordLemma().isEmpty()) {
                String lemma = lemmatize(token);
                if (lemma.equalsIgnoreCase(condition.getWordLemma())) {
                    annotationsList.add("lemma: " + condition.getWordLemma());
                } else if (satisfiesAllConditions) {
                    satisfiesAllConditions = false;
                }
            }

            // Check POS tag condition
            if (condition.getPosTag() != null && !condition.getPosTag().trim().isEmpty()) {
                if (tag.equals(condition.getPosTag().trim())) {
                    annotationsList.add("POS: " + tag);
                } else if (satisfiesAllConditions) {
                    satisfiesAllConditions = false;
                }
            }

            // Check regex condition
            if (condition.getRegex() != null && !condition.getRegex().isEmpty()) {
                if (isRegexMatch(token, condition.getRegex())) {
                    isRegexMatch = true;
                } else if (satisfiesAllConditions) {
                    satisfiesAllConditions = false;
                }
            }

            // If no conditions set, show all
            if (condition.getExactWord() == null &&
                    condition.getWordLemma() == null &&
                    condition.getPosTag() == null &&
                    condition.getRegex() == null) {
                satisfiesAllConditions = true;
            }

            // Neighbor markers not affected by AND relation
            boolean isMatch = isExactMatchFlag || isLeftNeighborFlag || isRightNeighborFlag;
            if (!satisfiesAllConditions && !isMatch) {
                result.appendToken(escapeHtml(token), false);
                continue;
            }

            // Build HTML
            StringBuilder tokenHtml = new StringBuilder();
            List<String> classes = new ArrayList<>();

            // Determine main class
            if (isExactMatchFlag) classes.add("exact-match");
            else if (isLeftNeighborFlag) classes.add("left-neighbor");
            else if (isRightNeighborFlag) classes.add("right-neighbor");

            // Add regex match handling
            if (isRegexMatch) tokenHtml.append("<span class='regex-brace'>{</span><span class='regex-brace'>{</span>");

            // Build class string
            if (!classes.isEmpty()) {
                tokenHtml.append("<span class='").append(String.join(" ", classes)).append("'>")
                        .append(escapeHtml(token)).append("</span>");
            } else {
                tokenHtml.append("<span>").append(escapeHtml(token)).append("</span>");
            }

            // Add regex match end
            if (isRegexMatch) tokenHtml.append("<span class='regex-brace'>}}</span>");

            // Add annotations (if any)
            if (!annotationsList.isEmpty()) {
                tokenHtml.append("<span class='annotation'> #")
                        .append(String.join(", ", annotationsList))
                        .append("# </span>");
            }

            result.appendToken(tokenHtml.toString(), true);
        }
        return result;
    }

    private static String escapeHtml(String content) {
        return content.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String lemmatize(String word) {
        if (lemmatizer == null) return word;
        try {
            String[] tags = posTagger.tag(new String[]{word});
            String[] lemmas = lemmatizer.lemmatize(new String[]{word}, tags);
            return (lemmas.length > 0) ? lemmas[0] : word;
        } catch (Exception e) {
            // Fallback to basic lemmatization
            String[] lemmas = lemmatizer.lemmatize(new String[]{word}, new String[]{"NN"});
            return (lemmas.length > 0) ? lemmas[0] : word;
        }
    }

    // Text statistics class
    public static class TextStatistics {
        public int totalWords;
        public int uniqueWords;
        public double avgWordLength;
        public int sentenceCount;
        public List<Map.Entry<String, Integer>> topFrequentWords;
        public Map<String, Integer> posDistribution;
        public List<Map.Entry<String, Double>> sortedPosDistribution;

        public TextStatistics() {
            this.topFrequentWords = new ArrayList<>();
            this.posDistribution = new HashMap<>();
            this.sortedPosDistribution = new ArrayList<>();
        }
    }
}