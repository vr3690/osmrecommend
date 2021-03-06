package com.osmrecommend.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

// courtesy of: http://backtothefront.net/2011/storing-sets-keyvalue-pairs-single-db-column-hibernate-postgresql-hstore-type/
public class HstoreUserType implements UserType, Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -4364515984197057578L;

	public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return cached;
    }

    public Object deepCopy(Object o) throws HibernateException {
        // It's not a true deep copy, but we store only String instances, and they
        // are immutable, so it should be OK
    	Object2ObjectOpenHashMap<String, String> m = (Object2ObjectOpenHashMap<String, String>) o;
        return new Object2ObjectOpenHashMap<Object, Object>(m);
    }

    public Serializable disassemble(Object o) throws HibernateException {
        return (Serializable) o;
    }

    public boolean equals(Object o1, Object o2) throws HibernateException {
    	Object2ObjectOpenHashMap<String, String> m1 = (Object2ObjectOpenHashMap<String, String>) o1;
    	Object2ObjectOpenHashMap<String, String> m2 = (Object2ObjectOpenHashMap<String, String>) o2;
        return m1.equals(m2);
    }

    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] strings, SessionImplementor sessionImplementor, Object o) throws HibernateException, SQLException {
        String col = strings[0];
        String val = "";
        if(null !=  resultSet.getString(col)) {
        		val = resultSet.getString(col);
        }
        return HstoreHelper.toMap(val);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SessionImplementor sessionImplementor) throws HibernateException, SQLException {
        String s = HstoreHelper.toString((Object2ObjectOpenHashMap<String, String>) o);
        preparedStatement.setObject(i, s, Types.OTHER);
    }

    public boolean isMutable() {
        return true;
    }

    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }

    public Class<Object2ObjectOpenHashMap> returnedClass() {
        return Object2ObjectOpenHashMap.class;
    }

    public int[] sqlTypes() {
        /*
         * i'm not sure what value should be used here, but it works, AFAIK only
         * length of this array matters, as it is a column span (1 in our case)
         */
        return new int[] { Types.VARCHAR  };
    }
}