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
FROM dockerhub.daneshrefah.ir/drp/scm-base
# Install Git
RUN apt-get update && \
    apt-get install -y git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


# Set environment variables
ENV JAVA_OPTS="-Xms4096m -Xmx8192m"

EXPOSE 80 5701 8888 8080 8000

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY scm-web-8.0.0.jar scm-web-8.0.0.jar
#COPY scm-web-boot.jar scm-console-boot.jar
COPY scm-cache-8.0.0.jar scm-cache-8.0.0.jar
COPY scm-uaa-8.0.0.jar scm-uaa-8.0.0.jar
COPY scm-config-server-8.0.0.jar scm-config-server-8.0.0.jar

# Expose the port the application runs on
#EXPOSE 80

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/$SCM_APP-8.0.0.jar --sever.port=$SERVER_PORT"]
