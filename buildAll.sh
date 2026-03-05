set -e

# build LSP4Jakarta JDT Extension
cd jakarta.jdt && mvn clean; mvn install && cd ..

# build LSP4Jakarta LS
cd jakarta.ls && mvn clean; mvn install && cd ..

# build LSP4Jakarta Eclipse plugin
cd jakarta.eclipse && mvn clean; mvn install && cd ..

