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

package org.antublue.verifyica.test.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;

@Verifyica.Testable
public class LifecycleInheritanceTest {

    private static final int ARGUMENT_COUNT = 1;

    // Antipattern, but used for testing
    public static List<String> actual = new ArrayList<>();

    public abstract static class BaseClass {

        @Verifyica.Prepare
        public void baseClassPrepare(ClassContext classContext) {
            System.out.println("baseClassPrepare()");

            actual.add("baseClassPrepare");
        }

        @Verifyica.Prepare
        public void baseClassPrepare2(ClassContext classContext) {
            System.out.println("baseClassPrepare2()");

            actual.add("baseClassPrepare2");
        }

        @Verifyica.BeforeAll
        public void baseClassBeforeAll(ArgumentContext argumentContext) {
            System.out.println("baseClassBeforeAll()");

            actual.add("baseClassBeforeAll");
        }

        @Verifyica.BeforeAll
        public void baseClassBeforeAll2(ArgumentContext argumentContext) {
            System.out.println("baseClassBeforeAll2()");

            actual.add("baseClassBeforeAll2");
        }

        @Verifyica.BeforeEach
        public void baseClassBeforeEach(ArgumentContext argumentContext) {
            System.out.println("baseClassBeforeEach()");

            actual.add("baseClassBeforeEach");
        }

        @Verifyica.BeforeEach
        public void baseClassBeforeEach2(ArgumentContext argumentContext) {
            System.out.println("baseClassBeforeEach2()");

            actual.add("baseClassBeforeEach2");
        }

        @Verifyica.Test
        public void test(ArgumentContext argumentContext) {
            System.out.println("baseClassTest()");

            actual.add("baseClassTest");
        }

        @Verifyica.Test
        public void test3(ArgumentContext argumentContext) {
            System.out.println("baseClassTest3()");

            actual.add("baseClassTest3");
        }

        @Verifyica.AfterEach
        public void baseClassAfterEach2(ArgumentContext argumentContext) {
            System.out.println("baseClassAfterEach2()");

            actual.add("baseClassAfterEach2");
        }

        @Verifyica.AfterEach
        public void baseClassAfterEach(ArgumentContext argumentContext) {
            System.out.println("baseClassAfterEach()");

            actual.add("baseClassAfterEach");
        }

        @Verifyica.AfterAll
        public void baseClassAfterAll2(ArgumentContext argumentContext) {
            System.out.println("baseClassAfterAll2()");

            actual.add("baseClassAfterAll2");
        }

        @Verifyica.AfterAll
        public void baseClassAfterAll(ArgumentContext argumentContext) {
            System.out.println("baseClassAfterAll()");

            actual.add("baseClassAfterAll");
        }

        @Verifyica.Conclude
        public void baseClassConclude2(ClassContext classContext) {
            System.out.println("baseClassConclude2()");

            actual.add("baseClassConclude2");
        }

        @Verifyica.Conclude
        public void baseClassConclude(ClassContext classContext) {
            System.out.println("baseClassConclude()");

            actual.add("baseClassConclude");

            List<String> expected = new ArrayList<>();

            expected.add("baseClassPrepare");
            expected.add("baseClassPrepare2");
            expected.add("subClassPrepare");
            expected.add("subClassPrepare2");
            expected.add("baseClassBeforeAll");
            expected.add("baseClassBeforeAll2");
            expected.add("subClassBeforeAll");
            expected.add("subClassBeforeAll2");
            expected.add("baseClassBeforeEach");
            expected.add("baseClassBeforeEach2");
            expected.add("subClassBeforeEach");
            expected.add("subClassBeforeEach2");
            expected.add("baseClassTest");
            expected.add("subClassAfterEach2");
            expected.add("subClassAfterEach");
            expected.add("baseClassAfterEach2");
            expected.add("baseClassAfterEach");
            expected.add("baseClassBeforeEach");
            expected.add("baseClassBeforeEach2");
            expected.add("subClassBeforeEach");
            expected.add("subClassBeforeEach2");
            expected.add("subClassTest2");
            expected.add("subClassAfterEach2");
            expected.add("subClassAfterEach");
            expected.add("baseClassAfterEach2");
            expected.add("baseClassAfterEach");
            expected.add("baseClassBeforeEach");
            expected.add("baseClassBeforeEach2");
            expected.add("subClassBeforeEach");
            expected.add("subClassBeforeEach2");
            expected.add("baseClassTest3");
            expected.add("subClassAfterEach2");
            expected.add("subClassAfterEach");
            expected.add("baseClassAfterEach2");
            expected.add("baseClassAfterEach");
            expected.add("subClassAfterAll2");
            expected.add("subClassAfterAll");
            expected.add("baseClassAfterAll2");
            expected.add("baseClassAfterAll");
            expected.add("subClassConclude2");
            expected.add("subClassConclude");
            expected.add("baseClassConclude2");
            expected.add("baseClassConclude");

            assertThat(actual.size()).isEqualTo(expected.size());

            int pad = pad(expected);

            for (int i = 0; i < expected.size(); i++) {
                System.out.printf(
                        "expected [%-" + pad + "s] actual [%-" + pad + "s]%n",
                        expected.get(i),
                        actual.get(i));

                assertThat(actual.get(i)).isEqualTo(expected.get(i));
            }
        }
    }

