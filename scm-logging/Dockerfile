FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV SERVER_PORT=9090
ENV SPRING_APPLICATION_NAME=scm-logging

ARG VERSION

WORKDIR /app

COPY build/libs/scm-logging-${VERSION}.jar logging.jar

EXPOSE 9090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/logging.jar --server.port=$SERVER_PORT"]

