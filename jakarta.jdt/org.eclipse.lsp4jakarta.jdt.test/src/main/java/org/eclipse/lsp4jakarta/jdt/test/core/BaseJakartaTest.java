/*******************************************************************************
* Copyright (c) 2019, 2025 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.test.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Modified from:
 * https://github.com/eclipse/lsp4mp/blob/bc926f75df2ca103d78c67b997c87adb7ab480b1/microprofile.jdt/org.eclipse.lsp4mp.jdt.test/src/main/java/org/eclipse/lsp4mp/jdt/core/BasePropertiesManagerTest.java
 * With certain methods modified or deleted to fit the purposes of LSP4Jakarta
 *
 */
public class BaseJakartaTest {

    protected static IJavaProject loadJavaProject(String projectName, String parentDirName) throws CoreException, Exception {
        // Move project to working directory
        File projectFolder = copyProjectToWorkingDirectory(projectName, parentDirName);

        IPath path = new Path(new File(projectFolder, "/.project").getAbsolutePath());
        IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(path);
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());

        if (!project.exists()) {
            project.create(description, null);
            project.open(null);

            // We need to call waitForBackgroundJobs with a Job which does nothing to have a
            // resolved classpath (IJavaProject#getResolvedClasspath) when search is done.
            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    monitor.done();

                }
            };
            IProgressMonitor monitor = new NullProgressMonitor();
            JavaCore.run(runnable, null, monitor);
            waitForBackgroundJobs(monitor);
        }

        IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(description.getName());
        return javaProject;
    }

    private static File copyProjectToWorkingDirectory(String projectName, String parentDirName) throws IOException {
        File from = new File("projects/" + parentDirName + "/" + projectName);
        File to = new File(getWorkingProjectDirectory(), java.nio.file.Paths.get(parentDirName, projectName).toString());

        if (to.exists()) {
            FileUtils.forceDelete(to);
        }

        if (from.isDirectory()) {
            FileUtils.copyDirectory(from, to);
        } else {
            FileUtils.copyFile(from, to);
        }

        return to;
    }

    public static File getWorkingProjectDirectory() throws IOException {
        File dir = new File("target", "workingProjects");
        FileUtils.forceMkdir(dir);
        return dir;
    }

    private static void waitForBackgroundJobs(IProgressMonitor monitor) throws Exception {
        JobHelpers.waitForJobsToComplete(monitor);
    }
}
