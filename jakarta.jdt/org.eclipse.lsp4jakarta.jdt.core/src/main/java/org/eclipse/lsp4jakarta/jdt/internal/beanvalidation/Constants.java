/*******************************************************************************
* Copyright (c) 2020, 2025 IBM Corporation, Reza Akhavan and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Reza Akhavan - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.beanvalidation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bean validation constants.
 */
public class Constants {

    /* Annotations */
    public static final String ASSERT_TRUE = "jakarta.validation.constraints.AssertTrue";
    public static final String ASSERT_FALSE = "jakarta.validation.constraints.AssertFalse";
    public static final String DIGITS = "jakarta.validation.constraints.Digits";
    public static final String DECIMAL_MAX = "jakarta.validation.constraints.DecimalMax";
    public static final String DECIMAL_MIN = "jakarta.validation.constraints.DecimalMin";
    public static final String EMAIL = "jakarta.validation.constraints.Email";
    public static final String PAST_OR_PRESENT = "jakarta.validation.constraints.PastOrPresent";
    public static final String FUTURE_OR_PRESENT = "jakarta.validation.constraints.FutureOrPresent";
    public static final String PAST = "jakarta.validation.constraints.Past";
    public static final String FUTURE = "jakarta.validation.constraints.Future";
    public static final String MIN = "jakarta.validation.constraints.Min";
    public static final String MAX = "jakarta.validation.constraints.Max";
    public static final String NEGATIVE_OR_ZERO = "jakarta.validation.constraints.NegativeOrZero";
    public static final String POSITIVE_OR_ZERO = "jakarta.validation.constraints.PositiveOrZero";
    public static final String NEGATIVE = "jakarta.validation.constraints.Negative";
    public static final String POSITIVE = "jakarta.validation.constraints.Positive";
    public static final String NOT_BLANK = "jakarta.validation.constraints.NotBlank";
    public static final String PATTERN = "jakarta.validation.constraints.Pattern";
    public static final String SIZE = "jakarta.validation.constraints.Size";
    public static final String NOT_EMPTY = "jakarta.validation.constraints.NotEmpty";

    /* Types */
    public static final String THAI_BUDDHIST_DATE = "java.time.chrono.ThaiBuddhistDate";
    public static final String MINGUO_DATE = "java.time.chrono.MinguoDate";
    public static final String JAPANESE_DATE = "java.time.chrono.JapaneseDate";
    public static final String HIJRAH_DATE = "java.time.chrono.HijrahDate";
    public static final String ZONED_DATE_TIME = "java.time.ZonedDateTime";
    public static final String YEAR_MONTH = "java.time.YearMonth";
    public static final String YEAR = "java.time.Year";
    public static final String OFFSET_TIME = "java.time.OffsetTime";
    public static final String OFFSET_DATE_TIME = "java.time.OffsetDateTime";
    public static final String MONTH_DAY = "java.time.MonthDay";
    public static final String LOCAL_TIME = "java.time.LocalTime";
    public static final String LOCAL_DATE_TIME = "java.time.LocalDateTime";
    public static final String LOCAL_DATE = "java.time.LocalDate";
    public static final String INSTANT = "java.time.Instant";
    public static final String CALENDAR = "java.util.Calendar";
    public static final String DATE = "java.util.Date";
    public static final String BOOLEAN_FQ = "java.lang.Boolean";
    public static final String CHAR_SEQUENCE_FQ = "java.lang.CharSequence";
    public static final String STRING_FQ = "java.lang.String";
    public static final String DOUBLE_FQ = "java.lang.Double";
    public static final String FLOAT_FQ = "java.lang.Float";
    public static final String LONG_FQ = "java.lang.Long";
    public static final String INTEGER_FQ = "java.lang.Integer";
    public static final String SHORT_FQ = "java.lang.Short";
    public static final String BYTE_FQ = "java.lang.Byte";
    public static final String BIG_INTEGER_FQ = "java.math.BigInteger";
    public static final String BIG_DECIMAL_FQ = "java.math.BigDecimal";

    public static final String DIAGNOSTIC_SOURCE = "jakarta-bean-validation";

    public final static Set<String> SET_OF_ANNOTATIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(ASSERT_TRUE, ASSERT_FALSE, DIGITS, DECIMAL_MAX,
                                                                                                                       DECIMAL_MIN, EMAIL, PAST_OR_PRESENT, FUTURE_OR_PRESENT, PAST,
                                                                                                                       FUTURE, MIN, MAX, NEGATIVE_OR_ZERO,
                                                                                                                       POSITIVE_OR_ZERO, NEGATIVE, POSITIVE, NOT_BLANK, PATTERN,
                                                                                                                       SIZE, NOT_EMPTY)));
    public final static Set<String> SET_OF_DATE_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(THAI_BUDDHIST_DATE, MINGUO_DATE, JAPANESE_DATE,
                                                                                                                      HIJRAH_DATE, ZONED_DATE_TIME, YEAR_MONTH, YEAR, OFFSET_TIME,
                                                                                                                      OFFSET_DATE_TIME, MONTH_DAY,
                                                                                                                      LOCAL_TIME, LOCAL_DATE_TIME, LOCAL_DATE, INSTANT, CALENDAR,
                                                                                                                      DATE)));

    public static final String[] NUMERIC_WRAPPER_TYPES = { BIG_DECIMAL_FQ, BIG_INTEGER_FQ, BYTE_FQ, SHORT_FQ,
                                                           INTEGER_FQ, LONG_FQ };

    public final static String[] NUMERIC_AND_DECIMAL_WRAPPER_TYPES = { BIG_DECIMAL_FQ, BIG_INTEGER_FQ, BYTE_FQ, SHORT_FQ,
                                                                       INTEGER_FQ, LONG_FQ, FLOAT_FQ, DOUBLE_FQ };
    public final static String[] NUMERIC_AND_CHAR_WRAPPER_TYPES = { BIG_DECIMAL_FQ, BIG_INTEGER_FQ, BYTE_FQ, SHORT_FQ,
                                                                    INTEGER_FQ, LONG_FQ, CHAR_SEQUENCE_FQ };

}
