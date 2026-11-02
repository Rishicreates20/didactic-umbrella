package com.voiceforge.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SynthesisJobRepository extends JpaRepository<SynthesisJob, UUID> {
}
