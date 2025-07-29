package io.openliberty.sample.jakarta.validation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public class ValidConstraints {
    @AssertTrue
    private boolean isHappy;

    @AssertFalse
    private boolean isSad;

    @DecimalMax("30.0")
    @DecimalMin("10.0")
    private BigDecimal bigDecimal;
    
    @Digits(integer=5, fraction=1)
    private BigInteger digies;
    
    @Email
    private String emailAddress;
    
    @FutureOrPresent
    private Calendar graduationDate;
    
    @Future
    private Year fergiesYear;

    @Min(value = 50)
    @Max(value = 100)
    private Integer gpa;
    
    @Negative
    private int subZero;
    
    @NegativeOrZero
    private double notPos;
    
    @NotBlank
    private String saysomething;
   
//    not yet implemented - see issue #63
//    @NotEmpty
//    private String imgivinguponyou;
    
    @NotNull
    private String thisIsUsed;
    
    @Null
    private String NeverUsed;

    @Past
    private OffsetDateTime theGoodOldDays;
    
    @PastOrPresent
    private YearMonth aGoodFieldName;
    
    @Positive
    private int area;
    
    @PositiveOrZero
    private int maybeZero;

//    not yet implemented - see issue #63
//    @Size
//    private boolean wordMap;
}