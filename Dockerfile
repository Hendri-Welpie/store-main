# =============================================================================
# Stage 1 — Build the fat JAR
# =============================================================================
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy wrapper + build scripts first — changes to these are rare,
# so this layer is cached across most rebuilds.
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Pre-download dependencies into a cached layer.
# The "|| true" prevents a failure here from stopping the build.
RUN ./gradlew dependencies --no-daemon -q || true

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# =============================================================================
# Stage 2 — Extract Spring Boot layers
# Spring Boot creates a layered JAR by default. Extracting the layers means
# Docker only rebuilds the "application" layer (your code) on each commit;
# the large "dependencies" layer is reused from cache.
# =============================================================================
FROM eclipse-temurin:17-jre AS extractor
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# =============================================================================
# Stage 3 — Lean runtime image
# =============================================================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for the HEALTHCHECK (temurin JRE images are Ubuntu-based).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as a non-root user — principle of least privilege.
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copy layers in ascending order of how often they change.
# Docker invalidates all subsequent layers when one changes, so put the
# most stable layers first.
COPY --from=extractor --chown=appuser:appgroup /app/dependencies ./
COPY --from=extractor --chown=appuser:appgroup /app/spring-boot-loader ./
COPY --from=extractor --chown=appuser:appgroup /app/snapshot-dependencies ./
COPY --from=extractor --chown=appuser:appgroup /app/application ./

USER appuser

EXPOSE 8080

# Ask Docker / orchestrators to check the Spring Boot health endpoint.
# --start-period gives the JVM + Liquibase time to fully start.
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    # Use up to 75 % of the container's cgroup memory limit as max heap.
    # Avoids hardcoding -Xmx which breaks when the container memory changes.
    "-XX:MaxRAMPercentage=75.0", \
    # Start with 50 % so the JVM doesn't over-reserve on startup.
    "-XX:InitialRAMPercentage=50.0", \
    # Kill the process immediately on OutOfMemoryError — let the orchestrator
    # restart it rather than running in a degraded, potentially deadlocked state.
    "-XX:+ExitOnOutOfMemoryError", \
    # Capture a heap dump on OOM for post-mortem analysis.
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
    # Faster SecureRandom — the default /dev/random on Linux can block
    # waiting for entropy, which delays startup and request handling.
    "-Djava.security.egd=file:/dev/./urandom", \
    # Disable JMX — use Actuator endpoints instead in containerised environments.
    "-Dspring.jmx.enabled=false", \
    # Spring Boot 3.2+ layered-jar launcher class.
    "org.springframework.boot.loader.launch.JarLauncher"]
