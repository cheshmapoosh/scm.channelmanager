FROM docker:29-cli AS docker-cli

FROM gradle:jdk21-alpine

USER root

COPY --from=docker-cli /usr/local/bin/docker /usr/local/bin/docker

ENV GRADLE_USER_HOME=/home/gradle/.gradle
ENV TZ=Asia/Tehran

USER gradle

WORKDIR /workspace

CMD ["sh"]