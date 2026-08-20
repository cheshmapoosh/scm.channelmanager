FROM reg.daneshrefah.ir/modern-banking/openjdk:latest

WORKDIR /home/app/
ARG VERSION=''
ENV VERSION=$VERSION
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"
ENV JDWP_OPTS=""
ENV SERVER_PORT=8083
ENV SPRING_APPLICATION_NAME=scm-web


COPY build/libs/scm-web-${VERSION}.jar scm-web.jar



ENTRYPOINT ["sh", "-c", "java -jar /app/scm-web.jar --server.port=$SERVER_PORT"]


