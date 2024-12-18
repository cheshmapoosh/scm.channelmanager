FROM reg.daneshrefah.ir/modern-banking/openjdk:17-alpine

WORKDIR /app
ARG VERSION=''
COPY build/libs/scm-cache-${VERSION}.jar scm-cache.jar

CMD ["java", "-jar", "/app/scm-cache.jar"]
