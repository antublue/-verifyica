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

import java.util.Objects;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;

/** Class to implement ConcreteEngineContext */
public class ConcreteEngineContext extends AbstractContext implements EngineContext {

    private final String version;

    /**
     * Constructor
     *
     * @param configuration configuration
     * @param version version
     */
    public ConcreteEngineContext(Configuration configuration, String version) {
        super(configuration);

        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "ConcreteEngineContext{" + "version='" + version + '\'' + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        ConcreteEngineContext that = (ConcreteEngineContext) object;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), version);
    }
}
