# VoiceForge

High-throughput asynchronous text-to-audio pipeline built on Spring Boot 3.3 and Java 21 virtual
threads, using ElevenLabs for speech synthesis.

## Status

Milestone 1 (scaffolding) is in place:

- Maven project on Spring Boot 3.3 / Java 21 with virtual threads enabled
- PostgreSQL schema from the PRD, applied by Flyway, with JPA entities and repositories
- Manuscript parser for `[Speaker]:` tags (FR-01)
- Linguistic chunker with a 250-character cap that never breaks words (FR-02)
- Deterministic SHA-256 content key for the cache-aside layer (FR-04)
- Actuator health and Prometheus metrics at `/actuator/prometheus`
- `docker-compose.yml` for PostgreSQL 16, Redis 7.2 and MinIO (via the `pgsty/minio` fork, since MinIO stopped publishing Docker Hub images)

Next up: the job API (`POST /api/v1/jobs`), the Redis/S3 cache-aside layer, the resilient
ElevenLabs client, and the virtual-thread worker pool.

## Running locally

Requires JDK 21 and Docker. Maven itself is not needed: the Maven wrapper downloads it on first run.

```bash
docker compose up -d      # PostgreSQL on :5433, Redis, MinIO (S3 on :9000, console on :9001)
./mvnw spring-boot:run    # starts on :8080   (Windows: .\mvnw.cmd spring-boot:run)
./mvnw test               # unit tests        (Windows: .\mvnw.cmd test)
```

Configuration is in `src/main/resources/application.yml`. Set `ELEVENLABS_API_KEY` to call
ElevenLabs. Database and Redis settings can be overridden with `VOICEFORGE_DB_URL`,
`VOICEFORGE_DB_USER`, `VOICEFORGE_DB_PASSWORD`, `VOICEFORGE_REDIS_HOST` and `VOICEFORGE_REDIS_PORT`.

## Notes on the schema

`job_status` includes `FAILED_PARTIAL` in addition to the PRD's DDL, because the partial-failure
and resume flow (PRD section 7.3) needs it.
