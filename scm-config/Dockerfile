FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

# Set environment variables
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV SERVER_PORT=8080
ENV SPRING_APPLICATION_NAME=scm-config
ARG VERSION=''

ENV ARTIFACT=build/libs/$SPRING_APPLICATION_NAME-$VERSION.jar
EXPOSE $SERVER_PORT

WORKDIR /app

COPY $ARTIFACT scm-config.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/scm-config.jar --server.port=$SERVER_PORT"]
