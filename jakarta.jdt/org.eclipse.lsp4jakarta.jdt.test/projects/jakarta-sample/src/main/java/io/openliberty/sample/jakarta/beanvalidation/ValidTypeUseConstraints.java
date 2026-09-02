package io.openliberty.sample.jakarta.beanvalidation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.*;

/**
 * Test resource for valid TYPE_USE constraint annotations — no diagnostics expected.
 * Each field has a constraint on a generic type argument that IS compatible with the constraint.
 */
public class ValidTypeUseConstraints {

    // @AssertTrue / @AssertFalse on Boolean type arg — valid
    private List<@AssertTrue Boolean> booleanFlags;
    private List<@AssertFalse Boolean> negatedFlags;

    // @DecimalMax / @DecimalMin / @Digits on BigDecimal type arg — valid
    private List<@DecimalMax("100") BigDecimal> cappedValues;
    private List<@DecimalMin("0") BigDecimal> flooredValues;
    private List<@Digits(integer = 5, fraction = 2) BigDecimal> preciseValues;

    // @Email / @NotBlank / @Pattern on String type arg — valid
    private List<@Email String> emails;
    private List<@NotBlank String> nonBlankStrings;
    private Map<String, @Pattern(regexp = "\\d+") String> codeMap;

    // @Future / @Past on LocalDate type arg — valid
    private List<@Future LocalDate> futureDates;
    private List<@Past LocalDate> pastDates;
    private List<@FutureOrPresent LocalDate> futureOrPresentDates;
    private List<@PastOrPresent LocalDate> pastOrPresentDates;

    // @Min / @Max on Integer type arg — valid
    private List<@Min(0) Integer> nonNegativeIntegers;
    private List<@Max(100) Integer> cappedIntegers;

    // @Negative / @Positive on Integer type arg — valid
    private List<@Negative Integer> negativeIntegers;
    private List<@Positive Integer> positiveIntegers;
    private List<@NegativeOrZero Integer> nonPositiveIntegers;
    private List<@PositiveOrZero Integer> nonNegativeOrZero;

    // @Size / @NotEmpty on List type arg — valid (Collection is allowed)
    private Map<String, @Size List<String>> sizedLists;
    private Map<String, @NotEmpty List<String>> nonEmptyLists;
}
