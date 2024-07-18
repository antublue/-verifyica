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

package org.antublue.verifyica.test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
public class AutoCloseableTest implements AutoCloseable {

    @Verifyica.ArgumentSupplier
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            collection.add(Argument.ofString("String " + i));
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
        System.out.println(format("beforeAll(%s)", argumentContext.getArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getArgument()).isNotNull();
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        System.out.println(format("beforeEach(%s)", argumentContext.getArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getArgument()).isNotNull();
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("test1(%s)", argumentContext.getArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getArgument()).isNotNull();
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        System.out.println(format("afterEach(%s)", argumentContext.getArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getArgument()).isNotNull();
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        System.out.println(format("afterAll(%s)", argumentContext.getArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getArgument()).isNotNull();
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println(format("conclude()"));

        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();
    }

    @Override
    public void close() {
        System.out.println("close()");
    }
}
