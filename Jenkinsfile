pipeline {
 agent any
 stages {
   stage('build') {
     steps {
       sh './gradlew build'
     }
   }
   stage('build docker') {
     agent {
         docker {
            image 'openjdk:8'
            args '-v $HOME/.gradle'
         }
     }
     steps {
       sh 'docker build -t gcr.io/centering-rex-212817/usermanagerment:v1 .'
     }
   }
   stage('push image to gcp') {

   agent {
        docker {
                 image 'google/cloud-sdk'
                 args '-u 0:0 -v $HOME/.config -e'
               }
   }
     steps {
             withCredentials([file(credentialsId: "CREDS", variable: 'deployKey')]) {
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