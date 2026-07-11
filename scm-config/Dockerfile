FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

# Set environment variables
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV SERVER_PORT=8080
ENV SCM_APP=scm-config
ARG VERSION=''

ENV ARTIFACT=build/libs/$SCM_APP-$VERSION.jar
EXPOSE $SERVER_PORT

WORKDIR /app

COPY $ARTIFACT scm-config.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/scm-config.jar --sever.port=$SERVER_PORT"]
