package io.openliberty.sample.jakarta.beanvalidation;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public class ConflictingConstraints {
    
    // @Min > @Max conflicts
    @Min(value = 100)
    @Max(value = 50)
    private int invalidMinMax;
    
    @Min(value = 10)
    @Max(value = 100)
    private int validMinMax;
    
    // @DecimalMin > @DecimalMax conflicts
    @DecimalMin("100.5")
    @DecimalMax("50.5")
    private BigDecimal invalidDecimalMinMax;
    
    @DecimalMin("10.5")
    @DecimalMax("100.5")
    private BigDecimal validDecimalMinMax;
    
    // @Size min > max conflicts
    @Size(min = 10, max = 5)
    private String invalidSize;
    
    @Size(min = 5, max = 10)
    private String validSize;
    
    // Method with conflicting constraints
    @Min(value = 200)
    @Max(value = 100)
    public int getInvalidMethodMinMax() {
        return 0;
    }
    
    // Parameter with conflicting constraints
    public void methodWithInvalidParam(@Min(value = 50) @Max(value = 10) int param) {
    }
    
    // Edge case: equal values (should be valid)
    @Min(value = 50)
    @Max(value = 50)
    private int equalMinMax;
    
    @Size(min = 5, max = 5)
    private String equalSize;
}

// Made with Bob
