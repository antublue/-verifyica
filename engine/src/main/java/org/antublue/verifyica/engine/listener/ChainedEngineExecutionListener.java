/*
 * Copyright (C) 2023 The Verifyica project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.verifyica.engine.listener;

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.engine.common.Precondition;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

/** Class to implement ChainedEngineExecutionListener */
public class ChainedEngineExecutionListener implements EngineExecutionListener {

    private final Collection<EngineExecutionListener> engineExecutionListeners = new ArrayList<>();

    /**
     * Constructor
     *
     * @param engineExecutionListener engineExecutionListener
     */
    public ChainedEngineExecutionListener(EngineExecutionListener engineExecutionListener) {
        Precondition.notNull(engineExecutionListener, "engineExecutionListener is null");

        this.engineExecutionListeners.add(engineExecutionListener);
    }

    /**
     * Method to add an EngineExecutionListener
     *
     * @param engineExecutionListener engineExecutionListener
     * @return this
     */
    public ChainedEngineExecutionListener add(EngineExecutionListener engineExecutionListener) {
        Precondition.notNull(engineExecutionListener, "engineExecutionListener is null");

        this.engineExecutionListeners.add(engineExecutionListener);

        return this;
    }

    /**
     * Method to get the Collection of EngineExecutionListeners
     *
     * @return the Collection of EngineExecutionListeners
     */
    public Collection<EngineExecutionListener> getEngineExecutionListeners() {
        return engineExecutionListeners;
    }

    @Override
    public void dynamicTestRegistered(TestDescriptor testDescriptor) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.dynamicTestRegistered(testDescriptor));
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.executionSkipped(testDescriptor, reason));
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.executionStarted(testDescriptor));
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.executionFinished(
                                testDescriptor, testExecutionResult));
    }

    @Override
    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.reportingEntryPublished(testDescriptor, entry));
    }
}
