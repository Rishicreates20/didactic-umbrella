package com.voiceforge.ingestion;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Splits text into natural acoustic units (FR-02).
 *
 * <p>Each sentence becomes one chunk, so that editing one sentence only invalidates one cache
 * entry. A sentence longer than the cap is split at the last clause boundary (comma, semicolon,
 * colon, dash) that fits, and only if there is none, at the last space. Words are never split,
 * except for a single word that is itself longer than the cap.
 */
@Component
public class LinguisticChunker {

    /** Lower-cased tokens (without trailing period) that end in "." without ending a sentence. */
    private static final Set<String> ABBREVIATIONS = Set.of(
            "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "st", "mt", "capt", "col", "gen", "lt", "sgt", "rev",
            "vs", "etc", "e.g", "i.e", "cf", "approx", "no", "vol", "fig", "inc", "ltd", "co", "jan", "feb",
            "mar", "apr", "jun", "jul", "aug", "sep", "sept", "oct", "nov", "dec");

    private static final String CLOSING_CHARS = "\"'”’)]";
    private static final String CLAUSE_CHARS = ",;:—–";

    private final int maxChars;

    public LinguisticChunker(ChunkingProperties properties) {
        this.maxChars = properties.maxChunkChars();
    }

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        for (String sentence : splitSentences(normalizeWhitespace(text))) {
            splitOversized(sentence, chunks);
        }
        return chunks;
    }

    static String normalizeWhitespace(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }

    List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        int start = 0;
        int i = 0;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if (c == '.' || c == '!' || c == '?' || c == '…') {
                int end = i + 1;
                while (end < n && isTerminator(text.charAt(end))) {
                    end++;
                }
                int beforeClosing = end;
                while (end < n && CLOSING_CHARS.indexOf(text.charAt(end)) >= 0) {
                    end++;
                }
                boolean atBoundary = end == n || Character.isWhitespace(text.charAt(end));
                // A quote closed by "!" or "?" and followed by a lower-case word is a dialogue tag:
                // "Steady!" he said.
                boolean dialogueTag = end > beforeClosing && nextWordStartsLower(text, end);
                if (atBoundary && !dialogueTag && !(c == '.' && isNonTerminalPeriod(text, start, i, end))) {
                    addIfNotBlank(sentences, text.substring(start, end));
                    start = end;
                }
                i = end;
            } else {
                i++;
            }
        }
        addIfNotBlank(sentences, text.substring(start));
        return sentences;
    }

    private static boolean isTerminator(char c) {
        return c == '.' || c == '!' || c == '?' || c == '…';
    }

    /** True when the period at {@code dot} belongs to an abbreviation, initial or decimal. */
    private static boolean isNonTerminalPeriod(String text, int sentenceStart, int dot, int end) {
        int wordStart = dot;
        while (wordStart > sentenceStart && !Character.isWhitespace(text.charAt(wordStart - 1))) {
            wordStart--;
        }
        String word = text.substring(wordStart, dot);
        // Strip leading punctuation such as an opening quote or parenthesis.
        word = word.replaceFirst("^[\"'“‘(\\[]+", "");
        if (word.isEmpty()) {
            return false;
        }
        String lower = word.toLowerCase(Locale.ROOT);
        if (ABBREVIATIONS.contains(lower)) {
            return true;
        }
        // Single initial like "J." in "J. R. R. Tolkien", but only when followed by a capital.
        if (word.length() == 1 && Character.isUpperCase(word.charAt(0))) {
            return nextWordStartsUpper(text, end);
        }
        return false;
    }

    private static boolean nextWordStartsUpper(String text, int from) {
        int j = from;
        while (j < text.length() && Character.isWhitespace(text.charAt(j))) {
            j++;
        }
        return j < text.length() && Character.isUpperCase(text.charAt(j));
    }

    private static boolean nextWordStartsLower(String text, int from) {
        int j = from;
        while (j < text.length() && Character.isWhitespace(text.charAt(j))) {
            j++;
        }
        return j < text.length() && Character.isLowerCase(text.charAt(j));
    }

    private void splitOversized(String sentence, List<String> out) {
        String rest = sentence.trim();
        while (rest.length() > maxChars) {
            int cut = lastBoundary(rest, CLAUSE_CHARS);
            if (cut < 0) {
                cut = lastSpace(rest);
            }
            if (cut <= 0) {
                // A single token longer than the cap: hard split as a last resort.
                cut = maxChars;
            }
            addIfNotBlank(out, rest.substring(0, cut));
            rest = rest.substring(cut).trim();
        }
        addIfNotBlank(out, rest);
    }

    /** Index just after the last clause character within the cap that is followed by a space. */
    private int lastBoundary(String s, String chars) {
        for (int i = Math.min(maxChars, s.length()) - 1; i > 0; i--) {
            if (chars.indexOf(s.charAt(i)) >= 0 && i + 1 < s.length() && Character.isWhitespace(s.charAt(i + 1))) {
                return i + 1;
            }
        }
        return -1;
    }

    private int lastSpace(String s) {
        for (int i = Math.min(maxChars, s.length() - 1); i > 0; i--) {
            if (Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static void addIfNotBlank(List<String> out, String s) {
        String t = s.trim();
        if (!t.isEmpty()) {
            out.add(t);
        }
    }
}
