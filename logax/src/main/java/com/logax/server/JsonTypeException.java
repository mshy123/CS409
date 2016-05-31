/*
 * JsonTypeException
 * version 1.0
 * This class is Exception class.
 * When the error occur in the SafeJson, return this Exception
 */

package com.logax.server;

public class JsonTypeException extends Exception
{
	public JsonTypeException(String msg)
	{
		super(msg);
	}
}
