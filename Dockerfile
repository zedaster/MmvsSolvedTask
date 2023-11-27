# Можно компилировать при сброке образа и потом его выполнять в Docker
# FROM gradle:8-jdk17-alpine AS build
# COPY --chown=gradle:gradle . /home/gradle/src
# WORKDIR /home/gradle/src
# RUN gradle build --no-daemon

# FROM eclipse-temurin:17-jre
# EXPOSE 8080
# RUN mkdir /app
# COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
# ENTRYPOINT ["java", "-jar", "/app/spring-boot-application.jar"]

# Или брать локальную сборку jar и накатывать ее с FFMpeg на JRE
FROM eclipse-temurin:17-jre
EXPOSE 8080
RUN mkdir /app
COPY /build/libs/*.jar /app/spring-boot-application.jar

RUN apt-get update && apt-get install -y ffmpeg
ENV FF_MPEG_PATH=/usr/bin/ffmpeg
ENV FF_PROBE_PATH=/usr/bin/ffprobe
ENV STORAGE_PATH=/home/storage
VOLUME $STORAGE_PATH

ENTRYPOINT ["java", "-jar", "/app/spring-boot-application.jar"]