pipeline {
 agent any
 stages {
    stage('Unit Test') {
      steps {
        sh './gradlew test'
      }
    }
   stage('build') {
     steps {
       sh './gradlew assemble'
     }
   }
   stage('build docker') {
     steps {
       sh 'docker build -t gcr.io/centering-rex-212817/usermanagerment:v1 .'
     }
   }
   stage('push image to gcp') {

   agent {
        docker {
                 image 'google/cloud-sdk'
                 args '-u 0:0 -v $HOME/.config -e HTTPS_PROXY=thd-svr-proxy-qa.homedepot.com:9090'
               }
   }
     steps {
             withCredentials([file(credentialsId: "CREDS", variable: 'deployKey')]) {
                   sh "gcloud config set proxy/type http"
                   sh "gcloud config set proxy/address thd-svr-proxy-qa.homedepot.com"
                   sh "gcloud config set proxy/port 7070"

                   //GCP Props.
                   sh "gcloud config set project centering-rex-212817"
                   sh "gcloud config set compute/zone us-east1-b"
                   sh "gcloud beta container clusters get-credentials cluster-1 --zone us-east1-b --project centering-rex-212817"




                   // Authenticate
                   sh "gcloud auth activate-service-account --key-file ${deployKey}"
                   sh 'gcloud docker -- push gcr.io/centering-rex-212817/usermanagerment:v1'
                   sh 'kubectl apply -f ./deployment.yml'
             }

     }
   }
 }
}