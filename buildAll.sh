set -e

# build LSP4Jakarta JDT Extension
cd jakarta.jdt && mvn clean install && cd ..
sleep 5

# build LSP4Jakarta LS
cd jakarta.ls && mvn clean install && cd ..
sleep 5

# build LSP4Jakarta Eclipse plugin
cd jakarta.eclipse && mvn clean install && cd ..

