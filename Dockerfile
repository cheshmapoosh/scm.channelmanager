FROM reg.daneshrefah.ir/modern-banking/scm-app

# Set environment variables
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV SERVER_PORT=8080
ENV SPRING_APPLICATION_NAME=scm-config
ARG VERSION=8.0.0
ENV ARTIFACT=build/libs/$SPRING_APPLICATION_NAME-$VERSION.jar
EXPOSE $SERVER_PORT

WORKDIR /app
COPY scm-config/build/libs/*-$VERSION.jar .
COPY scm-cache/build/libs*-$VERSION.jar .
COPY scm-uaa/build/libs/*-$VERSION.jar .
COPY scm-web/build/libs/*-$VERSION.jar .

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/$ARTIFACT --sever.port=$SERVER_PORT"]

