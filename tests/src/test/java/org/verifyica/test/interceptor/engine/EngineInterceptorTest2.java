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

package org.verifyica.test.interceptor.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.api.interceptor.ClassDefinition;
import org.verifyica.api.interceptor.EngineInterceptor;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.api.interceptor.MethodDefinition;

public class EngineInterceptorTest2 implements EngineInterceptor {

    @Verifyica.Autowired
    public static class ReverseTestMethodOrder implements EngineInterceptor {

        @Override
        public Predicate<ClassDefinition> onTestDiscoveryPredicate() {
            return classDefinition -> classDefinition.getTestClass() == EngineInterceptorTest2.class;
        }

        @Override
        public void onTestDiscovery(
                EngineInterceptorContext engineInterceptorContext, ClassDefinition classDefinition) {
            assertThat(classDefinition.getTestClass()).isEqualTo(EngineInterceptorTest2.class);

            System.out.println("reversing test method order");

            Set<MethodDefinition> methodDefinitions = classDefinition.getTestMethodDefinitions();
            ArrayList<MethodDefinition> list = new ArrayList<>(methodDefinitions);
            Collections.reverse(list);
            methodDefinitions.clear();
            methodDefinitions.addAll(list);
        }
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test(order = 1)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 2)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 3)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 4)
    public void test4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());
    }
}
