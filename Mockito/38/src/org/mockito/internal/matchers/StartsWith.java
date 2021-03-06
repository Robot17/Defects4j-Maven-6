/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.matchers;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;


public class StartsWith extends ArgumentMatcher<String> {

    private final String prefix;

    public StartsWith(String prefix) {
        this.prefix = prefix;
    }

    public boolean matches(Object actual) {
        return actual != null && ((String) actual).startsWith(prefix);
    }

    public void describeTo(Description description) {
        description.appendText("startsWith(\"" + prefix + "\")");
    }
}
