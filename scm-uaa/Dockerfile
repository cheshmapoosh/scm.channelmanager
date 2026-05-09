FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

ARG VERSION

WORKDIR /home/app/

RUN apk add --no-cache \
    ttf-dejavu \
    fontconfig

COPY build/libs/scm-uaa-${VERSION}.jar uaa.jar

CMD ["java","-jar","/home/app/uaa.jar"]

