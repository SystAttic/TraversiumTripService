FROM eclipse-temurin:17-jdk
MAINTAINER Traversium Developers
WORKDIR /opt/trip-service

COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/trip-service/app.jar"]