# Release Instructions

### LSP4Jakarta is built by Jenkins from a github repository. The following steps will publish new jars to the Eclipse Maven and p2 repositories.

1. Before running the release script we need to patch two pom files. The others will be patched by the release script. Clone the LSP4Jakarta repository and create a new branch from main with a name like e.g. `release-0.2.3`. Edit the files `jakarta.ls/pom.xml` and `jakarta.eclipse/org.eclipse.lsp4jakarta.lsp4e.core/pom.xml`.
1. for jakarta.ls/pom.xml remove "-SNAPSHOT" around line 67:
    ```
    <dependency>
        <groupId>org.eclipse.lsp4jakarta</groupId>
        <artifactId>org.eclipse.lsp4jakarta.jdt.core</artifactId>
    -   <version>0.2.3-SNAPSHOT</version>
    +   <version>0.2.3</version>
    </dependency>
    ```
1. for jakarta.eclipse/org.eclipse.lsp4jakarta.lsp4e.core/pom.xml remove "-SNAPSHOT" around line 45:
    ```
    <artifactItem>
        <groupId>org.eclipse.lsp4jakarta</groupId>
        <artifactId>org.eclipse.lsp4jakarta.ls</artifactId>
    -   <version>0.2.3-SNAPSHOT</version>
    +   <version>0.2.3</version>
        <classifier>jar-with-dependencies</classifier>
    </artifactItem>
    ```
1. Commit these changes to the branch and push to the LSP4Jakarta github repository (not a fork) e.g. `git push --set-upstream origin release-0.2.3` where `origin` refers to LSP4Jakarta.
1. In a browser open https://ci.eclipse.org/lsp4jakarta/, click on `log in` and authenticate using an Eclipse.org id. This brings you to the Dashboard.
1. Click on the flow `LSP4Jakarta Release` 
1. To check the git branch: Click on `Configure` and scroll down to find `Branch Specifier`. Type the name of the branch where you updated the poms e.g. `release-0.2.3` to build the latest LSP4Jakarta code.
1. Click Back and then `Build with Parameters`.
1. For parameter `VERSION` type the version number to use as the release number e.g. `0.2.3`
1. For parameter `VERSION_SNAPSHOT` type the name of the next version snapshot e.g. `0.2.4-SNAPSHOT`
1. Click `Build`. Jenkins will update the rest of the version numbers on the build machine but will *not* commit them to git. Jenkins will build and publish the JDT plugin and language server jars to the Eclipse repositories.
1. The Jenkins command will hang on the step `Push tag to git`. Click the small X on the current build in the left hand panel to cancel the rest of the build. Proceed with these steps to update the source with the current version number and then the snapshot version number for future development.
1. Next we need to check the version number source changes into git. We should still be on the branch `release-0.2.3`. Set up the environment variables `VERSION` and `VERSION_SNAPSHOT`. E.g. `export VERSION=0.2.3` and `export VERSION_SNAPSHOT=0.2.4-SNAPSHOT`. These values must be the same as used in the Jenkins build earlier. We will use `VERSION_SNAPSHOT` later.
1. Execute locally these commands copied from Release.Jenkinsfile that update the version numbers in the source. Do not execute the build commands. E.g.
    ```
        cd jakarta.jdt 
        ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION
        ./mvnw versions:set-scm-tag -DnewTag=$VERSION
    ```
1. For jakarta.ls 
    ```
        cd ../jakarta.ls
        ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION
        ./mvnw versions:set-scm-tag -DnewTag=$VERSION
    ```
1. Finally, for jakarta.eclipse
    ```
        cd ../jakarta.eclipse
        ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION
        cd ..
    ```
1. These changes should now be combined with the pom file changes performed above.
1. Commit the file changes with a comment like "Release 0.2.3". This commit will be the target of a tag in the next steps.
1. Save these changes to git on the branch e.g. `release-0.2.3`. Use this command: `git push origin` assuming you set the upstream as in the first time we pushed this branch. This branch will be used in the release process later so you will merge this branch but do not delete it.
1. Announce the new release on github in the following way. Navigate to https://github.com/eclipse-lsp4jakarta/lsp4jakarta and click on `Releases`
1. Click on `Draft a new release`
1. Click on `Choose a tag` and create a new tag with the name of the release.
1. Click on `Target: main` and select the release branch created above e.g. `release-0.2.3`
1. Type the title and release notes and style them according to the precedents set in previous releases. 
1. Select `Set as the latest release` and click `Publish release`
1. Next, to set up the code base for the next release we will change the version number to indicate the next snapshot. 
1. Create a new branch called `prepare-0.2.4`. Execute these commands as copied from Release.Jenkinsfile. These steps assume you still have `VERSION_SNAPSHOT` set from a previous step.
1. For jakarta.jdt
    ```
        cd jakarta.jdt
        ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION_SNAPSHOT
        ./mvnw versions:set-scm-tag -DnewTag=$VERSION_SNAPSHOT
    ````
1. For jakarta.ls
    ```
        cd ../jakarta.ls
        ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION_SNAPSHOT
        ./mvnw versions:set-scm-tag -DnewTag=$VERSION_SNAPSHOT
    ```
1. For jakarta.eclipse
    ```
        cd ../jakarta.eclipse
        ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION_SNAPSHOT
        cd ..
    ```
1. Manually edit the files `jakarta.ls/pom.xml` and `jakarta.eclipse/org.eclipse.lsp4jakarta.lsp4e.core/pom.xml` to use the new snapshot version as indicated at the beginning of these instructions.
1. Commit these changes with a comment like `New Development 0.2.4-SNAPSHOT`. You can merge and delete this branch.

If there is a serious error there is a help desk: https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues

