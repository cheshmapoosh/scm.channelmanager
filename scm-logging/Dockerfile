FROM reg.daneshrefah.ir/modern-banking/scm-base:1.0.1

# Set environment variables
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV SERVER_PORT=9090
ENV SPRING_APPLICATION_NAME=scm-logging
ARG VERSION=8.3.0
ENV ARTIFACT=build/libs/$SPRING_APPLICATION_NAME-$VERSION.jar
EXPOSE $SERVER_PORT

WORKDIR /app
COPY build/libs/$ARTIFACT .

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/$ARTIFACT --sever.port=$SERVER_PORT"]