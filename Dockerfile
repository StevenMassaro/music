FROM eclipse-temurin:18-jre
EXPOSE 8080
RUN apt-get update && apt-get install ffmpeg -y
ADD /music-api/target/music-api.jar music-api.jar
ENTRYPOINT ["java","-jar","music-api.jar"]
