FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

WORKDIR /home/app/
ARG VERSION


COPY build/libs/scm-web-${VERSION}.jar scm-web.jar



ENTRYPOINT ["java", "-jar", "/app/scm-web.jar"]


