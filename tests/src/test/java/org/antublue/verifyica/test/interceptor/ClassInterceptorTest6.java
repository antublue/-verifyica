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

package org.antublue.verifyica.test.interceptor;

import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;

/** Example test */
public class ClassInterceptorTest6 {

    @Verifyica.ClassInterceptorSupplier
    public static Object classInterceptors() {
        return ClassInterceptor.NOOP;
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s test()%n", argumentContext.getTestArgument().getPayload());
    }
}
