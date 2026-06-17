set -e

# build LSP4Jakarta JDT Extension
cd jakarta.jdt && ./mvnw clean install -B && cd ..

# build LSP4Jakarta LS
cd jakarta.ls && ./mvnw clean install -B && cd ..

# build LSP4Jakarta Eclipse plugin
cd jakarta.eclipse && ./mvnw clean install -B && cd ..

