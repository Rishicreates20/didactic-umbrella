package com.voiceforge.ingestion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManuscriptParserTest {

    private final ManuscriptParser parser = new ManuscriptParser();

    @Test
    void parsesSpeakerTagsFromThePrdExample() {
        String manuscript = "[Narrator]: The morning fog lingered over the bay.\n[Captain]: Steady as she goes, crew!";
        assertThat(parser.parse(manuscript)).containsExactly(
                new SpeakerBlock("Narrator", "The morning fog lingered over the bay."),
                new SpeakerBlock("Captain", "Steady as she goes, crew!"));
    }

    @Test
    void untaggedLinesContinueTheActiveSpeaker() {
        String manuscript = "[Alice]: Hello there.\nHow are you?\n\n[Bob]: Fine.";
        assertThat(parser.parse(manuscript)).containsExactly(
                new SpeakerBlock("Alice", "Hello there. How are you?"),
                new SpeakerBlock("Bob", "Fine."));
    }

    @Test
    void textBeforeAnyTagHasNoSpeaker() {
        assertThat(parser.parse("Once upon a time.\n[Alice]: Hi.")).containsExactly(
                new SpeakerBlock(null, "Once upon a time."),
                new SpeakerBlock("Alice", "Hi."));
    }

    @Test
    void stripsOneWrappingPairOfQuotes() {
        assertThat(parser.parse("[Alice]: \"Run!\"")).containsExactly(new SpeakerBlock("Alice", "Run!"));
        assertThat(parser.parse("[Alice]: \"Run,\" she said, \"now.\""))
                .containsExactly(new SpeakerBlock("Alice", "\"Run,\" she said, \"now.\""));
    }

    @Test
    void emptyManuscriptYieldsNoBlocks() {
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }
}
