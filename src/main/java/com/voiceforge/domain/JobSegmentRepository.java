package com.voiceforge.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobSegmentRepository extends JpaRepository<JobSegment, UUID> {
}
