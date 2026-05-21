FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn package

CMD ["java", "-jar", "target/prode-1.0.0.jar"]