package com.voiceforge.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LinguisticChunkerTest {

    private final LinguisticChunker chunker = new LinguisticChunker(new ChunkingProperties(250));

    @Test
    void splitsOnSentenceTerminators() {
        assertThat(chunker.chunk("The fog lingered. Was it morning? Yes! It was."))
                .containsExactly("The fog lingered.", "Was it morning?", "Yes!", "It was.");
    }

    @Test
    void doesNotSplitOnAbbreviations() {
        assertThat(chunker.chunk("Dr. Smith met Mr. Jones, e.g. at noon. They talked."))
                .containsExactly("Dr. Smith met Mr. Jones, e.g. at noon.", "They talked.");
    }

    @Test
    void doesNotSplitOnInitialsOrDecimals() {
        assertThat(chunker.chunk("J. R. R. Tolkien wrote 3.5 books. Not really."))
                .containsExactly("J. R. R. Tolkien wrote 3.5 books.", "Not really.");
    }

    @Test
    void keepsClosingQuotesWithTheSentence() {
        assertThat(chunker.chunk("\"Steady as she goes!\" he said. \"Aye.\""))
                .containsExactly("\"Steady as she goes!\" he said.", "\"Aye.\"");
    }

    @Test
    void handlesEllipsesAndRepeatedPunctuation() {
        assertThat(chunker.chunk("Wait... what?! No."))
                .containsExactly("Wait...", "what?!", "No.");
    }

    @Test
    void collapsesWhitespaceAndIgnoresBlankInput() {
        assertThat(chunker.chunk("  One.\n\n  Two.  ")).containsExactly("One.", "Two.");
        assertThat(chunker.chunk("   ")).isEmpty();
        assertThat(chunker.chunk(null)).isEmpty();
    }

    @Test
    void splitsLongSentencesAtClauseBoundariesWithinTheCap() {
        LinguisticChunker small = new LinguisticChunker(new ChunkingProperties(40));
        List<String> chunks = small.chunk("The ship left the harbour at dawn, the crew sang loudly; the captain stayed silent.");
        assertThat(chunks).containsExactly(
                "The ship left the harbour at dawn,",
                "the crew sang loudly;",
                "the captain stayed silent.");
        assertThat(chunks).allSatisfy(c -> assertThat(c.length()).isLessThanOrEqualTo(40));
    }

    @Test
    void fallsBackToWordBoundariesWithoutBreakingWords() {
        LinguisticChunker small = new LinguisticChunker(new ChunkingProperties(20));
        String text = "alpha bravo charlie delta echo foxtrot golf hotel";
        List<String> chunks = small.chunk(text);
        assertThat(chunks).allSatisfy(c -> assertThat(c.length()).isLessThanOrEqualTo(20));
        assertThat(String.join(" ", chunks)).isEqualTo(text);
    }

    @Test
    void neverExceedsDefaultCapOnLongRunOnText() {
        String sentence = "word ".repeat(200).trim() + ".";
        assertThat(chunker.chunk(sentence)).allSatisfy(c -> assertThat(c.length()).isLessThanOrEqualTo(250));
    }
}
