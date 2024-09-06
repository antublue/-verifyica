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

package org.antublue.verifyica.engine.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.ClassInterceptorContext;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.ThrowableCollector;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.ConcreteArgumentInterceptorContext;
import org.antublue.verifyica.engine.context.ConcreteClassInterceptorContext;
import org.antublue.verifyica.engine.context.ConcreteEngineContext;
import org.antublue.verifyica.engine.context.ConcreteEngineInterceptorContext;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement DefaultClassInterceptorRegistry */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class DefaultClassInterceptorRegistry {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefaultClassInterceptorRegistry.class);

    private final ReadWriteLock readWriteLock;
    private final List<ClassInterceptor> classInterceptors;
    private final Map<Class<?>, List<ClassInterceptor>> mappedClassInterceptors;
    private boolean initialized;

    /** Constructor */
    private DefaultClassInterceptorRegistry() {
        readWriteLock = new ReentrantReadWriteLock(true);
        classInterceptors = new ArrayList<>();
        mappedClassInterceptors = new LinkedHashMap<>();

        loadClassInterceptors();
    }

    /**
     * Method to register a class interceptor
     *
     * @param testClass testClass
     * @param classInterceptor classInterceptors
     * @return this ClassInterceptorRegistry
     */
    public DefaultClassInterceptorRegistry register(
            Class<?> testClass, ClassInterceptor classInterceptor) {
        Precondition.notNull(testClass, "testClass is null");
        Precondition.notNull(classInterceptor, "classInterceptor is null");

        getReadWriteLock().writeLock().lock();
        try {
            mappedClassInterceptors
                    .computeIfAbsent(testClass, c -> new ArrayList<>())
                    .add(classInterceptor);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to remove a class interceptor
     *
     * @param testClass testClass
     * @param classInterceptor classInterceptor
     * @return this ClassInterceptorRegistry
     */
    public DefaultClassInterceptorRegistry remove(
            Class<?> testClass, ClassInterceptor classInterceptor) {
        Precondition.notNull(testClass, "testClass is null");
        Precondition.notNull(classInterceptor, "classInterceptor is null");

        getReadWriteLock().writeLock().lock();
        try {
            mappedClassInterceptors.get(testClass).remove(classInterceptor);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get the number of class interceptors
     *
     * @param testClass testClass
     * @return the number of class interceptors
     */
    public int size(Class<?> testClass) {
        Precondition.notNull(testClass, "testClass is null");

        getReadWriteLock().readLock().lock();
        try {
            List<ClassInterceptor> classInterceptors = mappedClassInterceptors.get(testClass);
            return classInterceptors != null ? classInterceptors.size() : 0;
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to remove all class interceptors
     *
     * @param testClass testClass
     * @return this ClassInterceptorRegistry
     */
    public DefaultClassInterceptorRegistry clear(Class<?> testClass) {
        Precondition.notNull(testClass, "testClass is null");

        getReadWriteLock().writeLock().lock();
        try {
            mappedClassInterceptors.remove(testClass);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to execute class interceptors
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @return a test instance
     * @throws Throwable Throwable
     */
    public Object instantiate(EngineContext engineContext, Class<?> testClass) throws Throwable {
        Object testInstance = null;
        Throwable throwable = null;

        ConcreteEngineInterceptorContext engineInterceptorContext =
                new ConcreteEngineInterceptorContext(engineContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        throwableCollector.execute(
                () -> {
                    for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                        classInterceptor.preInstantiate(engineInterceptorContext, testClass);
                    }
                });

        if (throwableCollector.isEmpty()) {
            try {
                testInstance =
                        testClass
                                .getDeclaredConstructor((Class<?>[]) null)
                                .newInstance((Object[]) null);
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        throwable =
                throwableCollector.getThrowable() != null
                        ? throwableCollector.getThrowable()
                        : throwable;

        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
            try {
                classInterceptor.postInstantiate(
                        engineInterceptorContext, testClass, testInstance, throwable);
            } catch (Throwable t) {
                throwableCollector.add(t);
            }
        }

        throwableCollector.assertEmpty();

        return testInstance;
    }

    /**
     * Method to execute class interceptors
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    public void postInstantiate(
            EngineContext engineContext,
            Class<?> testClass,
            Object testInstance,
            Throwable throwable)
            throws Throwable {
        ConcreteEngineInterceptorContext engineInterceptorContext =
                new ConcreteEngineInterceptorContext(engineContext);

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                classInterceptor.postInstantiate(
                        engineInterceptorContext, testClass, testInstance, throwable);
            }
        } else if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute class interceptors
     *
     * @param classContext classContext
     * @param prepareMethods prepareMethods
     * @throws Throwable Throwable
     */
    public void prepare(ClassContext classContext, List<Method> prepareMethods) throws Throwable {
        Class<?> testClass = classContext.getTestClass();

        ClassInterceptorContext argumentInterceptorContext =
                new ConcreteClassInterceptorContext(classContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        final List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        if (!classInterceptors.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptors) {
                            classInterceptor.prePrepare(argumentInterceptorContext);
                        }
                    });
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        try {
                            for (Method prepareMethod : prepareMethods) {
                                prepareMethod.invoke(null, classContext);
                            }
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            Throwable throwable = throwableCollector.getThrowable();
            throwableCollector.clear();

            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                            classInterceptor.postPrepare(argumentInterceptorContext, throwable);
                        }
                    });
        }

        throwableCollector.assertEmpty();
    }

    /**
     * Method to execute class interceptors
     *
     * @param argumentContext argumentContext
     * @param beforeAllMethods beforeAllMethods
     * @throws Throwable Throwable
     */
    public void beforeAll(ArgumentContext argumentContext, List<Method> beforeAllMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();

        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        final List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        if (!classInterceptors.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptors) {
                            classInterceptor.preBeforeAll(argumentInterceptorContext);
                        }
                    });
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        try {
                            for (Method beforeAllMethod : beforeAllMethods) {
                                beforeAllMethod.invoke(testInstance, argumentContext);
                            }
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            Throwable throwable = throwableCollector.getThrowable();
            throwableCollector.clear();

            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                            classInterceptor.postBeforeAll(argumentInterceptorContext, throwable);
                        }
                    });
        }

        throwableCollector.assertEmpty();
    }

    /**
     * Method to execute class interceptors
     *
     * @param argumentContext argumentContext
     * @param beforeEachMethods beforeEachMethods
     * @throws Throwable Throwable
     */
    public void beforeEach(ArgumentContext argumentContext, List<Method> beforeEachMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();

        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        final List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        if (!classInterceptors.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptors) {
                            classInterceptor.preBeforeEach(argumentInterceptorContext);
                        }
                    });
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        try {
                            for (Method beforEachMethod : beforeEachMethods) {
                                beforEachMethod.invoke(testInstance, argumentContext);
                            }
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            Throwable throwable = throwableCollector.getThrowable();
            throwableCollector.clear();

            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                            classInterceptor.postBeforeEach(argumentInterceptorContext, throwable);
                        }
                    });
        }

        throwableCollector.assertEmpty();
    }

    /**
     * Method to execute class interceptors
     *
     * @param argumentContext argumentContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    public void test(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();

        Class<?> testClass = classContext.getTestClass();

        Object testInstance = classContext.getTestInstance();

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        final List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        if (!classInterceptors.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptors) {
                            classInterceptor.preTest(argumentInterceptorContext, testMethod);
                        }
                    });
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        try {
                            testMethod.invoke(testInstance, argumentContext);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            Throwable throwable = throwableCollector.getThrowable();
            throwableCollector.clear();

            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                            classInterceptor.postTest(
                                    argumentInterceptorContext, testMethod, throwable);
                        }
                    });
        }

        throwableCollector.assertEmpty();
    }

    /**
     * Method to execute class interceptors
     *
     * @param argumentContext argumentContext
     * @param afterEachMethods afterEachMethods
     * @throws Throwable Throwable
     */
    public void afterEach(ArgumentContext argumentContext, List<Method> afterEachMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();

        Class<?> testClass = classContext.getTestClass();

        Object testInstance = classContext.getTestInstance();

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        final List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        if (!classInterceptors.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptors) {
                            classInterceptor.preAfterEach(argumentInterceptorContext);
                        }
                    });
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        try {
                            for (Method afterEachMethod : afterEachMethods) {
                                afterEachMethod.invoke(testInstance, argumentContext);
                            }
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            Throwable throwable = throwableCollector.getThrowable();
            throwableCollector.clear();

            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                            classInterceptor.postAfterEach(argumentInterceptorContext, throwable);
                        }
                    });
        }

        throwableCollector.assertEmpty();
    }

    /**
     * Method to execute class interceptors
     *
     * @param argumentContext argumentContext
     * @param afterAllMethods afterAllMethods
     * @throws Throwable Throwable
     */
    public void afterAll(ArgumentContext argumentContext, List<Method> afterAllMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();

        Class<?> testClass = classContext.getTestClass();

        Object testInstance = classContext.getTestInstance();

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        final List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        if (!classInterceptors.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                            classInterceptor.preAfterAll(argumentInterceptorContext);
                        }
                    });
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        try {
                            for (Method afterAllMethod : afterAllMethods) {
                                afterAllMethod.invoke(testInstance, argumentContext);
                            }
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            Throwable throwable = throwableCollector.getThrowable();
            throwableCollector.clear();

            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                            classInterceptor.postAfterAll(argumentInterceptorContext, throwable);
                        }
                    });
        }

        throwableCollector.assertEmpty();
    }

    /**
     * Method to execute class interceptors
     *
     * @param classContext classContext
     * @param concludeMethods concludeMethods
     * @throws Throwable Throwable
     */
    public void conclude(ClassContext classContext, List<Method> concludeMethods) throws Throwable {
        Class<?> testClass = classContext.getTestClass();

        ClassInterceptorContext defaultClassInterceptorContext =
                new ConcreteClassInterceptorContext(classContext);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        final List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        if (!classInterceptors.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                            classInterceptor.preConclude(defaultClassInterceptorContext);
                        }
                    });
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        try {
                            for (Method concludeMethod : concludeMethods) {
                                concludeMethod.invoke(null, classContext);
                            }
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        final List<ClassInterceptor> classInterceptorsReversed =
                getClassInterceptorsReversed(testClass);

        if (!classInterceptorsReversed.isEmpty()) {
            Throwable throwable = throwableCollector.getThrowable();
            throwableCollector.clear();

            throwableCollector.execute(
                    () -> {
                        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                            classInterceptor.postConclude(
                                    defaultClassInterceptorContext, throwable);
                        }
                    });
        }

        throwableCollector.assertEmpty();
    }

    /**
     * Method to execute class interceptors
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    public void onDestroy(ClassContext classContext) throws Throwable {
        Class<?> testClass = classContext.getTestClass();

        ClassInterceptorContext classInterceptorContext =
                new ConcreteClassInterceptorContext(classContext);

        for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
            classInterceptor.onDestroy(classInterceptorContext);
        }
    }

    /**
     * Method to get a COPY of the List of ClassInterceptors (internal + class specific)
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassInterceptors (internal + class specific)
     */
    private List<ClassInterceptor> getClassInterceptors(Class<?> testClass) {
        getReadWriteLock().writeLock().lock();
        try {
            List<ClassInterceptor> classInterceptors = new ArrayList<>();

            if (this.classInterceptors != null) {
                classInterceptors.addAll(this.classInterceptors);
            }

            classInterceptors.addAll(
                    mappedClassInterceptors.computeIfAbsent(testClass, o -> new ArrayList<>()));

            return classInterceptors;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Method to get a COPY of the List of ClassInterceptors in reverse (internal + class specific)
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassInterceptors in reverse (internal + class specific)
     */
    private List<ClassInterceptor> getClassInterceptorsReversed(Class<?> testClass) {
        List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        Collections.reverse(classInterceptors);

        return classInterceptors;
    }

    /**
     * Method to get the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    private ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    /** Method to load class interceptors */
    private void loadClassInterceptors() {
        getReadWriteLock().writeLock().lock();
        try {
            if (!initialized) {
                LOGGER.trace("loadClassInterceptors()");

                List<Class<?>> autowiredClassInterceptors =
                        new ArrayList<>(
                                ClassSupport.findAllClasses(
                                        InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS));

                filter(autowiredClassInterceptors);

                OrderSupport.orderClasses(autowiredClassInterceptors);

                LOGGER.trace(
                        "autowired class interceptor count [%d]",
                        autowiredClassInterceptors.size());

                for (Class<?> classInterceptorClass : autowiredClassInterceptors) {
                    try {
                        LOGGER.trace(
                                "loading autowired class interceptor [%s]",
                                classInterceptorClass.getName());

                        Object object = ObjectSupport.createObject(classInterceptorClass);

                        classInterceptors.add((ClassInterceptor) object);

                        LOGGER.trace(
                                "autowired class interceptor [%s] loaded",
                                classInterceptorClass.getName());
                    } catch (EngineException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new EngineException(t);
                    }
                }

                initialized = true;
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Method to filter class interceptors
     *
     * @param classes classes
     */
    private static void filter(List<Class<?>> classes) {
        Set<Class<?>> filteredClasses = new LinkedHashSet<>(classes);

        ConcreteEngineContext.getInstance()
                .getConfiguration()
                .getOptional(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]",
                                    Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX,
                                    regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = filteredClasses.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    LOGGER.trace(
                                            "removing class interceptor [%s]", clazz.getName());

                                    iterator.remove();
                                }
                            }
                        });

        ConcreteEngineContext.getInstance()
                .getConfiguration()
                .getOptional(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]",
                                    Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX,
                                    regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            for (Class<?> clazz : classes) {
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    LOGGER.trace("adding class interceptor [%s]", clazz.getName());
                                    filteredClasses.add(clazz);
                                }
                            }
                        });

        classes.clear();
        classes.addAll(filteredClasses);
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static DefaultClassInterceptorRegistry getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final DefaultClassInterceptorRegistry SINGLETON =
                new DefaultClassInterceptorRegistry();
    }
}
