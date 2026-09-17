/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4jakarta.lsp4e;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * A stylish dialog for selecting Jakarta EE version with a dropdown and apply button.
 */
public class JakartaVersionSelectionDialog extends Dialog {

    private String projectUri;
    private List<String> versions;
    private String selectedVersion;
    private Combo versionCombo;

    /**
     * Create the dialog.
     *
     * @param parentShell the parent shell
     * @param projectUri the project URI
     * @param versions the list of available versions
     */
    public JakartaVersionSelectionDialog(Shell parentShell, String projectUri, List<String> versions) {
        super(parentShell);
        this.projectUri = projectUri;
        this.versions = versions;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Jakarta EE Version");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 15;
        layout.marginWidth = 15;
        layout.verticalSpacing = 10;
        container.setLayout(layout);

        // Title label
        Label titleLabel = new Label(container, SWT.WRAP);
        titleLabel.setText("Select Jakarta EE Version");
        titleLabel.setFont(org.eclipse.jface.resource.JFaceResources.getBannerFont());
        GridData titleData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        titleLabel.setLayoutData(titleData);

        // Project info label
        Label projectLabel = new Label(container, SWT.WRAP);
        projectLabel.setText("Project: " + getProjectName(projectUri));
        GridData projectData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        projectData.widthHint = 400;
        projectLabel.setLayoutData(projectData);

        // Separator
        Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData separatorData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        separatorData.verticalIndent = 5;
        separator.setLayoutData(separatorData);

        // Version selection composite
        Composite selectionComposite = new Composite(container, SWT.NONE);
        GridLayout selectionLayout = new GridLayout(2, false);
        selectionLayout.marginHeight = 10;
        selectionLayout.marginWidth = 0;
        selectionLayout.horizontalSpacing = 10;
        selectionComposite.setLayout(selectionLayout);
        GridData selectionData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        selectionComposite.setLayoutData(selectionData);

        // Version label
        Label versionLabel = new Label(selectionComposite, SWT.NONE);
        versionLabel.setText("Jakarta EE Version:");
        GridData labelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        versionLabel.setLayoutData(labelData);

        // Version dropdown (Combo)
        versionCombo = new Combo(selectionComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
        GridData comboData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboData.widthHint = 200;
        comboData.minimumWidth = 150;
        versionCombo.setLayoutData(comboData);

        // Populate combo with versions
        for (String version : versions) {
            versionCombo.add("Jakarta EE " + version);
        }

        // Select first item by default
        if (!versions.isEmpty()) {
            versionCombo.select(0);
            selectedVersion = versions.get(0);
        }

        // Add selection listener
        versionCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = versionCombo.getSelectionIndex();
                if (index >= 0 && index < versions.size()) {
                    selectedVersion = versions.get(index);
                }
            }
        });

        // Description label
        Label descLabel = new Label(container, SWT.WRAP);
        descLabel.setText("Select the Jakarta EE version that matches your project's dependencies. This will be used for code completion and validation.");
        GridData descData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        descData.widthHint = 400;
        descData.verticalIndent = 5;
        descLabel.setLayoutData(descData);

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // Create Apply and Cancel buttons
        createButton(parent, IDialogConstants.OK_ID, "Apply", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        // Update selected version before closing
        int index = versionCombo.getSelectionIndex();
        if (index >= 0 && index < versions.size()) {
            selectedVersion = versions.get(index);
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 300);
    }

    /**
     * Get the selected version.
     *
     * @return the selected version, or null if cancelled
     */
    public String getSelectedVersion() {
        return selectedVersion;
    }

    /**
     * Extract project name from URI.
     *
     * @param uri the project URI
     * @return the project name
     */
    private String getProjectName(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "Unknown Project";
        }

        // Extract the last segment of the URI as project name
        String[] segments = uri.split("/");
        if (segments.length > 0) {
            String lastSegment = segments[segments.length - 1];
            // Remove file:// prefix if present
            if (lastSegment.startsWith("file:")) {
                lastSegment = lastSegment.substring(5);
            }
            return lastSegment.isEmpty() ? uri : lastSegment;
        }

        return uri;
    }
}

// Made with Bob
