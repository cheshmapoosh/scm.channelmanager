FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

WORKDIR /app

ARG VERSION

COPY build/libs/scm-cache-${VERSION}.jar scm-cache.jar

ENTRYPOINT ["java", "-jar", "/app/scm-cache.jar"]

