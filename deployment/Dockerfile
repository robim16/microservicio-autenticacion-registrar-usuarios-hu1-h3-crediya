FROM eclipse-temurin:21-jre
WORKDIR /app

COPY applications/app-service/build/libs/registrar-usuarios-crediYA.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
