/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.tasks.testing;

import org.gradle.api.Incubating;
import org.gradle.api.internal.tasks.testing.DefaultTestFailure;
import org.gradle.api.internal.tasks.testing.DefaultTestFailureDetails;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Describes a test failure. Contains a reference to the failure and some structural information retrieved by the test worker.
 *
 * @since 7.5
 */
@Incubating
public abstract class TestFailure {

    /**
     * Returns the raw failure.
     *
     * @return the raw failure
     */
    public abstract Throwable getRawFailure();

    /**
     * Returns structural information about the failure.
     *
     * @return the failure structure
     */
    public abstract TestFailureDetails getDetails();

    /**
     * Creates a new TestFailure instance from an assertion failure.
     *
     * @param failure the assertion failure
     * @param expected the expected value for the failure; can be {@code null}
     * @param actual the actual value for the failure; can be {@code null}
     * @return the new instance
     */
    public static TestFailure fromTestAssertionFailure(Throwable failure, String expected, String actual) {
        DefaultTestFailureDetails details = new DefaultTestFailureDetails(failure.getMessage(), failure.getClass().getName(), stacktraceOf(failure), true, expected, actual);
        return new DefaultTestFailure(failure, details);
    }

    /**
     * Creates a new TestFailure instance from a test framework failure.
     *
     * @param failure the failure
     * @return the new instance
     */
    public static TestFailure fromTestFrameworkFailure(Throwable failure) {
        DefaultTestFailureDetails details = new DefaultTestFailureDetails(messageOf(failure), failure.getClass().getName(), stacktraceOf(failure), false, null, null);
        return new DefaultTestFailure(failure, details);
    }

    private static String messageOf(Throwable throwable) {
        try {
            return throwable.getMessage();
        } catch (Throwable t) {
            return String.format("Could not determine failure message for exception of type %s: %s", throwable.getClass().getName(), t);
        }
    }

    private static String stacktraceOf(Throwable throwable) {
        try {
            StringWriter out = new StringWriter();
            PrintWriter wrt = new PrintWriter(out);
            throwable.printStackTrace(wrt);
            return out.toString();
        } catch (Exception t) {
            return stacktraceOf(t);
        }
    }
}
