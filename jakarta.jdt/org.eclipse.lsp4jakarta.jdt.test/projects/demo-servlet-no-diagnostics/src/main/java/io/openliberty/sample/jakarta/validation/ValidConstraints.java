package io.openliberty.sample.jakarta.validation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Map;

import jakarta.fake.constraints.Email;
import jakarta.fake.constraints.Future;
import jakarta.fake.constraints.FutureOrPresent;
import jakarta.fake.constraints.Max;
import jakarta.fake.constraints.Min;
import jakarta.fake.constraints.Negative;
import jakarta.fake.constraints.NegativeOrZero;
import jakarta.fake.constraints.NotBlank;
import jakarta.fake.constraints.NotEmpty;
import jakarta.fake.constraints.NotNull;
import jakarta.fake.constraints.Null;
import jakarta.fake.constraints.Past;
import jakarta.fake.constraints.PastOrPresent;
import jakarta.fake.constraints.Positive;
import jakarta.fake.constraints.PositiveOrZero;
import jakarta.fake.constraints.Size;
import jakarta.fake.constraints.AssertFalse;
import jakarta.fake.constraints.AssertTrue;
import jakarta.fake.constraints.DecimalMax;
import jakarta.fake.constraints.DecimalMin;
import jakarta.fake.constraints.Digits;

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