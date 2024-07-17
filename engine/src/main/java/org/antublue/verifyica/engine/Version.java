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

package org.antublue.verifyica.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Class to implement Version */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Version {

    private static final String ENGINE_PROPERTIES = "/engine.properties";
    private static final String VERSION = "version";
    private static final String UNKNOWN = "unknown";

    /** Constructor */
    private Version() {
        // DO NOTHING
    }

    /**
     * Method to get the version
     *
     * @return the version
     */
    public static String version() {
        String value = UNKNOWN;

        try (InputStream inputStream = Version.class.getResourceAsStream(ENGINE_PROPERTIES)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(VERSION).trim();
            }
        } catch (IOException e) {
            // DO NOTHING
        }

        return value;
    }
}
