package io.openliberty.sample.jakarta.beanvalidation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class MethodParamConstraintValidation {

	// valid cases
	@AssertTrue
	public boolean truth(@Past Calendar theGoodOldDays, @Positive float area,
			@NotEmpty ValidConstraints[] validConstraints) {
		return true;
	}

	@AssertTrue
	public Boolean boolValid(@Negative int subZero, @NotBlank CharSequence saysomething,
			@Pattern(regexp = "") String thisIsUsed) {
		return false;
	}

	@AssertFalse
	private boolean boolTwoValid(@Digits(fraction = 0, integer = 0) BigInteger x, @Email String emailAddress,
			@FutureOrPresent Date graduationDate) {
		return true;
	}

	@Size
	private double[] getSalaryValid(@Size double[] x, @AssertTrue boolean s,
			@DecimalMax("30.0") BigDecimal bigDecimal) {
		return null;
	}

	
	// invalid cases
	@AssertTrue
	public boolean anotherTruth(@Past double theGoodOldDays, @Positive String[] area,
			@NotEmpty ValidConstraints validConstraints) {
		return true;
	}

	@AssertTrue
	public Boolean bool(@Negative boolean subZero, @NotBlank boolean saysomething,
			@Pattern(regexp = "") Calendar thisIsUsed) {
		return false;
	}

	@AssertFalse
	private boolean boolTwo(@Digits(fraction = 0, integer = 0) boolean x, @Email Integer emailAddress,
			@FutureOrPresent boolean graduationDate) {
		return true;
	}

	@Size
	private double[] getSalary(@Size double x, @AssertTrue String s, @DecimalMax("30.0") String bigDecimal) {
		return null;
	}

}