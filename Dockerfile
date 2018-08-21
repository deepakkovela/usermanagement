FROM openjdk:8u171-jdk-slim
RUN mkdir -p /app/
ADD build/libs/usermanagement-0.0.1-SNAPSHOT.jar /app/usermanagement.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/usermanagement.jar"]