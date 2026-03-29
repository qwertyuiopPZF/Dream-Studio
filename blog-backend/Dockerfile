FROM maven:3.9.6-amazoncorretto-21 AS builder

WORKDIR /app

COPY pom.xml ./pom.xml
COPY blog-common/pom.xml ./blog-common/pom.xml
COPY blog-pojo/pom.xml ./blog-pojo/pom.xml
COPY blog-server/pom.xml ./blog-server/pom.xml

RUN mvn -pl blog-server -am dependency:go-offline -DskipTests

COPY blog-common ./blog-common
COPY blog-pojo ./blog-pojo
COPY blog-server ./blog-server

RUN mvn -pl blog-server -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/blog-server/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
