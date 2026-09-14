/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
package org.eclipse.lsp4jakarta.commons;

import java.util.Collections;
import java.util.List;

/**
 * Stores labels for the project located at a specific project uri
 *
 * @author dakwon
 *
 */
public class ProjectLabelInfoEntry {
    public static final ProjectLabelInfoEntry EMPTY_PROJECT_INFO = new ProjectLabelInfoEntry("", "", Collections.emptyList(), Collections.emptyList());

    private final String uri;
    private final String name;
    private final List<String> labels;
    private final List<String> classPath;

    public ProjectLabelInfoEntry(String uri, String name, List<String> labels, List<String> classPath) {
        this.uri = uri;
        this.name = name;
        this.labels = labels;
        this.classPath = classPath;
    }

    /**
     * Returns the project uri
     *
     * @return the project uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns the name of the project
     *
     * @return The name of this project
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the labels for the current project uri
     *
     * @return the labels for the current project uri
     */
    public List<String> getLabels() {
        return labels;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    /**
     * Returns true if the project has the given label and false otherwise.
     *
     * @param label the label.
     * @return true if the project has the given label and false otherwise.
     */
    public boolean hasLabel(String label) {
        //boolean truth = true;
        //return truth;
        // right?
        return labels != null && labels.contains(label);
    }
}