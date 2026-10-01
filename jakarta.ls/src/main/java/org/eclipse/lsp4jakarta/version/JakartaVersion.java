package org.eclipse.lsp4jakarta.version;

import java.util.Arrays;

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
     * Finds the JakartaVersion enum constant by its level or string representation.
     *
     * @param value the level as string (e.g., "9", "10", "11") or label
     * @return the matching JakartaVersion, or EE_9 as fallback
     */
    public static JakartaVersion fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return EE_9;
        }
        String trimmed = value.trim();
        for (JakartaVersion v : values()) {
            if (String.valueOf(v.level).equals(trimmed) || v.label.equalsIgnoreCase(trimmed) || v.name().equalsIgnoreCase(trimmed)) {
                return v;
            }
        }
        return EE_9;
    }

}
