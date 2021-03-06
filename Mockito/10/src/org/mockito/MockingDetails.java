/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito;

import java.util.Collection;

import org.mockito.invocation.Invocation;

/**
 * Provides mocking information.
 * For example, you can identify whether a particular object is either a mock or a spy.
 *
 * @since 1.9.5
 */
@Incubating
public interface MockingDetails {
    
    /**
     * Informs if the object is a mock.
     * @return true if the object is a mock or a spy.
     *
     * @since 1.9.5
     */
    boolean isMock();

    /**
     * Informs if the object is a spy.
     * @return true if the object is a spy.
     *
     * @since 1.9.5
     */
    boolean isSpy();
    
    /**
     * Provides a collection of methods indicating the invocations of the object
     * @return collection of Invocation representing the invocations 
     * for the object.
     *
     * @since 1.10.0
     */
    Collection<Invocation> getInvocations();
}