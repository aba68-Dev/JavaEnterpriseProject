FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY enterprise-soap-service/target/enterprise-soap-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
