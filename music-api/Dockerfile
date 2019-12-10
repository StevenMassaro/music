FROM openjdk:13-alpine
EXPOSE 8080
ADD /target/music-api.jar demo.jar
RUN apk add --no-cache ffmpeg
ENTRYPOINT ["java","-jar","demo.jar"]
