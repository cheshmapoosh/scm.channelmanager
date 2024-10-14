FROM openjdk:17-jdk-alpine

WORKDIR /home/app/
RUN apk add --no-cache curl
COPY scm-config-server-boot.jar .
CMD ["java", "-jar", "/home/app/scm-config-server-boot.jar"]
