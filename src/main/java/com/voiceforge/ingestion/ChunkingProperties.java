package com.voiceforge.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "voiceforge.chunking")
public record ChunkingProperties(@DefaultValue("250") int maxChunkChars) {

    public ChunkingProperties {
        if (maxChunkChars < 20) {
            throw new IllegalArgumentException("voiceforge.chunking.max-chunk-chars must be at least 20");
        }
    }
}
