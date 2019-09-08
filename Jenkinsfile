node {
   def mvnHome
   stage('Preparation') { 
      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'http://192.168.0.117:8084/git/music/music.git']]])
      mvnHome = tool 'M3'
   }
   stage('Build') {
      // Run the maven build
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" -Dmaven.test.failure.ignore clean install'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" -Dmaven.test.failure.ignore clean install/)
         }
      }
   }
   stage('Results') {
      junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'music-api/target/*.war'
   }
   stage('Deploy') {
	   sh label: '', script: '''rm -rf /webapps/${JOB_BASE_NAME}.war
		while [ -d /webapps/${JOB_BASE_NAME} ]
		do
		  sleep 1
		  echo "${JOB_BASE_NAME} not removed yet, sleeping"
		done
		cp ${WORKSPACE}/music-api/target/${JOB_BASE_NAME}.war /webapps/${JOB_BASE_NAME}.war'''
   }
}