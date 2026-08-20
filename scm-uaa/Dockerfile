FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

ARG VERSION=''
ENV VERSION=$VERSION
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV JDWP_OPTS=""
ENV SERVER_PORT=8000
ENV SPRING_APPLICATION_NAME=scm-uaa

WORKDIR /home/app/

RUN apk add --no-cache \
    ttf-dejavu \
    fontconfig

COPY build/libs/scm-uaa-${VERSION}.jar uaa.jar


ENTRYPOINT ["sh", "-c", "java -jar /app/scm-uaa.jar --server.port=$SERVER_PORT"]

