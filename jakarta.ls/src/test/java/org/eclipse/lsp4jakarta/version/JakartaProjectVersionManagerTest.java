/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JakartaProjectVersionManagerTest {

    private static final List<JakartaVersion> DETECTED = Arrays.asList(JakartaVersion.EE_10, JakartaVersion.EE_9, JakartaVersion.EE_11);

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private JakartaProjectVersionManager manager;

    private String project() throws IOException {
        Path project = tmp.newFolder("project").toPath();
        return project.toString();
    }

    private CountDownLatch resolvedLatch;
    private final List<String> resolved = new CopyOnWriteArrayList<>();

    private JakartaProjectVersionManager manager(CompletableFuture<String> answer, boolean selectorSupported) {
        resolvedLatch = new CountDownLatch(1);
        manager = new JakartaProjectVersionManager(params -> answer, () -> selectorSupported, uri -> {
            resolved.add(uri);
            resolvedLatch.countDown();
        });
        return manager;
    }

    @Test
    public void userSelectionIsApplied() throws Exception {
        String project = project();
        JakartaProjectVersionManager m = manager(CompletableFuture.completedFuture(JakartaVersion.EE_10.getLabel()), true);

        assertFalse(m.resolve(project, DETECTED));
        assertTrue(resolvedLatch.await(5, TimeUnit.SECONDS));

        VersionData data = m.getVersionData(project);
        assertNotNull(data);
        assertEquals(JakartaVersion.EE_10.getLabel(), data.getVersion());
        assertEquals(SelectionMode.USER_SELECTED, data.getSelectionMode());
        m.shutdown();
    }

    @Test
    public void cancelledSelectionAppliesDefaultVersion() throws Exception {
        String project = project();
        JakartaProjectVersionManager m = manager(CompletableFuture.completedFuture(null), true);

        assertFalse(m.resolve(project, DETECTED));
        assertTrue(resolvedLatch.await(5, TimeUnit.SECONDS));

        VersionData data = m.getVersionData(project);
        assertNotNull(data);
        assertEquals(JakartaVersion.EE_11.getLabel(), data.getVersion());
        assertEquals(SelectionMode.DEFAULT, data.getSelectionMode());
        assertEquals(Collections.singletonList(project), resolved);
        m.shutdown();
    }

    @Test
    public void clientWithoutSelectorAppliesDefaultVersion() throws Exception {
        String project = project();
        JakartaProjectVersionManager m = manager(new CompletableFuture<>(), false);

        assertFalse(m.resolve(project, DETECTED));

        VersionData data = m.getVersionData(project);
        assertNotNull(data);
        assertEquals(JakartaVersion.EE_11.getLabel(), data.getVersion());
        assertEquals(SelectionMode.DEFAULT, data.getSelectionMode());
        m.shutdown();
    }

    @Test
    public void defaultIsHighestDetectedVersion() {
        assertEquals(JakartaVersion.EE_11.getLabel(), JakartaProjectVersionManager.selectDefaultVersion(DETECTED, "highest"));
    }

    @Test
    public void lowestStrategyPicksLowestDetectedVersion() {
        assertEquals(JakartaVersion.EE_9.getLabel(), JakartaProjectVersionManager.selectDefaultVersion(DETECTED, "lowest"));
    }

    @Test
    public void unknownStrategyPicksHighestDetectedVersion() {
        assertEquals(JakartaVersion.EE_11.getLabel(), JakartaProjectVersionManager.selectDefaultVersion(DETECTED, "other"));
    }

    @Test
    public void nothingDetectedFallsBackToEe9() {
        assertEquals(JakartaVersion.EE_9.getLabel(), JakartaProjectVersionManager.selectDefaultVersion(Collections.emptyList(), "highest"));
    }
}
