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

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
public class ConcreteTest2 extends AbstractTest1 {

    @Verifyica.ArgumentSupplier
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        collection.add(Argument.ofString("String 0"));
        collection.add(Argument.ofString("String 1"));
        collection.add(Argument.ofString("String 3"));

        return collection;
    }
}
