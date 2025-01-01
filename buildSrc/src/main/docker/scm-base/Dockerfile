# For run locl docker in intellij ubuntu must be run:
# sudo chmod a+rwx /var/run/docker.sock
# sudo chmod a+rwx /var/run/docker.pid

## Stage 1: Build the application using Gradle (or Maven)
#FROM reg.daneshrefah.ir/modern-banking/gradle:8.3-jdk17-alpine AS build
#
## Set the working directory
#WORKDIR /app
#
## Copy the Gradle wrapper files
##COPY ../gradlew ./
#COPY scm-config-server/build.gradle ./
#COPY scm-config-server/src ./src
#COPY buildSrc /app/buildSrc
#
## Copy the rest of the project files
#
## Build the application
#RUN gradle clean bootJar -no-daemon -x test

# Stage 2: Create the final image using OpenJDK
FROM reg.daneshrefah.ir/modern-banking/gradle:8.3-jdk17-alpine

# Install Git
RUN apk update && apk upgrade
RUN apk add git

RUN apk add --no-cache \
    ttf-dejavu \
    fontconfig

RUN apk add busybox-extras


RUN apk add --no-cache curl