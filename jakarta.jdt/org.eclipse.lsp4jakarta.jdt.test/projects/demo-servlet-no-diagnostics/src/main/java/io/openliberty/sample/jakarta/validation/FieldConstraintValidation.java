package io.openliberty.sample.jakarta.validation;

import java.util.Calendar;
import java.util.List;

import jakarta.fake.constraints.*;

public class FieldConstraintValidation {

    @AssertTrue
    private int isHappy;                    // invalid types

    @AssertFalse
    private Double isSad;

    @DecimalMax("30.0")
    @DecimalMin("10.0")
    private String bigDecimal;

    @Digits(fraction = 0, integer = 0)
    private boolean digits;

    @Email
    private Integer emailAddress;

    @FutureOrPresent
    private boolean graduationDate;

    @Future
    private double fergiesYear;

    @Min(value = 50)
    @Max(value = 100)
    private boolean gpa;

    @Negative
    private boolean subZero;

    @NegativeOrZero
    private String notPos;

    @NotBlank
    private boolean saysomething;

    @Pattern(regexp = "")
    private Calendar thisIsUsed;

    @Past
    private double theGoodOldDays;

    @PastOrPresent
    private char[] aGoodFieldName;

    @Positive
    private String[] area;

    @PositiveOrZero
    private List<String> maybeZero;

    @AssertTrue
    private static boolean typeValid;       // static

    @Past
    private static boolean doubleBad;      // static and invalid type
}