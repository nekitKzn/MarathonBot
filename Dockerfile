FROM openjdk:21-slim
COPY /build/libs/MarathonBot-7.jar app.jar
CMD ["java", "-jar", "app.jar"]
EXPOSE 8080
