package com.ebuddy.nokia.s40;

import org.kohsuke.args4j.Option;

/**
 * Eugen Martynov
 */
public class Arguments {
	@Option(name = "-h", aliases = {"--host"}, required = true, usage = "signing server host name")
	String host;

	@Option(name = "-u", aliases = {"--user"}, required = true, usage = "signing username")
	String username;

	@Option(name = "-p", aliases = {"--password"}, required = true, usage = "signing password")
	String password;

	@Option(name = "-jad", required = true, usage = "jad file")
	String jad;

	@Option(name = "-jar", required = true, usage = "jar file")
	String jar;
}
