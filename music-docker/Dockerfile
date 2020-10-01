FROM openjdk:13-alpine
EXPOSE 8080
RUN apk add --no-cache ffmpeg
ADD /target/music-api.jar demo.jar
ENTRYPOINT ["java","-jar","demo.jar"]
