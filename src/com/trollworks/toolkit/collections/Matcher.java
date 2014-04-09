package com.trollworks.toolkit.collections;

/** Used to match objects. */
public interface Matcher<T> {
	/**
	 * @param obj The object to test.
	 * @return <code>true</code> if the object matched the criteria.
	 */
	boolean matches(T obj);
}
