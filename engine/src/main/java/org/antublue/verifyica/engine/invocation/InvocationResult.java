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

package org.antublue.verifyica.engine.invocation;

public class InvocationResult {

    public enum Type {
        SUCCESS,
        FAILURE,
        SKIPPED
    }

    private final Type type;

    private InvocationResult(Type type) {
        this.type = type;
    }

    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }

    public boolean isFailure() {
        return type == Type.FAILURE;
    }

    public boolean isSkipped() {
        return type == Type.SKIPPED;
    }

    public static InvocationResult create(Type type) {
        return new InvocationResult(type);
    }
}
