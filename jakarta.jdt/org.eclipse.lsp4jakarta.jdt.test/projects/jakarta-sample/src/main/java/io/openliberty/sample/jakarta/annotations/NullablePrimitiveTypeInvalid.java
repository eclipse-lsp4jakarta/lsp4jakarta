package io.openliberty.sample.jakarta.annotations;

import jakarta.annotation.Nullable;

public class NullablePrimitiveTypeInvalid {

    // Primitive fields (8 types)
    @Nullable
    private boolean booleanField;

    @Nullable
    private byte byteField;

    @Nullable
    private char charField;

    @Nullable
    private short shortField;

    @Nullable
    private int intField;

    @Nullable
    private long longField;

    @Nullable
    private float floatField;

    @Nullable
    private double doubleField;

    // Primitive / void return methods (8 types + void)
    @Nullable
    public boolean getBooleanField() {
        return false;
    }

    @Nullable
    public byte getByteField() {
        return 0;
    }

    @Nullable
    public char getCharField() {
        return 'a';
    }

    @Nullable
    public short getShortField() {
        return 0;
    }

    @Nullable
    public int getIntField() {
        return 0;
    }

    @Nullable
    public long getLongField() {
        return 0L;
    }

    @Nullable
    public float getFloatField() {
        return 0.0f;
    }

    @Nullable
    public double getDoubleField() {
        return 0.0;
    }

    @Nullable
    public void getVoidMethod() {
    }

    // Primitive method parameters (8 types)
    public void setPrimitiveParams(
            @Nullable boolean pBoolean,
            @Nullable byte pByte,
            @Nullable char pChar,
            @Nullable short pShort,
            @Nullable int pInt,
            @Nullable long pLong,
            @Nullable float pFloat,
            @Nullable double pDouble) {
    }
}
