# build LSP4Jakarta JDT Extension
cd jakarta.jdt && mvn clean install && cd ..

# build LSP4Jakarta LS
cd jakarta.ls && mvn clean install && cd ..

# build LSP4Jakarta Eclipse plugin
cd jakarta.eclipse && mvn clean install