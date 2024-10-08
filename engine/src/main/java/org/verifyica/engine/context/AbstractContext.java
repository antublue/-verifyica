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

package org.verifyica.engine.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.verifyica.api.Configuration;
import org.verifyica.api.Context;

/** Class to implement AbstractContext */
public abstract class AbstractContext implements Context {

    private final Map<String, Object> map;

    /**
     * Constructor
     */
    protected AbstractContext() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public abstract Configuration configuration();

    @Override
    public Map<String, Object> map() {
        return map;
    }

    @Override
    public abstract Configuration getConfiguration();

    @Override
    public Map<String, Object> getMap() {
        return map;
    }
}
