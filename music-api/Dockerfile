FROM openjdk:13-jdk-buster
EXPOSE 8080
ADD /target/music-api.jar demo.jar
RUN apt update
RUN apt-get --yes install ffmpeg
ENTRYPOINT ["java","-jar","demo.jar"]
