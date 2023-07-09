FROM openjdk:8-jdk-alpine
COPY build/libs/app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]