    public static class SubClass extends BaseClass {

        @Verifyica.ArgumentSupplier
        public static Collection<Argument<String>> arguments() {
            Collection<Argument<String>> collection = new ArrayList<>();
            for (int i = 0; i < ARGUMENT_COUNT; i++) {
                collection.add(Argument.of("argument[" + i + "]", String.valueOf(i)));
            }
            return collection;
        }

        @Verifyica.Prepare
        public void subClassPrepare(ClassContext classContext) {
            System.out.println("subClassPrepare()");

            actual.add("subClassPrepare");
        }

        @Verifyica.Prepare
        public void subClassPrepare2(ClassContext classContext) {
            System.out.println("subClassPrepare2()");

            actual.add("subClassPrepare2");
        }

        @Verifyica.BeforeAll
        public void subClassBeforeAll(ArgumentContext argumentContext) {
            System.out.println("subClassBeforeAll()");

            actual.add("subClassBeforeAll");
        }

        @Verifyica.BeforeAll
        public void subClassBeforeAll2(ArgumentContext argumentContext) {
            System.out.println("subClassBeforeAll2()");

            actual.add("subClassBeforeAll2");
        }

        @Verifyica.BeforeEach
        public void subClassBeforeEach(ArgumentContext argumentContext) {
            System.out.println("subClassBeforeEach()");

            actual.add("subClassBeforeEach");
        }

        @Verifyica.BeforeEach
        public void subClassBeforeEach2(ArgumentContext argumentContext) {
            System.out.println("subClassBeforeEach2()");

            actual.add("subClassBeforeEach2");
        }

        @Verifyica.Test
        public void test2(ArgumentContext argumentContext) {
            System.out.println("subClassTest2()");

            actual.add("subClassTest2");
        }

        @Verifyica.AfterEach
        public void subClassAfterEach(ArgumentContext argumentContext) {
            System.out.println("subClassAfterEach()");

            actual.add("subClassAfterEach");
        }

        @Verifyica.AfterEach
        public void subClassAfterEach2(ArgumentContext argumentContext) {
            System.out.println("subClassAfterEach2()");

            actual.add("subClassAfterEach2");
        }

        @Verifyica.AfterAll
        public void subClassAfterAll2(ArgumentContext argumentContext) {
            System.out.println("subClassAfterAll2()");

            actual.add("subClassAfterAll2");
        }

        @Verifyica.AfterAll
        public void subClassAfterAll(ArgumentContext argumentContext) {
            System.out.println("subClassAfterAll()");

            actual.add("subClassAfterAll");
        }

        @Verifyica.Conclude
        public void subClassConclude2(ClassContext classContext) {
            System.out.println("subClassConclude2()");

            actual.add("subClassConclude2");
        }

        @Verifyica.Conclude
        public void subClassConclude(ClassContext classContext) {
            System.out.println("subClassConclude()");

            actual.add("subClassConclude");
        }
    }

    private static int pad(List<String> strings) {
        int pad = 0;

        for (String string : strings) {
            pad = Math.max(string.length(), pad);
        }

        return pad;
    }
}
