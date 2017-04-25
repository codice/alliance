//"Jenkins Pipeline is a suite of plugins which supports implementing and integrating continuous delivery pipelines into Jenkins. Pipeline provides an extensible set of tools for modeling delivery pipelines "as code" via the Pipeline DSL."
//More information can be found on the Jenkins Documentation page https://jenkins.io/doc/
pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr:'25'))
    }
    triggers {
        cron('H H(20-23) * * *')
    }
    stages {
        stage('Setup') {
            steps{
                slackSend color: 'good', message: "STARTED: ${JOB_NAME} ${BUILD_NUMBER} ${BUILD_URL}"
            }
        }
        stage('Parallel Build') {
            // TODO CAL-296 refactor this stage from scripted syntax to declarative syntax to match the rest of the stages - https://issues.jenkins-ci.org/browse/JENKINS-41334
            steps{
                parallel(
                    linux: {
                        node('linux-small') {
                            timeout(time: 60, unit: 'MINUTES') {
                                checkout scm
                                withMaven(maven: 'M3', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings') {
                                    sh 'mvn clean install -pl !distribution/test/itests/test-itests-alliance'
                                    sh 'mvn install -pl distribution/test/itests/test-itests-alliance -nsu'
                                }
                            }
                        }
                    }, windows: {
                        node('proxmox-windows'){
                            timeout(time: 60, unit: 'MINUTES') {
                                checkout scm
                                withMaven(maven: 'M3', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings') {
                                    bat 'mvn clean install -pl !distribution/test/itests/test-itests-alliance'
                                    bat 'mvn install -pl distribution/test/itests/test-itests-alliance -nsu'
                                }
                            }
                        }
                    }
                )
            }
        }
        stage('Deploy') {
            agent { label 'linux-small' }
            steps{
                withMaven(maven: 'M3', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice_settings.xml') {
                    checkout scm
                    sh 'mvn javadoc:aggregate -DskipStatic=true -DskipTests=true'
                    sh 'mvn deploy -DskipStatic=true -DskipTests=true'
                }
            }
        }
    }
    post {
        success {
            slackSend color: 'good', message: "SUCCESS: ${JOB_NAME} ${BUILD_NUMBER}"
        }
        failure {
            slackSend color: '#ea0017', message: "FAILURE: ${JOB_NAME} ${BUILD_NUMBER}. See the results here: ${BUILD_URL}"
        }
        unstable {
            slackSend color: '#ffb600', message: "UNSTABLE: ${JOB_NAME} ${BUILD_NUMBER}. See the results here: ${BUILD_URL}"
        }
    }
}