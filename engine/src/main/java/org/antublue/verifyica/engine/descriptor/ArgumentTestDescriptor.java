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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ArgumentSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.util.StateMonitor;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;
    private final Argument<?> testArgument;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgument testArgument
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     */
    public ArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Argument<?> testArgument,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods) {
        super(uniqueId, displayName);

        ArgumentSupport.notNull(testClass, "testClass is null");
        ArgumentSupport.notNull(testArgument, "testArgument is null");

        this.testClass = testClass;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
        this.testArgument = testArgument;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Method to get the test argument
     *
     * @return the test argument
     */
    public Argument<?> getTestArgument() {
        return testArgument;
    }

    @Override
    public void execute(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("execute ArgumentTestDescriptor [%s]", this);

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;

        ArgumentSupport.notNull(executionRequest, "executionRequest is null");
        ArgumentSupport.notNull(defaultArgumentContext, "defaultArgumentContext is null");
        ArgumentSupport.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");

        getStopWatch().reset();

        defaultArgumentContext.setTestArgument(testArgument);

        ArgumentSupport.notNull(defaultArgumentContext.getTestInstance(), "testInstance is null");
        ArgumentSupport.notNull(defaultArgumentContext.getTestArgument(), "testArgument is null");

        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateMonitor<String> stateMonitor = new StateMonitor<>();

        try {
            stateMonitor.put("beforeAll");
            beforeAll(defaultArgumentContext);
            stateMonitor.put("beforeAll->SUCCESS");
        } catch (Throwable t) {
            stateMonitor.put("beforeAll->FAILURE", t);
            t.printStackTrace(System.err);
        }

        if (stateMonitor.contains("beforeAll->SUCCESS")) {
            try {
                stateMonitor.put("doExecute");
                doExecute(executionRequest, defaultArgumentContext);
                stateMonitor.put("doExecute->SUCCESS");
            } catch (Throwable t) {
                stateMonitor.put("doExecute->FAILURE", t);
                // Don't log the throwable since it's from downstream test descriptors
            }
        }

        if (stateMonitor.contains("beforeAll->FAILURE")) {
            try {
                stateMonitor.put("doSkip");
                doSkip(executionRequest, defaultArgumentContext);
                stateMonitor.put("doSkip->SUCCESS");
            } catch (Throwable t) {
                stateMonitor.put("doSkip->FAILURE", t);
                // Don't log the throwable since it's from downstream test descriptors
            }
        }

        try {
            stateMonitor.put("afterAll");
            afterAll(defaultArgumentContext);
            stateMonitor.put("afterAll->SUCCESS");
        } catch (Throwable t) {
            stateMonitor.put("afterAll->FAILURE", t);
            t.printStackTrace(System.err);
        }

        getStopWatch().stop();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(this);
            stateMonitor
                    .entrySet()
                    .forEach(
                            new Consumer<StateMonitor.Entry<String>>() {
                                @Override
                                public void accept(StateMonitor.Entry<String> stateTrackerEntry) {
                                    LOGGER.trace("%s %s", this, stateTrackerEntry);
                                }
                            });
        }

        TestExecutionResult testExecutionResult =
                stateMonitor
                        .getFirstStateEntryWithThrowable()
                        .map(entry -> TestExecutionResult.failed(entry.getThrowable()))
                        .orElse(TestExecutionResult.successful());

        executionRequest.getEngineExecutionListener().executionFinished(this, testExecutionResult);
    }

    @Override
    public void skip(ExecutionRequest executionRequest, Context context) {
        LOGGER.trace("skip [%s]", this);

        getStopWatch().reset();

        DefaultArgumentContext defaultArgumentContext = (DefaultArgumentContext) context;
        defaultArgumentContext.setTestArgument(testArgument);

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor ->
                                executableTestDescriptor.skip(
                                        executionRequest, defaultArgumentContext));

        getStopWatch().stop();

        executionRequest
                .getEngineExecutionListener()
                .executionSkipped(this, format("Argument [%s] skipped", testArgument.getName()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " "
                + getUniqueId()
                + " {"
                + " testClass ["
                + testClass.getName()
                + "]"
                + " beforeAllMethods ["
                + ObjectSupport.toString(beforeAllMethods)
                + "]"
                + " afterAllMethods ["
                + ObjectSupport.toString(afterAllMethods)
                + "] "
                + "}";
    }

    /**
     * Method to invoke all before all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void beforeAll(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "beforeAll() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ClassInterceptorRegistry.getInstance().beforeAll(defaultArgumentContext, beforeAllMethods);
    }

    /**
     * Method to execute all child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultArgumentContext defaultArgumentContext
     */
    private void doExecute(
            ExecutionRequest executionRequest, DefaultArgumentContext defaultArgumentContext) {
        LOGGER.trace(
                "doExecute() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ArgumentSupport.notNull(executionRequest, "executionRequest is null");

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor ->
                                executableTestDescriptor.execute(
                                        executionRequest, defaultArgumentContext));
    }

    /**
     * Method to skip all child test descriptors
     *
     * @param executionRequest executionRequest
     * @param defaultArgumentContext defaultArgumentContext
     */
    private void doSkip(
            ExecutionRequest executionRequest, DefaultArgumentContext defaultArgumentContext) {
        LOGGER.trace(
                "doSkip() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        getChildren().stream()
                .map(TO_EXECUTABLE_TEST_DESCRIPTOR)
                .forEach(
                        executableTestDescriptor -> {
                            executableTestDescriptor.skip(executionRequest, defaultArgumentContext);
                        });
    }

    /**
     * Method to invoke all after all methods
     *
     * @param defaultArgumentContext defaultArgumentContext
     * @throws Throwable Throwable
     */
    private void afterAll(DefaultArgumentContext defaultArgumentContext) throws Throwable {
        LOGGER.trace(
                "afterAll() testClass [%s] argument [%s]",
                testClass.getName(), defaultArgumentContext.getTestArgument().getName());

        ClassInterceptorRegistry.getInstance().afterAll(defaultArgumentContext, afterAllMethods);

        if (testArgument instanceof AutoCloseable) {
            ((AutoCloseable) testArgument).close();
        }
    }
}
