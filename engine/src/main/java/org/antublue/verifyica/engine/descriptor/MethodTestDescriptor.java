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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.extension.ClassExtensionRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.util.StateTracker;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement MethodTestDescriptor */
public class MethodTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param beforeEachMethods beforeEachMethods
     * @param testMethod testMethod
     * @param afterEachMethods afterEachMethods
     */
    public MethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> beforeEachMethods,
            Method testMethod,
            List<Method> afterEachMethods) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    @Override
    public void execute(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("execute [%s]", this);

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        Preconditions.notNull(executionRequest, "executionRequest is null");
        Preconditions.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");
        Preconditions.notNull(defaultArgumentContext.getTestArgument(), "testArgument is null");

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        getParent().get().getParent().get().getDisplayName());

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_ARGUMENT,
                        defaultArgumentContext.getTestArgument());
        getMetadata().put(MetadataTestDescriptorConstants.TEST_METHOD, testMethod);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());

        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateTracker<String> stateTracker = new StateTracker<>();

        try {
            stateTracker.put("beforeEach");
            beforeEach(defaultArgumentContext);
            stateTracker.put("beforeEach->SUCCESS");
        } catch (Throwable t) {
            stateTracker.put("beforeEach->FAILURE", t);
            t.printStackTrace(System.err);
        }

        if (stateTracker.contains("beforeEach->SUCCESS")) {
            try {
                stateTracker.put("test");
                test(defaultArgumentContext);
                stateTracker.put("test->SUCCESS");
            } catch (Throwable t) {
                stateTracker.put("test->FAILURE", t);
                t.printStackTrace(System.err);
            }
        }

        try {
            stateTracker.put("afterEach");
            afterEach(defaultArgumentContext);
            stateTracker.put("afterEach->SUCCESS");
        } catch (Throwable t) {
            stateTracker.put("afterEach->FAILURE", t);
            t.printStackTrace(System.err);
        }

        stopWatch.stop();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(this);
            stateTracker
                    .entrySet()
                    .forEach(
                            new Consumer<StateTracker.Entry<String>>() {
                                @Override
                                public void accept(StateTracker.Entry<String> stateTrackerEntry) {
                                    LOGGER.trace("%s %s", this, stateTrackerEntry);
                                }
                            });
        }

        StateTracker.Entry<String> entry = stateTracker.getFirstStateEntryWithThrowable();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        stopWatch.elapsedTime());

        TestExecutionResult testExecutionResult = TestExecutionResult.successful();

        if (entry != null) {
            testExecutionResult = TestExecutionResult.failed(entry.getThrowable());
        }

        executionRequest.getEngineExecutionListener().executionFinished(this, testExecutionResult);
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("skip [%s]", this);

        stopWatch.reset();

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_ARGUMENT,
                        defaultArgumentContext.getTestArgument());
        getMetadata().put(MetadataTestDescriptorConstants.TEST_METHOD, testMethod);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION,
                        Duration.ofMillis(0));
        /*
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        MetadataTestDescriptorConstants.SKIP);
         */

        executionRequest
                .getEngineExecutionListener()
                .executionSkipped(
                        this,
                        format(
                                "Argument [%s] test method [%s] skipped",
                                defaultArgumentContext.getTestArgument().getName(),
                                testMethod.getName()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " "
                + getUniqueId()
                + " { "
                + "testClass ["
                + testClass.getName()
                + "]"
                + " beforeEachMethods ["
                + ObjectSupport.toString(beforeEachMethods)
                + "]"
                + " testMethod ["
                + testMethod.getName()
                + "] afterEachMethods ["
                + ObjectSupport.toString(afterEachMethods)
                + "] "
                + "}";
    }

    /**
     * Method to invoke all before all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void beforeEach(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "beforeEach() testClass [%s] testMethod [%s] argument [%s]",
                testClass.getName(),
                testMethod.getName(),
                defaultArgumentContext.getTestArgument().getName());

        ClassExtensionRegistry.getInstance().beforeEach(defaultArgumentContext, beforeEachMethods);
    }

    /**
     * Method to invoke the test method
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void test(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "test() testClass [%s] testMethod [%s] argument [%s]",
                testClass.getName(),
                testMethod.getName(),
                defaultArgumentContext.getTestArgument().getName());

        ClassExtensionRegistry.getInstance().test(defaultArgumentContext, testMethod);
    }

    /**
     * Method to invoke all after each methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void afterEach(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "afterEach() testClass [%s] testMethod [%s] argument [%s]",
                testClass.getName(),
                testMethod.getName(),
                defaultArgumentContext.getTestArgument().getName());

        ClassExtensionRegistry.getInstance().afterEach(defaultArgumentContext, afterEachMethods);
    }
}
