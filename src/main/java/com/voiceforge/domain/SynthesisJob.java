package com.voiceforge.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "synthesis_jobs")
public class SynthesisJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "total_segments", nullable = false)
    private int totalSegments;

    @Column(name = "completed_segments", nullable = false)
    private int completedSegments;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "job_status")
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "master_s3_key", length = 1024)
    private String masterS3Key;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<JobSegment> segments = new ArrayList<>();

    protected SynthesisJob() {
    }

    public SynthesisJob(String title) {
        this.title = title;
    }

    public void addSegment(JobSegment segment) {
        segment.setJob(this);
        segments.add(segment);
        totalSegments = segments.size();
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public int getTotalSegments() { return totalSegments; }
    public int getCompletedSegments() { return completedSegments; }
    public void setCompletedSegments(int completedSegments) { this.completedSegments = completedSegments; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public String getMasterS3Key() { return masterS3Key; }
    public void setMasterS3Key(String masterS3Key) { this.masterS3Key = masterS3Key; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<JobSegment> getSegments() { return segments; }
}
