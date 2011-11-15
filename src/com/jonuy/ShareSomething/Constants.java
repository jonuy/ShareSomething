/**
 * @author Jonathan Uy - jonathan.j.uy@gmail.com
 * ShareSomething app - a Do Something code sample.
 * 
 * Constants.java
 * Non-instantiable class that provides static access to constant values
 */

package com.jonuy.ShareSomething;

public class Constants {
	// Private constructor that throws an assert to prevent instantiation
	private Constants() {
		throw new AssertionError();
	}
	
	// Identifier for Activity actions
	public static final int ACTIVITY_IMG_SELECT = 0;
	public static final int ACTIVITY_SHARE = 1;
}