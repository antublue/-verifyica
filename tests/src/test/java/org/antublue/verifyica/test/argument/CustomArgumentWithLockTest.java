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

package org.antublue.verifyica.test.argument;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.engine.support.RandomSupport;

/** Example test */
public class CustomArgumentWithLockTest {

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Collection<CustomArgument> arguments() {
        Lock lock = new ReentrantLock(true);
        Collection<CustomArgument> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(new CustomArgument(lock, "String " + i));
        }

        return collection;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.println(format("prepare()"));

        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        argumentContext.getTestArgument(CustomArgument.class).getPayload().lock();

        System.out.println(format("beforeAll(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        System.out.println(format("beforeEach(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("test1(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        Thread.sleep(RandomSupport.randomInt(0, 1000));
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("test2(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        Thread.sleep(RandomSupport.randomInt(0, 1000));
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        System.out.println(format("afterEach(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("afterAll(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        argumentContext.getTestArgument(CustomArgument.class).getPayload().unlock();
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println(format("conclude()"));

        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();
    }

    public static class CustomArgument implements Argument<CustomArgument> {

        private final Lock lock;
        private final String value;

        public CustomArgument(Lock lock, String value) {
            this.lock = lock;
            this.value = value;
        }

        @Override
        public String getName() {
            return value;
        }

        @Override
        public CustomArgument getPayload() {
            return this;
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
