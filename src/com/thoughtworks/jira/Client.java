package com.thoughtworks.jira;

import com.thoughtworks.jira.util.AuthenticationReader;
import com.thoughtworks.jira.util.CfdCreator;
import com.thoughtworks.jira.util.Config;

public class Client {
	public static void main(String[] args) {
		Config config = new Config();
		
		new CfdCreator(new Jira(config, new AuthenticationReader()).retrieveStoriesWithChangelog(), config).generate();
	}
}
