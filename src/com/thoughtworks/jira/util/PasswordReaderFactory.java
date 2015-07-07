package com.thoughtworks.jira.util;

public class PasswordReaderFactory {
	public PasswordReader createPasswordReader() {
		if(System.console() == null)
			return new DialogReader();
		
		return new ConsolePasswordReader();
	}
}
