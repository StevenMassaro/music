node {
   def mvnHome
   properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')), disableConcurrentBuilds()])
   stage('Preparation') { 
      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'http://192.168.0.117:8084/git/music/music.git']]])
      mvnHome = tool 'M3'
   }
   stage('Build') {
      // Run the maven build
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" clean install'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" clean install/)
         }
      }
   }
   stage('Docker build') {
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" -Dmaven.test.failure.ignore -pl :music-api dockerfile:build'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" -Dmaven.test.failure.ignore -pl :music-api dockerfile:build/)
         }
      }
   }
   stage('Publish docker image') {
	   sh label: '', script: 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
       sh label: '', script: 'docker push stevenmassaro/music-api:latest'
   }
   stage('Results') {
      //junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'music-api/target/*.jar'
   }
}