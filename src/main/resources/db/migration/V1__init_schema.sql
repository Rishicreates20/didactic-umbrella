-- VoiceForge initial schema (PRD section 4.1).
-- FAILED_PARTIAL is added to job_status to support the partial-failure/resume flow (PRD section 7.3).
CREATE TYPE job_status AS ENUM ('PENDING', 'PROCESSING', 'STITCHING', 'COMPLETED', 'FAILED', 'FAILED_PARTIAL');
CREATE TYPE segment_status AS ENUM ('WAITING', 'CACHED', 'SYNTHESIZING', 'COMPLETED', 'FAILED');

CREATE TABLE synthesis_jobs (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title              VARCHAR(255) NOT NULL,
    total_segments     INT NOT NULL DEFAULT 0,
    completed_segments INT NOT NULL DEFAULT 0,
    status             job_status NOT NULL DEFAULT 'PENDING',
    master_s3_key      VARCHAR(1024),
    error_message      TEXT,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE voice_profiles (
    id               VARCHAR(64) PRIMARY KEY, -- ElevenLabs voice ID
    name             VARCHAR(128) NOT NULL,
    model_id         VARCHAR(64) NOT NULL DEFAULT 'eleven_multilingual_v2',
    stability        DECIMAL(3, 2) NOT NULL DEFAULT 0.50,
    similarity_boost DECIMAL(3, 2) NOT NULL DEFAULT 0.80,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_segments (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id         UUID NOT NULL REFERENCES synthesis_jobs(id) ON DELETE CASCADE,
    sequence_order INT NOT NULL,
    voice_id       VARCHAR(64) NOT NULL REFERENCES voice_profiles(id),
    text_content   TEXT NOT NULL,
    content_hash   VARCHAR(64) NOT NULL, -- SHA-256 hex
    status         segment_status NOT NULL DEFAULT 'WAITING',
    s3_key         VARCHAR(1024),
    duration_ms    INT,
    retry_count    INT DEFAULT 0,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_job_sequence UNIQUE (job_id, sequence_order)
);

CREATE INDEX idx_job_segments_hash ON job_segments(content_hash);
CREATE INDEX idx_job_status ON synthesis_jobs(status);
