FROM reg.daneshrefah.ir/modern-banking/openjdk:latest
ENV SERVER_PORT=8888
ENV SPRING_APPLICATION_NAME=scm-cache
ARG VERSION='9.0.1'

ENV ARTIFACT=build/libs/$SPRING_APPLICATION_NAME-$VERSION.jar
EXPOSE $SERVER_PORT

WORKDIR /app

COPY $ARTIFACT scm-cache.jar

ENTRYPOINT ["sh", "-c", "java -jar /app/scm-cache.jar --server.port=$SERVER_PORT"]