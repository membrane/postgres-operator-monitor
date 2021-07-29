@Library("p8-jenkins-utils@1.20")
final def p8 = new de.predic8.jenkinsutils.P8(this, scm, 'pg-op-mon')

pipeline {
    agent {
        kubernetes {
            yamlFile 'deploy/builder-pod.yaml'
        }
    }

    parameters {
        string(name: 'KUBECTL_OPTS', defaultValue: '', description: 'Kubernetes Cluster URL')
    }

    stages {
        stage('Build') {
            steps {
                sh 'sed -i "s/ARG VERSION/ENV VERSION=${BUILD_NUMBER}/" Dockerfile'
                container('buildah') {
                    script {
                        p8.maven.useInternalRepo()
                    }
                    sh 'mv ~/.m2/settings.xml .'
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
                sh 'sed -i "s/\\\$BUILD_NUMBER/$BUILD_NUMBER/" ./pg-op-mon.yaml'
                container('kubectl') {
                    script {
                        p8.kubectl.prepareCredentials()
                        p8.kubectl.applyV2()
                    }
                    sh 'kubectl $KUBECTL_OPTS -n monitoring rollout status deployment pg-op-mon'
                }
            }
        }
    }
}
