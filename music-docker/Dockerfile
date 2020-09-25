FROM openjdk:13-jdk-buster
EXPOSE 8080
RUN apt update
RUN apt-get --yes install ffmpeg
ADD /target/music-api.jar demo.jar
ENTRYPOINT ["java","-jar","demo.jar"]
