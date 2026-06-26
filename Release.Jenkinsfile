pipeline {
  agent any
  tools {
    jdk 'temurin-jdk17-latest'
  }
  environment {
    MAVEN_HOME = "$WORKSPACE/.m2/"
    MAVEN_USER_HOME = "$MAVEN_HOME"
  }
  parameters {
      string(name: 'VERSION', defaultValue: '', description: 'Version to Release?')
      string(name: 'VERSION_SNAPSHOT', defaultValue: '', description: 'Next Development Version?')
  }
  stages {
    stage("Release LSP4Jakarta Language Server"){
      steps {
        script {
          if (!params.VERSION) {
            error('Not releasing')
          }
        }
        withMaven {
          sh "VERSION=${params.VERSION}"
          sh '''
                cd jakarta.jdt
                ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:4.0.10:set-version -DnewVersion=$VERSION
                ./mvnw versions:set-scm-tag -DnewTag=$VERSION
                ./mvnw clean deploy -B -Peclipse-sign -Dcbi.jarsigner.skip=false
                cd ../jakarta.ls
                ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:4.0.10:set-version -DnewVersion=$VERSION
                ./mvnw versions:set-scm-tag -DnewTag=$VERSION
                ./mvnw clean deploy -B -Peclipse-sign -Dcbi.jarsigner.skip=false
                cd ../jakarta.eclipse
                ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:4.0.10:set-version -DnewVersion=$VERSION
                cd ..
              '''
        }
      }
    }

    stage('Deploy to downloads.eclipse.org') {
      steps {
        sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
          sh "VERSION=${params.VERSION}"
          sh '''
            targetDir=/home/data/httpd/download.eclipse.org/lsp4jakarta/releases/$VERSION
            ssh genie.lsp4jakarta@projects-storage.eclipse.org rm -rf $targetDir
            ssh genie.lsp4jakarta@projects-storage.eclipse.org mkdir -p $targetDir
            scp -r jakarta.jdt/org.eclipse.lsp4jakarta.jdt.site/target/*.zip genie.lsp4jakarta@projects-storage.eclipse.org:$targetDir
            ssh genie.lsp4jakarta@projects-storage.eclipse.org unzip $targetDir/*.zip -d $targetDir/repository
            '''
        }
        //Push release to latest
        sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
          sh "VERSION=latest"
          sh '''
            targetDir=/home/data/httpd/download.eclipse.org/lsp4jakarta/releases/$VERSION
            ssh genie.lsp4jakarta@projects-storage.eclipse.org rm -rf $targetDir
            ssh genie.lsp4jakarta@projects-storage.eclipse.org mkdir -p $targetDir
            scp -r jakarta.jdt/org.eclipse.lsp4jakarta.jdt.site/target/*.zip genie.lsp4jakarta@projects-storage.eclipse.org:$targetDir
            ssh genie.lsp4jakarta@projects-storage.eclipse.org unzip $targetDir/*.zip -d $targetDir/repository
            '''
        }
      }
    }

    stage('Push tag to git') {
      steps {
        sshagent(['github-bot-ssh']) {
          sh "VERSION=${params.VERSION}"
          sh '''
            git config --global user.email "lsp4jakarta-bot@eclipse.org"
            git config --global user.name "LSP4Jakarta GitHub Bot"
            git add "**/pom.xml" "**/MANIFEST.MF" "**/feature.xml"
            git commit -sm "Release $VERSION"
            git tag $VERSION
            git push origin $VERSION
          '''
        }
      }
    }

    stage("Update to next development version"){
      steps {
        withMaven {
          sh "$VERSION_SNAPSHOT=${params.VERSION_SNAPSHOT}"
          sh '''
            cd jakarta.jdt
            ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:4.0.10:set-version -DnewVersion=$VERSION_SNAPSHOT
            ./mvnw versions:set-scm-tag -DnewTag=$VERSION_SNAPSHOT
            cd ../jakarta.ls
            ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:4.0.10:set-version -DnewVersion=$VERSION_SNAPSHOT
            ./mvnw versions:set-scm-tag -DnewTag=$VERSION_SNAPSHOT
            cd ../jakarta.eclipse
            ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:4.0.10:set-version -DnewVersion=$VERSION_SNAPSHOT
            cd ..
          '''
        }
      }
    }

    stage('Push next development version') {
      steps {
        sshagent(['github-bot-ssh']) {
          sh "$VERSION_SNAPSHOT=${params.$VERSION_SNAPSHOT}"
          sh '''
            git config --global user.email "lsp4jakarta-bot@eclipse.org"
            git config --global user.name "LSP4Jakarta GitHub Bot"
            git add "**/pom.xml" "**/MANIFEST.MF" "**/feature.xml"
            git commit -sm "New Development $VERSION_SNAPSHOT"
            git push origin
          '''
        }
      }
    }
  }
}
