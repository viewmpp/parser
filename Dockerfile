FROM gradle:9.6.1-jdk25 AS builder

WORKDIR /opt/app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src ./src
RUN gradle build -x test --parallel --build-cache

FROM eclipse-temurin:25-jre

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

RUN useradd --system --uid 10001 parser

WORKDIR /opt/app

COPY --from=builder /opt/app/build/libs/*.jar /opt/app/app.jar

USER parser
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/opt/app/app.jar"]