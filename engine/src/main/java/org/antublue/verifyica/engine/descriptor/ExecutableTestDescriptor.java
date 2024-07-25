/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.engine.descriptor;

import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.util.StopWatch;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Abstract class to implement ExecutableTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor
        implements MetadataTestDescriptor {

    /** Metadata */
    protected final Metadata metadata;

    /** Stopwatch */
    protected final StopWatch stopWatch;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected ExecutableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);

        metadata = new Metadata();
        stopWatch = new StopWatch();
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executionRequest executionRequest
     * @param context context
     */
    public abstract void execute(ExecutionRequest executionRequest, Context context);

    /**
     * Method to skip child test descriptors
     *
     * @param executionRequest executionRequest
     * @param context context
     */
    public abstract void skip(ExecutionRequest executionRequest, Context context);
}
