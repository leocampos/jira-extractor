package com.thoughtworks.jira;

import java.util.List;

import com.thoughtworks.jira.util.AuthenticationReader;
import com.thoughtworks.jira.util.CfdCreator;
import com.thoughtworks.jira.util.Config;

public class Client {
	public static void main(String[] args) {
		Config config = new Config();
		
		List<Story> stories = new Jira(config, new AuthenticationReader()).retrieveStoriesWithChangelog();
		
		System.out.println(new CfdCreator(stories, config.getStatusList()).generate());
	}
}
