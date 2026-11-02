package com.voiceforge.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "voice_profiles")
public class VoiceProfile {

    /** ElevenLabs voice ID. */
    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "model_id", nullable = false, length = 64)
    private String modelId = "eleven_multilingual_v2";

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal stability = new BigDecimal("0.50");

    @Column(name = "similarity_boost", nullable = false, precision = 3, scale = 2)
    private BigDecimal similarityBoost = new BigDecimal("0.80");

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected VoiceProfile() {
    }

    public VoiceProfile(String id, String name, String modelId, BigDecimal stability, BigDecimal similarityBoost) {
        this.id = id;
        this.name = name;
        this.modelId = modelId;
        this.stability = stability;
        this.similarityBoost = similarityBoost;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getModelId() { return modelId; }
    public BigDecimal getStability() { return stability; }
    public BigDecimal getSimilarityBoost() { return similarityBoost; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
