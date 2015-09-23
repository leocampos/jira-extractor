package com.thoughtworks.jira;

import java.util.List;

import com.thoughtworks.jira.util.AuthenticationReader;
import com.thoughtworks.jira.util.CfdCreator;
import com.thoughtworks.jira.util.Config;

public class Client {
	private Jira jiraClient;
	private CfdCreator cfdCreator;
	
	public static void main(String[] args) {
		new Client(configCreator(args)).generateCSV();
	}

	private static Config configCreator(String[] args) {
		return args.length > 0 ? new Config(args[0]): new Config();
	}

	public Client(Config config) {
		this(new Jira(config, new AuthenticationReader()), new CfdCreator(config));
	}
	
	public Client(Jira jiraClient, CfdCreator cfdCreator) {
		this.jiraClient = jiraClient;
		this.cfdCreator = cfdCreator;
	}
	
	public void generateCSV() {
		generateCSV(retrieveStoriesWithChangelog());
	}

	public List<Story> retrieveStoriesWithChangelog() {
		return jiraClient.retrieveStoriesWithChangelog();
	}

	private void generateCSV(List<Story> storiesWithStatusChanges) {
		cfdCreator.generateCSVAndWriteToFile(storiesWithStatusChanges);
	}
}
