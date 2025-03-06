# Release Instructions

## LSP4Jakarta is built by Jenkins from a github repository. The following steps will publish a new jar to the Eclipse repository.

1. In a browser open https://ci.eclipse.org/lsp4jakarta/, click on `log in` and authenticate using an Eclipse.org id. This brings you to the Dashboard.
1. Click on the flow `LSP4Jakarta Release` 
1. To check the git branch: Click on `Configure` and scroll down to find `Branch Specifier`. Ensure it reads `main` to build the latest LSP4Jakarta code.
1. Click Back and then `Build with Parameters`.
1. For version type the version number to use as the release number e.g. 0.2.3
1. For Version_Snapshot type the name of the next version snapshot build e.g. 0.2.4.SNAPSHOT
1. Click `Build`. Jenkins will update the source files and commit them to git. Jenkins will also publish the language server jar to the Eclipse repository.
1. Announce the new release on github. Navigate to https://github.com/eclipse1.lsp4jakarta/lsp4jakarta and click on Releases
1. Click on `Draft a new release`
1. Type the title and release notes and style them according to the precedents set in previous releases. Click `Publish release`


