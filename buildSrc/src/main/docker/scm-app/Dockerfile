FROM reg.daneshrefah.ir/modern-banking/scm-base:1.0.1

# Set environment variables
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV SERVER_PORT=8080
ENV SPRING_APPLICATION_NAME=scm-config
ARG VERSION=8.2.0
ENV ARTIFACT=build/libs/$SPRING_APPLICATION_NAME-$VERSION.jar
EXPOSE $SERVER_PORT

WORKDIR /app
COPY ../../../../../scm-config/build/libs/scm-config-$VERSION.jar .
COPY ../../../../../scm-cache/build/libs/scm-cache-$VERSION.jar .
COPY ../../../../../scm-uaa/build/libs/scm-uaa-$VERSION.jar .
COPY ../../../../../scm-web/build/libs/scm-web-$VERSION.jar .
COPY ../../../../../scm-web/build/libs/scm-logging-$VERSION.jar .

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/$ARTIFACT --sever.port=$SERVER_PORT"]