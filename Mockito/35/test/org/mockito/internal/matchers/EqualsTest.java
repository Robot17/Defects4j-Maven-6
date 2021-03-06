/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.matchers;

import org.junit.Test;
import org.mockitoutil.TestBase;


public class EqualsTest extends TestBase {
    
    public void shouldBeEqual() {
        assertEquals(new Equals(null), new Equals(null));
        assertEquals(new Equals(new Integer(2)), new Equals(new Integer(2)));
        assertFalse(new Equals(null).equals(null));
        assertFalse(new Equals(null).equals("Test"));
        try {
            new Equals(null).hashCode();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }
    
    @Test
    public void shouldDescribeWithExtraTypeInfo() throws Exception {
        String descStr = describe(new Equals(100).withExtraTypeInfo());
        
        assertEquals("(Integer) 100", descStr);
    }

    @Test
    public void shouldDescribeWithExtraTypeInfoOfLong() throws Exception {
        String descStr = describe(new Equals(100L).withExtraTypeInfo());
        
        assertEquals("(Long) 100", descStr);
    }
    
    @Test
    public void shouldAppendQuotingForString() {
        String descStr = describe(new Equals("str"));
        
        assertEquals("\"str\"", descStr);
    }

    @Test
    public void shouldAppendQuotingForChar() {
        String descStr = describe(new Equals('s'));
        
        assertEquals("'s'", descStr);
    }
    
    @Test
    public void shouldDescribeUsingToString() {
        String descStr = describe(new Equals(100));
        
        assertEquals("100", descStr);
    }

    @Test
    public void shouldDescribeNull() {
        String descStr = describe(new Equals(null));
        
        assertEquals("null", descStr);
    }
    
    @Test
    public void shouldMatchTypes() throws Exception {
        //when
        ContainsExtraTypeInformation equals = new Equals(10);
        
        //then
        assertTrue(equals.typeMatches(10));
        assertFalse(equals.typeMatches(10L));
    }
    
    @Test
    public void shouldMatchTypesSafelyWhenActualIsNull() throws Exception {
        //when
        ContainsExtraTypeInformation equals = new Equals(null);
        
        //then
        assertFalse(equals.typeMatches(10));
    }

    @Test
    public void shouldMatchTypesSafelyWhenGivenIsNull() throws Exception {
        //when
        ContainsExtraTypeInformation equals = new Equals(10);
        
        //then
        assertFalse(equals.typeMatches(null));
    }
}
