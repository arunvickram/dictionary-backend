FROM openjdk:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/dictionary-backend-0.0.1-SNAPSHOT-standalone.jar /dictionary-backend/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/dictionary-backend/app.jar"]
