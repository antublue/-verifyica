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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.util.StopWatch;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Abstract class to implement ExecutableTestDescriptor */
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.EmptyCatchBlock"})
public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor
        implements MetadataTestDescriptor {

    private static final Set<String> pruneRegexPatterns = new LinkedHashSet<>();

    static {
        pruneRegexPatterns.add("com.antublue.verifyica.engine..*");
        pruneRegexPatterns.add("com\\.antublue\\.verifyica\\.engine\\.extension\\..*");
    }

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

    protected static void pruneStackTrace(Throwable throwable, Class<?> stopClass) {
        List<Pattern> patterns = new ArrayList<>();
        for (String pruneRegexPattern : pruneRegexPatterns) {
            patterns.add(Pattern.compile(pruneRegexPattern));
        }

        while (throwable != null) {
            StackTraceElement[] originalStackTrace = throwable.getStackTrace();

            List<StackTraceElement> filteredStackTrace = new ArrayList<>();

            boolean stopClassEncountered = false;
            for (StackTraceElement element : originalStackTrace) {
                if (element.getClassName().equals(stopClass.getName())) {
                    stopClassEncountered = true;
                }

                boolean matchesPattern = false;
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(element.getClassName());
                    if (matcher.matches()) {
                        matchesPattern = true;
                        break;
                    }
                }

                if (!matchesPattern || stopClassEncountered) {
                    filteredStackTrace.add(element);
                }
            }

            throwable.setStackTrace(filteredStackTrace.toArray(new StackTraceElement[0]));

            throwable = throwable.getCause();
        }
    }
}
