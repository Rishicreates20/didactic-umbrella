package com.voiceforge.ingestion;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a manuscript into speaker blocks (FR-01).
 *
 * <p>A line starting with {@code [Name]:} switches the active speaker. Untagged lines continue the
 * active speaker; text before the first tag has no speaker and falls back to the default voice.
 * A single pair of quotes wrapping a tagged line's text is removed.
 */
@Component
public class ManuscriptParser {

    private static final Pattern SPEAKER_TAG = Pattern.compile("^\\s*\\[([^\\[\\]]{1,64})]\\s*:\\s*(.*)$");

    public List<SpeakerBlock> parse(String manuscript) {
        List<SpeakerBlock> blocks = new ArrayList<>();
        if (manuscript == null || manuscript.isBlank()) {
            return blocks;
        }

        String currentSpeaker = null;
        StringBuilder current = new StringBuilder();

        for (String line : manuscript.split("\\R")) {
            Matcher m = SPEAKER_TAG.matcher(line);
            if (m.matches()) {
                flush(blocks, currentSpeaker, current);
                currentSpeaker = m.group(1).trim();
                current.append(stripWrappingQuotes(m.group(2).trim()));
            } else if (!line.isBlank()) {
                if (!current.isEmpty()) {
                    current.append(' ');
                }
                current.append(line.trim());
            }
        }
        flush(blocks, currentSpeaker, current);
        return blocks;
    }

    private static void flush(List<SpeakerBlock> blocks, String speaker, StringBuilder text) {
        String value = text.toString().trim();
        if (!value.isEmpty()) {
            blocks.add(new SpeakerBlock(speaker, value));
        }
        text.setLength(0);
    }

    static String stripWrappingQuotes(String s) {
        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if ((first == '"' && last == '"') || (first == '“' && last == '”')) {
                String inner = s.substring(1, s.length() - 1);
                // Only strip when it is one quoted utterance, not several quoted fragments.
                if (inner.indexOf('"') < 0 && inner.indexOf('“') < 0 && inner.indexOf('”') < 0) {
                    return inner.trim();
                }
            }
        }
        return s;
    }
}
