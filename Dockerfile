FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080
RUN apk add --no-cache --update ffmpeg
ADD /music-api/target/music-api.jar music-api.jar
ENTRYPOINT ["java","-jar","music-api.jar"]
