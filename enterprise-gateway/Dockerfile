FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY enterprise-gateway/target/enterprise-gateway-1.0.0.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
