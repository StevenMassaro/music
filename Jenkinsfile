node {
   def mvnHome
   properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')), disableConcurrentBuilds()])
   stage('Preparation') {
      sh 'curl https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage -d chat_id=${TELEGRAM_CHAT_ID} -d text="${JOB_BASE_NAME} - ${BUILD_NUMBER} started" || true'
      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'GitBucketReadOnly', url: 'http://192.168.0.117:8084/git/music/music.git']]])
      mvnHome = tool 'M3'
   }
   stage('Build') {
      // Run the maven build
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" clean deploy'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" clean deploy/)
         }
      }
   }
   stage('Results') {
      //junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'music-api/target/*.jar'
      sh 'curl https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage -d chat_id=${TELEGRAM_CHAT_ID} -d text="${JOB_BASE_NAME} - ${BUILD_NUMBER} finished" || true'
   }
   stage('GitHub mirror') {
   		dir('gh'){
   		    cleanWs()
   		    def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()
   		    def mirrorUrl = "https://github.com/StevenMassaro/music.git"

   		    sh 'git config --global credential.helper cache'
            sh "git config --global credential.helper 'cache --timeout=3600'"
            git credentialsId: 'GitBucketReadOnly', url: scmUrl
            git credentialsId: 'GitHubStevenMassaro', url: mirrorUrl

			 sh "git clone --bare ${scmUrl}"
			 dir('music.git'){
				sh "git push --mirror ${mirrorUrl}"
			 }
   		}
	}
}