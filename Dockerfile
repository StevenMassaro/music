FROM openjdk:13-alpine
EXPOSE 8080
RUN apk add --no-cache ffmpeg
ADD /music-api/target/music-api.jar music-api.jar
ENTRYPOINT ["java","-jar","music-api.jar"]
