package io.openliberty.sample.jakarta.beanvalidation;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.*;

/**
 * Test resource for TYPE_USE constraint validation diagnostics.
 * Each field exercises one constraint annotation placed on a generic type argument
 * where the type argument is incompatible with the constraint.
 */
public class TypeUseConstraintValidation {

    // line 14 — @AssertTrue on Integer (not boolean/Boolean) → error
    private List<@AssertTrue Integer> assertTrueOnInteger;

    // line 17 — @AssertFalse on String (not boolean/Boolean) → error
    private List<@AssertFalse String> assertFalseOnString;

    // line 20 — @DecimalMax on Boolean (not numeric/CharSequence) → error
    private List<@DecimalMax("10") Boolean> decimalMaxOnBoolean;

    // line 23 — @DecimalMin on Boolean → error
    private List<@DecimalMin("1") Boolean> decimalMinOnBoolean;

    // line 26 — @Digits on Boolean → error
    private List<@Digits(integer = 3, fraction = 0) Boolean> digitsOnBoolean;

    // line 29 — @Email on Integer (not String/CharSequence) → error
    private List<@Email Integer> emailOnInteger;

    // line 32 — @Future on String (not a date type) → error
    private List<@Future String> futureOnString;

    // line 35 — @FutureOrPresent on Integer → error
    private List<@FutureOrPresent Integer> futureOrPresentOnInteger;

    // line 38 — @Past on Boolean → error
    private List<@Past Boolean> pastOnBoolean;

    // line 41 — @PastOrPresent on String → error
    private List<@PastOrPresent String> pastOrPresentOnString;

    // line 44 — @Min on Boolean (not numeric) → error
    private List<@Min(1) Boolean> minOnBoolean;

    // line 47 — @Max on String → error
    private List<@Max(100) String> maxOnString;

    // line 50 — @Negative on Boolean → error
    private List<@Negative Boolean> negativeOnBoolean;

    // line 53 — @NegativeOrZero on String → error
    private List<@NegativeOrZero String> negativeOrZeroOnString;

    // line 56 — @Positive on Boolean → error
    private List<@Positive Boolean> positiveOnBoolean;

    // line 59 — @PositiveOrZero on String → error
    private List<@PositiveOrZero String> positiveOrZeroOnString;

    // line 62 — @NotBlank on Integer (not String/CharSequence) → error
    private List<@NotBlank Integer> notBlankOnInteger;

    // line 65 — @Pattern on Integer → error
    private List<@Pattern(regexp = ".*") Integer> patternOnInteger;

    // line 68 — @Size on Boolean (not CharSequence/Collection/Map/Array) → error
    private Map<String, @Size Boolean> sizeOnBoolean;

    // line 71 — @NotEmpty on Boolean → error
    private List<@NotEmpty Boolean> notEmptyOnBoolean;

    // line 74 — nested: inner type arg @Email on Integer → error on outer field
    private Map<String, List<@Email Integer>> nestedEmailOnInteger;

	public List<Integer> getAssertTrueOnInteger() {
		return assertTrueOnInteger;
	}

	public void setAssertTrueOnInteger(List<Integer> assertTrueOnInteger) {
		this.assertTrueOnInteger = assertTrueOnInteger;
	}

	public List<String> getAssertFalseOnString() {
		return assertFalseOnString;
	}

	public void setAssertFalseOnString(List<String> assertFalseOnString) {
		this.assertFalseOnString = assertFalseOnString;
	}

	public List<Boolean> getDecimalMaxOnBoolean() {
		return decimalMaxOnBoolean;
	}

	public void setDecimalMaxOnBoolean(List<Boolean> decimalMaxOnBoolean) {
		this.decimalMaxOnBoolean = decimalMaxOnBoolean;
	}

	public List<Boolean> getDecimalMinOnBoolean() {
		return decimalMinOnBoolean;
	}

	public void setDecimalMinOnBoolean(List<Boolean> decimalMinOnBoolean) {
		this.decimalMinOnBoolean = decimalMinOnBoolean;
	}

	public List<Boolean> getDigitsOnBoolean() {
		return digitsOnBoolean;
	}

