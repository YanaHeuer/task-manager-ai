# Built from the root of the multi-module project (see build context in docker-compose.yml),
# since the module needs the parent pom.xml and sibling modules for a reactor build (-am).
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY task-service/pom.xml task-service/pom.xml
COPY ai-service/pom.xml ai-service/pom.xml
# Cache dependencies in a separate layer so code changes don't force a re-download
RUN mvn -q -B -pl config-server -am dependency:go-offline || true
COPY . .
RUN mvn -q -B -pl config-server -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/config-server/target/*.jar app.jar
USER app
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
