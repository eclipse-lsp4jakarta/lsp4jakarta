package org.eclipse.lsp4jakarta.version;

public class JakartaVersionTest {

    public static void main(String[] args) {

        JakartaVersion projectVersion9 = JakartaVersion.EE_9; // Project 1
        JakartaVersion projectVersion10 = JakartaVersion.EE_10;// Project 2
        JakartaVersion projectVersion11 = JakartaVersion.EE_11;// Project 3

        JakartaDiagnostic D1 = JakartaDiagnostic.INVALID_CONSTRAIN_ANNOTATION_ON_STATIC_METHOD_OR_FIELD; // JEE9
        JakartaDiagnostic D2 = JakartaDiagnostic.INVALID_ANNOTATION_ON_NON_BOOLEAN_METHOD_OR_FIELD; // JEE10
        JakartaDiagnostic D3 = JakartaDiagnostic.INVALID_ANNOTATION_ON_NON_DATETIME_METHOD_OR_FIELD; // JEE11

        System.out.println("Project version 9 ----------\n");
        System.out.println(D1.getCode() + "-" + D1.getMinVersion() + ":" + D1.isApplicableTo(projectVersion9));
        System.out.println(D2.getCode() + "-" + D2.getMinVersion() + ":" + D2.isApplicableTo(projectVersion9));
        System.out.println(D3.getCode() + "-" + D3.getMinVersion() + ":" + D3.isApplicableTo(projectVersion9));

        System.out.println("\nProject version 10 ----------\n");
        System.out.println(D1.getCode() + "-" + D1.getMinVersion() + ":" + D1.isApplicableTo(projectVersion10));
        System.out.println(D2.getCode() + "-" + D2.getMinVersion() + ":" + D2.isApplicableTo(projectVersion10));
        System.out.println(D3.getCode() + "-" + D3.getMinVersion() + ":" + D3.isApplicableTo(projectVersion10));

        System.out.println("\nProject version 11 ----------\n");
        System.out.println(D1.getCode() + "-" + D1.getMinVersion() + ":" + D1.isApplicableTo(projectVersion11));
        System.out.println(D2.getCode() + "-" + D2.getMinVersion() + ":" + D2.isApplicableTo(projectVersion11));
        System.out.println(D3.getCode() + "-" + D3.getMinVersion() + ":" + D3.isApplicableTo(projectVersion11));

    }
}
