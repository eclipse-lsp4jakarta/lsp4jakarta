package io.openliberty.sample.jakarta.annotations;

import jakarta.annotation.Nullable;

public class NullablePrimitiveTypeValid {

    // Wrapper, String, and array fields
    @Nullable
    private Boolean booleanWrapperField;

    @Nullable
    private Byte byteWrapperField;

    @Nullable
    private Character charWrapperField;

    @Nullable
    private Short shortWrapperField;

    @Nullable
    private Integer intWrapperField;

    @Nullable
    private Long longWrapperField;

    @Nullable
    private Float floatWrapperField;

    @Nullable
    private Double doubleWrapperField;

    @Nullable
    private String stringField;

    @Nullable
    private int[] primitiveArrayField;

    // Wrapper, String, and array return methods
    @Nullable
    public Boolean getBooleanWrapperField() {
        return null;
    }

    @Nullable
    public Byte getByteWrapperField() {
        return null;
    }

    @Nullable
    public Character getCharWrapperField() {
        return null;
    }

    @Nullable
    public Short getShortWrapperField() {
        return null;
    }

    @Nullable
    public Integer getIntWrapperField() {
        return null;
    }

    @Nullable
    public Long getLongWrapperField() {
        return null;
    }

    @Nullable
    public Float getFloatWrapperField() {
        return null;
    }

    @Nullable
    public Double getDoubleWrapperField() {
        return null;
    }

    @Nullable
    public String getStringField() {
        return null;
    }

    @Nullable
    public int[] getPrimitiveArrayField() {
        return null;
    }

    // Wrapper, String, and array parameters
    public void setWrapperParams(
            @Nullable Boolean pBoolean,
            @Nullable Byte pByte,
            @Nullable Character pChar,
            @Nullable Short pShort,
            @Nullable Integer pInt,
            @Nullable Long pLong,
            @Nullable Float pFloat,
            @Nullable Double pDouble,
            @Nullable String pString,
            @Nullable int[] pArray) {
    }
}
