/*
 * CoreController
 * version 0.1
 * This class is used to control fluentd and spark.
 * Not working
 */

package com.logax.server;

import java.io.IOException;

public class CoreController
{
	public static void startCore() throws IOException
	{
		//Runtime.getRuntime().exec("zkServer start");
		//Runtime.getRuntime().exec("kafka-server-start /usr/local/etc/kafka/server.properties");
		Runtime.getRuntime().exec("launchctl start com.logax.server.fluentd");
		//Runtime.getRuntime().exec("launchctl start com.logax.server.spark");
	}

	public static void stopCore() throws IOException
	{
		//Runtime.getRuntime().exec("kafka-server-stop");
		//Runtime.getRuntime().exec("zkServer stop");
		Runtime.getRuntime().exec("launchctl stop com.logax.server.fluentd");
		//Runtime.getRuntime().exec("launchctl stop com.logax.server.spark");
	}

	public static void sparkStart() throws IOException
	{
		//Runtime.getRuntime().exec("launchctl start com.logax.server.spark");
	}
	
	public static void sparkStop() throws IOException
	{
		//Runtime.getRuntime().exec("launchctl stop com.logax.server.spark");
	}
}
