FROM eclipse-temurin:21

WORKDIR /app

COPY . .

RUN chmod +x mvnw || true

RUN ./mvnw package || mvn package

CMD ["java", "-jar", "target/prode-1.0.0.jar"]