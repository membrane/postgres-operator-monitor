@Library("p8-jenkins-utils@1.18")
final def p8 = new de.predic8.jenkinsutils.P8(this, scm, 'pg-op-mon')

pipeline {
    agent {
        kubernetes {
            yamlFile 'deploy/builder-pod.yaml'
        }
    }

    parameters {
        choice(name: 'INST', choices: ['test', 'prod'], description: '')
        string(name: 'KUBECTL_OPTS', defaultValue: '', description: 'Kubernetes Cluster URL')
    }

    stages {
        stage('Build') {
            steps {
                sh 'sed -i "s/ARG VERSION/ENV VERSION=${BUILD_NUMBER}/" Dockerfile'
                container('buildah') {
                    withCredentials([usernamePassword(credentialsId: '350ecaca-7d1f-4acf-9602-8791a4a866b6',
                            usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh '''
cat <<EOF >init.gradle
allprojects {
  repositories {
    mavenLocal()
    maven {
        credentials {
            username "$USERNAME"
            password "$PASSWORD"
        }
        url 'https://repository.membrane-soa.org/repository/internal'
    }
  }
}
EOF
                        '''
                    }
                    script {
                        p8.oci.repoLogin()
                        p8.oci.buildV2()
                    }
                }
            }
        }

        stage('Docker push') {
            steps {
                container('buildah') {
                    script {
                        p8.oci.pushV2()
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'cp deploy/pg-op-mon.yaml pg-op-mon.yaml'
                sh 'sed -i "s/\\\$INST/$INST/" ./pg-op-mon.yaml'
                sh 'sed -i "s/\\\$BUILD_NUMBER/$BUILD_NUMBER/" ./pg-op-mon.yaml'
                container('kubectl') {
                    script {
                        p8.kubectl.prepareCredentials()
                        p8.kubectl.applyV2()
                    }
                    sh 'kubectl $KUBECTL_OPTS -n pg-op-mon rollout status deployment pg-op-mon-$INST'
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}