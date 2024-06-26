FROM alpine:3.20
EXPOSE 8080
RUN apk add --no-cache --update ffmpeg openjdk17-jre
ADD /music-api/target/music-api.jar music-api.jar
ENTRYPOINT ["java","-jar","music-api.jar"]
