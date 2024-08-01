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

package org.antublue.verifyica.test.extension;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.extension.ClassExtension;

/** Example test */
public class ClassExtensionTest1 {

    @Verifyica.ClassExtensionSupplier
    public static Collection<ClassExtension> classExtensions() {
        Collection<ClassExtension> collection = new ArrayList<>();

        collection.add(new ExampleClassExtension1());
        collection.add(new ExampleClassExtension2());

        return collection;
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) throws Throwable {
        System.out.println(format("  %s prepare()", classContext.getTestClass().getName()));
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s beforeAll()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s beforeEach()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Test
    @Verifyica.Order(order = 0)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test1()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test2()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test3()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s afterEach()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s afterAll()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) throws Throwable {
        System.out.println(format("  %s conclude()", classContext.getTestClass().getName()));
    }
}
