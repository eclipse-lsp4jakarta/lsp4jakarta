package org.eclipse.lsp4jakarta.version;

import java.util.Arrays;

public enum JakartaVersion {

    EE_11(11, "Jakarta EE 11"),
    EE_10(10, "Jakarta EE 10"),
    EE_9(9, "Jakarta EE 9 / 9.1"),
    EE_8(8, "Jakarta EE 8 / 8"),
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

    /**
     * Finds the JakartaVersion enum constant by its label.
     *
     * @param label the label to search for (e.g., "Jakarta EE 11")
     * @return the matching JakartaVersion, or UNKNOWN if not found
     */
    public static JakartaVersion fromLabel(String label) {
        if (label == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values()).filter(v -> v.label.equals(label)).findFirst().orElse(UNKNOWN);
    }

    /**
     * Finds the JakartaVersion enum constant by its level.
     *
     * @param level the level to search for (e.g., 11)
     * @return the matching JakartaVersion, or UNKNOWN if not found
     */
    public static JakartaVersion fromLevel(int level) {
        return Arrays.stream(values()).filter(v -> v.level == level).findFirst().orElse(UNKNOWN);
    }
}
