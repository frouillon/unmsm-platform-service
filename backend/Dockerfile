# Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package \
    && JAR="$(ls target/platform-*.jar | grep -v plain | head -n 1)" \
    && cp "$JAR" /tmp/app.jar

# Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd --system --create-home --uid 10001 appuser \
    && mkdir -p /data/evidencias \
    && chown -R appuser:appuser /app /data

COPY --from=build /tmp/app.jar /app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /actuator/health HTTP/1.1\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n' >&3 && cat <&3 | grep -q UP"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
