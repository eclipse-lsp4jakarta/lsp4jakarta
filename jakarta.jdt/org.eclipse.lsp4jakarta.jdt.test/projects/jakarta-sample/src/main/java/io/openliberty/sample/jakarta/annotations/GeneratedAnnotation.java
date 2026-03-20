package io.openliberty.sample.jakarta.annotations;

import jakarta.annotation.Generated;

@Generated(value = "", date="")
public class GeneratedAnnotation {

    @Generated(value = "com.demoServlet", date="not_ISO_compliant")
    private Integer studentId;

    @Generated(value = "demoServlet", date="2001-07-04T12:08:56.235-0700")
    private boolean isHappy;

    @Generated(value = "com.demoServlet", date="NOTISOCOMPLIANT2")
    private boolean isSad;

    @Generated("com.sun.xml.rpc.AProcessor")
    private String emailAddress;
    
    @Generated(value="com.sun.xml.rpc.AProcessor")
    private String homeTown;
    
    public GeneratedAnnotation(Integer studentId, Boolean isHappy, boolean isSad, String graduationDate, Integer gpa,
            String emailAddress) {
        this.studentId = studentId;
        this.isHappy = isHappy;
        this.isSad = isSad;
        this.emailAddress = emailAddress;
    }

}
