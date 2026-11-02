package com.voiceforge.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_segments")
public class JobSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private SynthesisJob job;

    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder;

    @Column(name = "voice_id", nullable = false, length = 64)
    private String voiceId;

    @Column(name = "text_content", nullable = false)
    private String textContent;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "segment_status")
    private SegmentStatus status = SegmentStatus.WAITING;

    @Column(name = "s3_key", length = 1024)
    private String s3Key;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected JobSegment() {
    }

    public JobSegment(int sequenceOrder, String voiceId, String textContent, String contentHash) {
        this.sequenceOrder = sequenceOrder;
        this.voiceId = voiceId;
        this.textContent = textContent;
        this.contentHash = contentHash;
    }

    void setJob(SynthesisJob job) { this.job = job; }

    public UUID getId() { return id; }
    public SynthesisJob getJob() { return job; }
    public int getSequenceOrder() { return sequenceOrder; }
    public String getVoiceId() { return voiceId; }
    public String getTextContent() { return textContent; }
    public String getContentHash() { return contentHash; }
    public SegmentStatus getStatus() { return status; }
    public void setStatus(SegmentStatus status) { this.status = status; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
