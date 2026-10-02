# =============================================================================
# Taller Evaluativo 2 - Fase 2: Empaquetamiento Inmutable Multi-Stage
# Backend: studytrack-api
#
# - Stage 1 compila con Maven (JDK 17). Las dependencias se descargan en una
#   capa propia (solo cambia si cambia el pom.xml) para aprovechar la cache.
# - Stage 2 solo contiene el JRE 17 y el .jar: sin Maven, sin JDK, sin fuentes.
# - Se ejecuta con un usuario sin privilegios (appuser).
# - Ninguna imagen base usa el tag ":latest".
# =============================================================================

# ---------- Stage 1: Builder ----------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Versión inmutable del artefacto (el pipeline pasa BUILD_NUMBER + commit corto)
ARG REVISION=1.0.0-local

# 1) Solo el descriptor: capa de dependencias cacheable
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -Drevision=${REVISION}

# 2) Código fuente y empaquetado (las pruebas ya corrieron en el stage
#    "Checkout & Test" del pipeline)
COPY src ./src
RUN mvn -B -q package -DskipTests -Drevision=${REVISION}

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine

ARG REVISION=1.0.0-local
LABEL org.opencontainers.image.title="studytrack-api" \
      org.opencontainers.image.version="${REVISION}"

RUN addgroup -S appgroup && adduser -S -G appgroup appuser
WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/target/studytrack-api.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
