# This script is used for syncing up jakarta-sample and demo-servlet-no-diagnostics project automatically. 
# It should be run at the root directory of lsp4jakarta before code check-in.
# This can also be called first in buildAll.sh which will be run anyway before check-in.
#!/bin/bash
set -e

BASE_DIR="jakarta.jdt/org.eclipse.lsp4jakarta.jdt.test/projects"
SRC_PRJ="jakarta-sample"
DEST_PRJ="demo-servlet-no-diagnostics"
PRJ_DIR="src/main/java/io/openliberty/sample/jakarta"

DEFAULT_SRC_DIR="$BASE_DIR/$SRC_PRJ/$PRJ_DIR"
DEFAULT_DEST_DIR="$BASE_DIR/$DEST_PRJ/$PRJ_DIR"

SRC_DIR="${1:-$DEFAULT_SRC_DIR}"
DEST_DIR="${2:-$DEFAULT_DEST_DIR}"

echo "Syncing files from $SRC_DIR to $DEST_DIR with fake imports..."

find "$SRC_DIR" -type f -name "*.java" | while IFS= read -r srcFile; do
  relPath="${srcFile#$SRC_DIR/}"
  destFile="$DEST_DIR/$relPath"

  mkdir -p "$(dirname "$destFile")"

  tmpSrc=$(mktemp)
  tmpDest=$(mktemp)

  # Normalize source file by stripping the first subpackage after jakarta, for comparison
  sed -E 's/import[[:space:]]+jakarta\.[^.]+\./import jakarta./g' "$srcFile" > "$tmpSrc"

  # Normalize destination file by stripping fake, for comparison
   if [ -f "$destFile" ]; then
  sed -E 's/import[[:space:]]+jakarta\.fake\./import jakarta./g' "$destFile" > "$tmpDest"
  fi

  # Compare normalized files
  if [ ! -f "$destFile" ] || ! cmp -s "$tmpSrc" "$tmpDest"; then
    echo "Updating $destFile"
    cp "$srcFile" "$destFile"
    # Imports are written as fake
    sed -E 's/^import[[:space:]]+jakarta\.[^.]+\./import jakarta.fake./g' "$destFile" > "$destFile.tmp" && mv "$destFile.tmp" "$destFile"
  else
    echo "Skipping $destFile (no changes except imports)"
  fi

  rm -f "$tmpSrc" "$tmpDest"
done
echo "Sync complete."