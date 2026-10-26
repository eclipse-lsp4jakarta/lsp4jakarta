package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Test class with valid @Id types that should NOT trigger diagnostics
 */
@Entity
public class ValidIdTypes {

    // Valid: Primitive types
    @Id
    private int intId;

    @Id
    private long longId;

    @Id
    private short shortId;

    @Id
    private byte byteId;

    @Id
    private char charId;

    @Id
    private boolean booleanId;

    @Id
    private float floatId;

    @Id
    private double doubleId;

    // Valid: Wrapper types
    @Id
    private Integer integerId;

    @Id
    private Long longWrapperId;

    @Id
    private Short shortWrapperId;

    @Id
    private Byte byteWrapperId;

    @Id
    private Character characterId;

    @Id
    private Boolean booleanWrapperId;

    @Id
    private Float floatWrapperId;

    @Id
    private Double doubleWrapperId;

    // Valid: String
    @Id
    private String stringId;

    // Valid: Date types
    @Id
    @Temporal(TemporalType.DATE)
    private Date utilDateId;

    @Id
    private java.sql.Date sqlDateId;

    // Valid: BigDecimal and BigInteger
    @Id
    private BigDecimal bigDecimalId;

    @Id
    private BigInteger bigIntegerId;

    // Valid: Getters with valid return types
    @Id
    public Long getMethodLongId() {
        return 1L;
    }

    @Id
    public String getMethodStringId() {
        return "id";
    }

    @Id
    public BigInteger getMethodBigIntegerId() {
        return BigInteger.ONE;
    }

    @Id
    @Temporal(TemporalType.DATE)
    public java.util.Date getMethodDateId() {
        return new Date();
    }
}

