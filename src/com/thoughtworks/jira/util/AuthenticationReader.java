package com.thoughtworks.jira.util;

import java.util.Scanner;

public class AuthenticationReader {
	private String login, password;
	private PasswordReaderFactory passReaderFactory = new PasswordReaderFactory();
	
	public void askForClientsLoginAndPassword() {
		System.out.println("Entre seu Login:");
		try(Scanner scanner = new Scanner(System.in)) {
			login = scanner.nextLine();
		} catch (Exception e) {
			e.getMessage();
		}

		System.out.println("Entre seu password:");
		PasswordReader passwordReader = passReaderFactory.createPasswordReader();
		password = new String(passwordReader.readPassword());
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}
}
