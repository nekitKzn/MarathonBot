FROM openjdk:21-slim
COPY /build/libs/MarathonBot-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
EXPOSE 8080
