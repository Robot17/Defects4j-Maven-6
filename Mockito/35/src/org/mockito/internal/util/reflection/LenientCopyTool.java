/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class LenientCopyTool {

    FieldCopier fieldCopier = new FieldCopier();

    public <T> void copyToMock(T from, T mock) {
        Class clazz = from.getClass();
        Class mockSuperClass = mock.getClass().getSuperclass();
        assert mockSuperClass == clazz 
            : "Classes must have the same type: class of the object from: " + clazz + ", mock super class: " + mockSuperClass;

        while (clazz != Object.class) {
            copyValues(from, mock, clazz);
            clazz = clazz.getSuperclass();
        }
    }

    private <T> void copyValues(T from, T mock, Class classFrom) {
        Field[] fields = classFrom.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            // ignore static fields
            Field field = fields[i];
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            AccessibilityChanger accessibilityChanger = new AccessibilityChanger();
            try {
                accessibilityChanger.enableAccess(field);
                fieldCopier.copyValue(from, mock, field);
            } catch (Throwable t) {
                //Ignore - be lenient - if some field cannot be copied then let's be it
            } finally {
                accessibilityChanger.safelyDisableAccess(field);
            }
        }
    }
}