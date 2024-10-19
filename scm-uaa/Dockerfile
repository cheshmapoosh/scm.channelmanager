FROM openjdk:17-jdk-alpine

WORKDIR /home/app/
RUN apk add --no-cache \
    ttf-dejavu \
    fontconfig

COPY scm-uaa-boot.jar .

CMD ["java", "-jar", "/home/app/scm-uaa-boot.jar"]
