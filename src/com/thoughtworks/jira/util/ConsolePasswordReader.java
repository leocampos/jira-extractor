package com.thoughtworks.jira.util;

import java.io.Console;

public class ConsolePasswordReader implements PasswordReader {

	@Override
	public String readPassword() {
		Console console = System.console();
		return new String(console.readPassword());
	}

}
