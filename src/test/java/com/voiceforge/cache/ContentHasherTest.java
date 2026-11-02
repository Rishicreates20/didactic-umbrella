package com.voiceforge.cache;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ContentHasherTest {

    private final ContentHasher hasher = new ContentHasher();
    private static final BigDecimal STABILITY = new BigDecimal("0.50");
    private static final BigDecimal SIMILARITY = new BigDecimal("0.80");

    @Test
    void isDeterministicAndHex() {
        String a = hasher.hash("voice", "model", STABILITY, SIMILARITY, "Hello world.");
        String b = hasher.hash("voice", "model", STABILITY, SIMILARITY, "Hello world.");
        assertThat(a).isEqualTo(b).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void normalizesWhitespaceAndDecimalScale() {
        String a = hasher.hash("voice", "model", new BigDecimal("0.5"), new BigDecimal("0.8"), "Hello   world.");
        String b = hasher.hash("voice", "model", STABILITY, SIMILARITY, " Hello world. ");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void anyParameterChangeChangesTheKey() {
        String base = hasher.hash("voice", "model", STABILITY, SIMILARITY, "Hello.");
        assertThat(hasher.hash("other", "model", STABILITY, SIMILARITY, "Hello.")).isNotEqualTo(base);
        assertThat(hasher.hash("voice", "other", STABILITY, SIMILARITY, "Hello.")).isNotEqualTo(base);
        assertThat(hasher.hash("voice", "model", new BigDecimal("0.51"), SIMILARITY, "Hello.")).isNotEqualTo(base);
        assertThat(hasher.hash("voice", "model", STABILITY, new BigDecimal("0.81"), "Hello.")).isNotEqualTo(base);
        assertThat(hasher.hash("voice", "model", STABILITY, SIMILARITY, "Hello!")).isNotEqualTo(base);
    }

    @Test
    void fieldBoundariesCannotCollide() {
        assertThat(hasher.hash("ab", "c", STABILITY, SIMILARITY, "x"))
                .isNotEqualTo(hasher.hash("a", "bc", STABILITY, SIMILARITY, "x"));
    }
}
