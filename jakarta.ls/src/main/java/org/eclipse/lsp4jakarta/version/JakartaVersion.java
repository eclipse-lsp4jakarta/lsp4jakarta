package org.eclipse.lsp4jakarta.version;

public enum JakartaVersion {

    EE_11(11, "Jakarta EE 11"),
    EE_10(10, "Jakarta EE 10"),
    EE_9(9, "Jakarta EE 9 / 9.1"),
    UNKNOWN(0, "Unknown");

    private final int level;
    private final String label;

    JakartaVersion(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
