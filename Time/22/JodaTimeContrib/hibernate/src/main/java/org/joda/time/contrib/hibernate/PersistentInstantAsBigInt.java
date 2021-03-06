/*
 *  Copyright 2001-2009 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time.contrib.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.joda.time.Instant;

/**
 * Persist {@link org.joda.time.Instant} via hibernate as a BIGINT.
 * 
 * @author Martin Grove (marting@optrak.co.uk))
 */
public class PersistentInstantAsBigInt implements EnhancedUserType, Serializable {

    public static final PersistentInstantAsBigInt INSTANCE = new PersistentInstantAsBigInt();

    private static final int[] SQL_TYPES = new int[] { Types.BIGINT };

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Class returnedClass() {
        return Instant.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        Instant ix = (Instant) x;
        Instant iy = (Instant) y;
        return ix.equals(iy);
    }

    public int hashCode(Object object) throws HibernateException {
        return object.hashCode();
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object object) throws HibernateException, SQLException {
        return nullSafeGet(resultSet, names[0]);
    }

    public Object nullSafeGet(ResultSet resultSet, String name) throws HibernateException, SQLException {
        Object value = Hibernate.LONG.nullSafeGet(resultSet, name);
        if (value == null) {
            return null;
        }
        return new Instant(value);
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            Hibernate.LONG.nullSafeSet(preparedStatement, null, index);
        } else {
            Hibernate.LONG.nullSafeSet(preparedStatement, new Long(((Instant) value).getMillis()), index);
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object assemble(Serializable serializable, Object value) throws HibernateException {
        return serializable;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    // __________ EnhancedUserType ____________________

    public String objectToSQLString(Object object) {
        throw new UnsupportedOperationException();
    }

    public String toXMLString(Object object) {
        return object.toString();
    }

    public Object fromXMLString(String string) {
        return new Instant(string);
    }

}
