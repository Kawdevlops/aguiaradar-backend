# syntax=docker/dockerfile:1.7
# =====================================================================
#  AguiaRadar Backend - Dockerfile multi-stage
#  Estagio 1 (build): compila com Maven + JDK 17
#  Estagio 2 (runtime): so o JRE 17 + o .jar (imagem pequena e segura)
# =====================================================================

# ---------- Estagio 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia so o pom primeiro: se o pom nao mudar, o Docker reaproveita
# a camada de dependencias (build muito mais rapido)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
# Os testes rodam no pipeline (job "build-test"); aqui so empacotamos
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL org.opencontainers.image.title="aguiaradar-backend" \
      org.opencontainers.image.description="AguiaRadar - Cidades ESG Inteligentes (Spring Boot + MongoDB)" \
      org.opencontainers.image.licenses="MIT"

WORKDIR /app

# Usuario sem privilegios de root (boa pratica de seguranca)
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /app/target/aguiaradar-backend.jar app.jar
USER app

# Versao gravada na imagem (o pipeline passa o SHA do commit)
ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION} \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# O Docker marca o container como "healthy" so quando a API responde
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- "http://localhost:${PORT}/actuator/health" | grep -q '"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
