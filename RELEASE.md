# Release Instructions

### LSP4Jakarta is built by Jenkins from a github repository. The following steps will publish a new jar to the Eclipse repository.

1. Before running the release script we need to patch two pom files. The others will be patched by the release script. Clone the lsp4jakarta repository and edit the files `jakarta.ls/pom.xml` and `jakarta.eclipse/org.eclipse.lsp4jakarta.lsp4e.core/pom.xml`.
1. for jakarta.ls/pom.xml remove "-SNAPSHOT" around line 67:
    ```
    <dependency>
        <groupId>org.eclipse.lsp4jakarta</groupId>
        <artifactId>org.eclipse.lsp4jakarta.jdt.core</artifactId>
    -   <version>0.2.0-SNAPSHOT</version>
    +   <version>0.2.0</version>
    </dependency>
    ```
1. for jakarta.eclipse/org.eclipse.lsp4jakarta.lsp4e.core/pom.xml remove "-SNAPSHOT" around line 45:
    ```
    <artifactItem>
        <groupId>org.eclipse.lsp4jakarta</groupId>
        <artifactId>org.eclipse.lsp4jakarta.ls</artifactId>
    -   <version>0.2.0-SNAPSHOT</version>
    +   <version>0.2.0</version>
        <classifier>jar-with-dependencies</classifier>
    </artifactItem>
    ```
1. Commit these changes to a branch (e.g. `patch-0.2.4`) and push to the github repository (not a fork)
1. In a browser open https://ci.eclipse.org/lsp4jakarta/, click on `log in` and authenticate using an Eclipse.org id. This brings you to the Dashboard.
1. Click on the flow `LSP4Jakarta Release` 
1. To check the git branch: Click on `Configure` and scroll down to find `Branch Specifier`. Ensure it reads `main` to build the latest LSP4Jakarta code.
1. Click Back and then `Build with Parameters`.
1. For version type the version number to use as the release number e.g. `0.2.3`
1. For Version_Snapshot type the name of the next version snapshot build e.g. `0.2.4.SNAPSHOT`
1. Click `Build`. Jenkins will update the source files and commit them to git. Jenkins will also publish the JDT plugin and language server jars to the Eclipse repository.
1. Announce the new release on github in the following way. Navigate to https://github.com/eclipse-lsp4jakarta/lsp4jakarta and click on Releases
1. Click on `Draft a new release`
1. Type the title and release notes and style them according to the precedents set in previous releases. Click `Publish release`

If there is a serious error there is a help desk: https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues

