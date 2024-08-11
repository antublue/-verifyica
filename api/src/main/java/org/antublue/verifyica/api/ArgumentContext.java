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

package org.antublue.verifyica.api;

/** Interface to implement ArgumentContext */
@SuppressWarnings({"deprecated", "unchecked"})
public interface ArgumentContext extends Context {

    /**
     * Returns the ClassContext
     *
     * @return the ClassContext
     */
    ClassContext getClassContext();

    /**
     * Returns the Argument
     *
     * @return the Argument
     */
    Argument<?> getTestArgument();

    /**
     * Returns the Argument, casting the Argument payload to the payload type
     *
     * @param type the Argument payload type
     * @return the Argument
     * @param <T> the Argument payload type
     */
    <T> Argument<T> getTestArgument(Class<T> type);
}
