/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.matchers;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;


public class InstanceOf extends ArgumentMatcher<Object> {

    private static final long serialVersionUID = 517358915876138366L;
    private final Class<?> clazz;

    public InstanceOf(Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean matches(Object actual) {
        return (actual != null) && clazz.isAssignableFrom(actual.getClass());
    }

    public void describeTo(Description description) {
        description.appendText("isA(" + clazz.getName() + ")");
    }
}
