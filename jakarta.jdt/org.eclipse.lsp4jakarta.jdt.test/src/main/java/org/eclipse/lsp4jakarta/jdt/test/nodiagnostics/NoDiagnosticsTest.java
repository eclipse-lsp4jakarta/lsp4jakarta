/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.test.nodiagnostics;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.test.core.BaseJakartaTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NoDiagnosticsTest extends BaseJakartaTest {

    @Parameter
    public String filePath;

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void checkForNoDiagnostics() throws Exception {
        IJavaProject javaProject = loadJavaProject("demo-servlet-no-diagnostics", "");
        IFile javaFile = javaProject.getProject().getFile(new Path(filePath));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // should be no diagnostics in the file.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);

    }

    // list of java files in demo-servlet-no-diagnostics.
    @Parameters
    public static List<String> projectFileProvider() throws Exception {

        String packagePath = "/src/main/java/io/openliberty/sample/jakarta/";
        String basePath = System.getProperty("user.dir")
                          + "/projects/demo-servlet-no-diagnostics/";
        File dir = new File(basePath + packagePath);
        String[] extensions = new String[] { "java" };
        List<String> results = new ArrayList<String>();

        Collection<File> files = FileUtils.listFiles(dir, extensions, true);
        for (File file : files) {
            // Get relative path from source folder and add it in the results array.
            results.add(file.getAbsolutePath().substring(basePath.length()));
        }

        return results;
    }

}
