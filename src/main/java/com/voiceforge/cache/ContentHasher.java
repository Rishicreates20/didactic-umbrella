package com.voiceforge.cache;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Builds the deterministic content-addressable key for a segment (FR-04, PRD section 3.2.2):
 * {@code SHA-256(voice_id || model_id || stability || similarity || normalized_text)}.
 *
 * <p>Fields are joined with the ASCII unit separator so that no two different inputs can produce
 * the same pre-image. Text is NFC-normalized with whitespace collapsed; voice settings are fixed
 * to two decimals so that 0.5 and 0.50 hash the same.
 */
@Component
public class ContentHasher {

    private static final char SEPARATOR = '\u001F';

    public String hash(String voiceId, String modelId, BigDecimal stability, BigDecimal similarityBoost, String text) {
        Objects.requireNonNull(voiceId, "voiceId");
        Objects.requireNonNull(modelId, "modelId");
        String preImage = voiceId + SEPARATOR
                + modelId + SEPARATOR
                + scale(stability) + SEPARATOR
                + scale(similarityBoost) + SEPARATOR
                + normalizeText(text);
        return sha256Hex(preImage);
    }

    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFC).replaceAll("\\s+", " ").trim();
    }

    private static String scale(BigDecimal value) {
        return Objects.requireNonNull(value, "voice setting").setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
