FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV JDWP_OPTS=""
ENV SERVER_PORT=8080

ARG VERSION="8.5.3"
ENV VERSION=$VERSION
ARG SPRING_APPLICATION_NAME

ENV SPRING_APPLICATION_NAME=$SPRING_APPLICATION_NAME

EXPOSE 8080
EXPOSE 8888
EXPOSE 5701
EXPOSE 8082
EXPOSE 5005

WORKDIR /app

COPY scm-config/build/libs/scm-config-${VERSION}.jar .
COPY scm-cache/build/libs/scm-cache-${VERSION}.jar .
COPY scm-uaa/build/libs/scm-uaa-${VERSION}.jar .
COPY scm-web/build/libs/scm-web-${VERSION}.jar .
COPY scm-logging/build/libs/scm-logging-${VERSION}.jar .

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $JDWP_OPTS -jar /app/$SPRING_APPLICATION_NAME-$VERSION.jar --server.port=$SERVER_PORT"]


#docker run -d \
#  --name scm-config \
#  -e SPRING_APPLICATION_NAME=scm-config \
#  -e VERSION=8.5.3 \
#  -p 8081:8080 \
#  reg.daneshrefah.ir/modern-banking/scm-all-in-one:8.5.3