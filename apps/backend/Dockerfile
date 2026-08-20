FROM eclipse-temurin:26-jre-alpine AS builder
WORKDIR /builder

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted


FROM eclipse-temurin:26-jre-alpine
WORKDIR /application

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]