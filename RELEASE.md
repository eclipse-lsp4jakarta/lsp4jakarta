### Release Instructions ###

LSP4Jakarta is built by Jenkins from a github repository. The following steps will publish a new jar to the Eclipse repository.

- In a browser open https://ci.eclipse.org/lsp4jakarta/, click on `log in` and authenticate. This brings you to the Dashboard.
- Click on the flow `LSP4Jakarta Release` 
- Check the git branch. Click on `Configure` and scroll down to find `Branch Specifier`. Ensure it reads `main` to build the latest LSP4Jakarta code.
- Click Back and then `Build with Parameters`.
- For version type the version number to use as the release number e.g. 0.2.3
- For Version_Snapshot type the name of the next version snapshot build e.g. 0.2.4-SNAPSHOT
- Click `Build`. Jenkins will update the source files and commit them to git. Jenkins will also publish the language server jar to the Eclipse repository.
- Announce the new release on github. Navigate to https://github.com/eclipse-lsp4jakarta/lsp4jakarta and click on Releases
- Click on `Draft a new release`
- Type the title and release notes and click `Publish release`