	public void setDigitsOnBoolean(List<Boolean> digitsOnBoolean) {
		this.digitsOnBoolean = digitsOnBoolean;
	}

	public List<Integer> getEmailOnInteger() {
		return emailOnInteger;
	}

	public void setEmailOnInteger(List<Integer> emailOnInteger) {
		this.emailOnInteger = emailOnInteger;
	}

	public List<String> getFutureOnString() {
		return futureOnString;
	}

	public void setFutureOnString(List<String> futureOnString) {
		this.futureOnString = futureOnString;
	}

	public List<Integer> getFutureOrPresentOnInteger() {
		return futureOrPresentOnInteger;
	}

	public void setFutureOrPresentOnInteger(List<Integer> futureOrPresentOnInteger) {
		this.futureOrPresentOnInteger = futureOrPresentOnInteger;
	}

	public List<Boolean> getPastOnBoolean() {
		return pastOnBoolean;
	}

	public void setPastOnBoolean(List<Boolean> pastOnBoolean) {
		this.pastOnBoolean = pastOnBoolean;
	}

	public List<String> getPastOrPresentOnString() {
		return pastOrPresentOnString;
	}

	public void setPastOrPresentOnString(List<String> pastOrPresentOnString) {
		this.pastOrPresentOnString = pastOrPresentOnString;
	}

	public List<Boolean> getMinOnBoolean() {
		return minOnBoolean;
	}

	public void setMinOnBoolean(List<Boolean> minOnBoolean) {
		this.minOnBoolean = minOnBoolean;
	}

	public List<String> getMaxOnString() {
		return maxOnString;
	}

	public void setMaxOnString(List<String> maxOnString) {
		this.maxOnString = maxOnString;
	}

	public List<Boolean> getNegativeOnBoolean() {
		return negativeOnBoolean;
	}

	public void setNegativeOnBoolean(List<Boolean> negativeOnBoolean) {
		this.negativeOnBoolean = negativeOnBoolean;
	}

	public List<String> getNegativeOrZeroOnString() {
		return negativeOrZeroOnString;
	}

	public void setNegativeOrZeroOnString(List<String> negativeOrZeroOnString) {
		this.negativeOrZeroOnString = negativeOrZeroOnString;
	}

	public List<Boolean> getPositiveOnBoolean() {
		return positiveOnBoolean;
	}

	public void setPositiveOnBoolean(List<Boolean> positiveOnBoolean) {
		this.positiveOnBoolean = positiveOnBoolean;
	}

	public List<String> getPositiveOrZeroOnString() {
		return positiveOrZeroOnString;
	}

	public void setPositiveOrZeroOnString(List<String> positiveOrZeroOnString) {
		this.positiveOrZeroOnString = positiveOrZeroOnString;
	}

	public List<Integer> getNotBlankOnInteger() {
		return notBlankOnInteger;
	}

	public void setNotBlankOnInteger(List<Integer> notBlankOnInteger) {
		this.notBlankOnInteger = notBlankOnInteger;
	}

	public List<Integer> getPatternOnInteger() {
		return patternOnInteger;
	}

	public void setPatternOnInteger(List<Integer> patternOnInteger) {
		this.patternOnInteger = patternOnInteger;
	}

	public Map<String, Boolean> getSizeOnBoolean() {
		return sizeOnBoolean;
	}

	public void setSizeOnBoolean(Map<String, Boolean> sizeOnBoolean) {
		this.sizeOnBoolean = sizeOnBoolean;
	}

	public List<Boolean> getNotEmptyOnBoolean() {
		return notEmptyOnBoolean;
	}

	public void setNotEmptyOnBoolean(List<Boolean> notEmptyOnBoolean) {
		this.notEmptyOnBoolean = notEmptyOnBoolean;
	}

	public Map<String, List<Integer>> getNestedEmailOnInteger() {
		return nestedEmailOnInteger;
	}

	public void setNestedEmailOnInteger(Map<String, List<Integer>> nestedEmailOnInteger) {
		this.nestedEmailOnInteger = nestedEmailOnInteger;
	}
    
    
}
