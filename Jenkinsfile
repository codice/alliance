//"Jenkins Pipeline is a suite of plugins which supports implementing and integrating continuous delivery pipelines into Jenkins. Pipeline provides an extensible set of tools for modeling delivery pipelines "as code" via the Pipeline DSL."
//More information can be found on the Jenkins Documentation page https://jenkins.io/doc/

@Library('github.com/connexta/github-utils-shared-library@master') _

pipeline {
    agent {
        node {
            label 'linux-large-alliance'
            customWorkspace "/jenkins/workspace/${JOB_NAME}/${BUILD_NUMBER}"
        }
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout()
        disableResume()
    }
    triggers {
        /*
          Restrict nightly builds to master branch, all others will be built on change only.
          Note: The BRANCH_NAME will only work with a multi-branch job using the github-branch-source
        */
        cron(BRANCH_NAME == "master" ? "H H(19-21) * * *" : "")
    }
    environment {
        DOCS = 'distribution/docs'
        ITESTS = 'distribution/test/itests/test-itests-alliance'
        LARGE_MVN_OPTS = '-Xmx8192M -Xss128M -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC '
        DISABLE_DOWNLOAD_PROGRESS_OPTS = '-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn '
        LINUX_MVN_RANDOM = '-Djava.security.egd=file:/dev/./urandom'
        COVERAGE_EXCLUSIONS = '**/test/**/*,**/itests/**/*,**/*Test*,**/sdk/**/*,**/*.js,**/node_modules/**/*,**/jaxb/**/*,**/wsdl/**/*,**/nces/sws/**/*,**/*.adoc,**/*.txt,**/*.xml'
        GITHUB_USERNAME = 'codice'
        GITHUB_TOKEN = credentials('cxbot')
        GITHUB_REPONAME = 'alliance'
    }
    stages {
        stage('Setup') {

            steps {
                slackSend color: 'good', message: "STARTED: ${JOB_NAME} ${BUILD_NUMBER} ${BUILD_URL}"
                 postCommentIfPR("Internal build has been started, your results will be available at build completion.", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${GITHUB_TOKEN}")
            }
        }
        // Checkout the repository
        stage('Checkout repo') {
            steps {
                retry(3) {
                    checkout scm
                }
            }
        }
        // The incremental build will be triggered only for PRs. It will build the differences between the PR and the target branch
        stage('Incremental Build') {
            when {
                allOf {
                    expression { env.CHANGE_ID != null }
                    expression { env.CHANGE_TARGET != null }
                }
            }
            options {
                timeout(time: 1, unit: 'HOURS')
            }
            steps {
                // TODO: Maven downgraded to work around a linux build issue. Falling back to system java to work around a linux build issue. re-investigate upgrading later
                withMaven(maven: 'maven-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LARGE_MVN_OPTS} ${LINUX_MVN_RANDOM}', options: [artifactsPublisher(disabled: true), dependenciesFingerprintPublisher(disabled: true, includeScopeCompile: false, includeScopeProvided: false, includeScopeRuntime: false, includeSnapshotVersions: false)]) {
                    sh '''
                        unset JAVA_TOOL_OPTIONS
                        mvn install -B -DskipStatic=true -DskipTests=true $DISABLE_DOWNLOAD_PROGRESS_OPTS
                    '''
                    
                    sh '''
                        unset JAVA_TOOL_OPTIONS
                        mvn clean install -B -pl !$ITESTS -Dgib.enabled=true -Dgib.referenceBranch=/refs/remotes/origin/$CHANGE_TARGET $DISABLE_DOWNLOAD_PROGRESS_OPTS
                    '''
                    
                    sh '''
                        unset JAVA_TOOL_OPTIONS
                        mvn install -B -pl $ITESTS -nsu $DISABLE_DOWNLOAD_PROGRESS_OPTS
                    '''
                }
            }
        }
        // The full build will be run against all regular branches
        stage('Full Build') {
            when { 
                expression { env.CHANGE_ID == null } 
            }
            options {
                timeout(time: 1, unit: 'HOURS')
            }
            steps {
                // TODO: Maven downgraded to work around a linux build issue. Falling back to system java to work around a linux build issue. re-investigate upgrading later
                withMaven(maven: 'maven-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LARGE_MVN_OPTS} ${LINUX_MVN_RANDOM}') {
                    sh '''
                        unset JAVA_TOOL_OPTIONS
                        mvn clean install -B -pl !$ITESTS $DISABLE_DOWNLOAD_PROGRESS_OPTS
                    '''
                    
                    sh '''
                        unset JAVA_TOOL_OPTIONS
                        mvn install -B -pl $ITESTS -nsu $DISABLE_DOWNLOAD_PROGRESS_OPTS
                    '''
                }
            }
        }

        stage('Codecov.io upload') {
            when {
                expression {env.CHANGE_ID == null}
            }
            environment {
                CODECOV_TOKEN = credentials('Alliance_CodeCov')
            }
            steps {
                sh 'curl -s https://codecov.io/bash | bash -s - -t ${CODECOV_TOKEN}'
            }
        }

        stage('Dependency Check') {
            steps {
                withMaven(maven: 'maven-latest', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LARGE_MVN_OPTS} ${LINUX_MVN_RANDOM}') {
                    script {
                        // If this build is not a pull request, run full owasp scan. Otherwise run incremental scan
                        if (env.CHANGE_ID == null) {
                            sh 'mvn org.commonjava.maven.plugins:directory-maven-plugin:highest-basedir@directories dependency-check:check dependency-check:aggregate -q -B -Powasp -DskipTests=true -DskipStatic=true -pl !$DOCS $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                        } else {
                            sh 'mvn org.commonjava.maven.plugins:directory-maven-plugin:highest-basedir@directories dependency-check:check -q -B -Powasp -DskipTests=true -DskipStatic=true -pl !$DOCS -Dgib.enabled=true -Dgib.referenceBranch=/refs/remotes/origin/$CHANGE_TARGET $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                        }
                    }
                }
            }
        }

        stage ('SonarCloud') {
            when {
                // Currently Sonar is not run for pull requests
                expression { env.CHANGE_ID == null }
            }
            environment {
                SONARQUBE_GITHUB_TOKEN = credentials('SonarQubeGithubToken')
                SONAR_TOKEN = credentials('sonarqube-token')
            }
            steps {
                withMaven(maven: 'maven-latest', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LARGE_MVN_OPTS} ${LINUX_MVN_RANDOM}') {
                            sh 'mvn -q -B -Dcheckstyle.skip=true org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN  -Dsonar.organization=codice -Dsonar.projectKey=org.codice:alliance -Dsonar.exclusions=${COVERAGE_EXCLUSIONS} -pl !$DOCS,!$ITESTS $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                }
            }
        }
        
        /*
          Deploy stage will only be executed for deployable branches. These include master and any patch branch matching M.m.x format (i.e. 2.10.x, 2.9.x, etc...).
          It will also only deploy in the presence of an environment variable JENKINS_ENV = 'prod'. This can be passed in globally from the jenkins master node settings.
        */
        stage('Deploy') {
            when {
              allOf {
                expression { env.CHANGE_ID == null }
                expression { env.BRANCH_NAME ==~ /((?:\d*\.)?\d.x|master)/ }
                environment name: 'JENKINS_ENV', value: 'prod'
              }
            }
            steps {
                withMaven(maven: 'maven-latest', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LINUX_MVN_RANDOM}') {
                    sh 'mvn deploy -B -DskipStatic=true -DskipTests=true -DretryFailedDeploymentCount=10 $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                }
            }
        }
    }

    post {
        always{
            postCommentIfPR("Build ${currentBuild.currentResult} See the job results in [legacy Jenkins UI](${BUILD_URL}) or in [Blue Ocean UI](${BUILD_URL}display/redirect).", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${GITHUB_TOKEN}")
        }
        success {
            slackSend color: 'good', message: "SUCCESS: ${JOB_NAME} ${BUILD_NUMBER}"
        }
        failure {
            slackSend color: '#ea0017', message: "FAILURE: ${JOB_NAME} ${BUILD_NUMBER}. See the results here: ${BUILD_URL}"
        }
        unstable {
            slackSend color: '#ffb600', message: "UNSTABLE: ${JOB_NAME} ${BUILD_NUMBER}. See the results here: ${BUILD_URL}"
        }
        aborted {
            slackSend color: '#909090', message: "ABORTED: ${JOB_NAME} ${BUILD_NUMBER}. See the results here: ${BUILD_URL}"
        }
        cleanup {
            catchError(buildResult: null, stageResult: 'FAILURE', message: 'Cleanup Failure') {
                echo '...Cleaning up workspace'
                cleanWs()
                sh 'rm -rf ~/.m2/repository'
                wrap([$class: 'MesosSingleUseSlave']) {
                    sh 'echo "...Shutting down single-use slave: `hostname`"'
                }
            }
        }
    }
}
