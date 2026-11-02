package com.voiceforge.ingestion;

/**
 * A contiguous run of manuscript text attributed to one speaker.
 *
 * @param speaker the speaker tag (e.g. "Narrator"), or {@code null} when the text has no tag
 *                and should use the job's default voice
 * @param text    the raw text spoken by that speaker
 */
public record SpeakerBlock(String speaker, String text) {
}
