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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
public class EngineInterceptorTest {

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("test(%s)", argumentContext.getTestArgument().getPayload()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getObjectStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(
                        argumentContext.getClassContext().getEngineContext()
                                == argumentContext.getClassContext().getEngineContext())
                .isTrue();

        // Validate that the extension added a global String to the EngineContext Store
        assertThat(
                        (Object)
                                argumentContext
                                        .getClassContext()
                                        .getEngineContext()
                                        .getObjectStore()
                                        .get(ExampleEngineInterceptor.KEY))
                .isNotNull();

        assertThat(
                        argumentContext
                                .getClassContext()
                                .getEngineContext()
                                .getObjectStore()
                                .get(ExampleEngineInterceptor.KEY, String.class))
                .isEqualTo(ExampleEngineInterceptor.VALUE);
    }
}
