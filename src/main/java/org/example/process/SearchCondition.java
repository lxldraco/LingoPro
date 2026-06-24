package org.example.process;

public class SearchCondition {
    private String exactWord;
    private String wordLemma;
    private String posTag;
    private String regex;
    private boolean caseSensitive;
    private boolean neighbored;
    private int leftNeighbor;
    private int rightNeighbor;

    // Constructor
    public SearchCondition() {}

    // Getters and Setters
    public String getExactWord() { return exactWord; }
    public void setExactWord(String exactWord) { this.exactWord = exactWord; }

    public String getWordLemma() { return wordLemma; }
    public void setWordLemma(String wordLemma) { this.wordLemma = wordLemma; }

    public String getPosTag() { return posTag; }
    public void setPosTag(String posTag) { this.posTag = posTag; }

    public String getRegex() { return regex; }
    public void setRegex(String regex) { this.regex = regex; }

    public boolean isCaseSensitive() { return caseSensitive; }
    public void setCaseSensitive(boolean caseSensitive) { this.caseSensitive = caseSensitive; }

    public boolean isNeighbored() { return neighbored; }
    public void setNeighbored(boolean neighbored) { this.neighbored = neighbored; }

    public int getLeftNeighbor() { return leftNeighbor; }
    public void setLeftNeighbor(int leftNeighbor) { this.leftNeighbor = leftNeighbor; }

    public int getRightNeighbor() { return rightNeighbor; }
    public void setRightNeighbor(int rightNeighbor) { this.rightNeighbor = rightNeighbor; }

    // Add regular expression matching status check
//    public boolean hasRegex() { return regex != null && !regex.isEmpty(); }

    @Override
    public String toString() {
        return "SearchCondition{" +
                "exactWord='" + exactWord + '\'' +
                ", wordLemma='" + wordLemma + '\'' +
                ", posTag='" + posTag + '\'' +
                ", regex='" + regex + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", neighbored=" + neighbored +
                ", leftNeighbor=" + leftNeighbor +
                ", rightNeighbor=" + rightNeighbor +
                '}';
    }
}