package com.thoughtworks.jira;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.thoughtworks.jira.util.AuthenticationReader;
import com.thoughtworks.jira.util.Config;

public class Jira {
	private Config config;
	private AuthenticationReader authentication;
	private Expandos[] expandArr = new Expandos[] { Expandos.CHANGELOG };
	private List<Expandos> expand = Arrays.asList(expandArr);
	private List<Story> issues = new ArrayList<>();

	public Jira(Config config, AuthenticationReader authenticationReader) {
		this.config = config;
		this.authentication = authenticationReader;
	}

	public List<Story> retrieveStoriesWithChangelog() {
		logINFO("Starting to retrive stories.");
		
		try(JiraRestClient restClient = getRestClient()) {
			readChangelogFromEachIssueAndPopulateIssues(restClient.getIssueClient(), retrieveIssues(restClient));
		} catch (Exception e) {
			config.getLogger().log( Level.SEVERE, e.toString(), e);
		}
		
		return Collections.unmodifiableList(issues);
	}

	private Iterable<Issue> retrieveIssues(JiraRestClient restClient) {
		SearchResult claim = restClient.getSearchClient().searchJql(config.getJQL(), config.getPageSize(), 0, null).claim();
		return claim.getIssues();
	}

	private void logINFO(String msg) {
		config.getLogger().log(Level.INFO, msg);
	}
	
	private JiraRestClient getRestClient() {
		authentication.askForClientsLoginAndPassword();
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(config.getJiraUri(), authentication.getLogin(), authentication.getPassword());
	}

	private void readChangelogFromEachIssueAndPopulateIssues(IssueRestClient issueClient, Iterable<Issue> issues) {
		for (Issue issue : issues)
			retrieveChangelogAndPopulateIssues(issueClient.getIssue(issue.getKey(), expand).claim());
	}

	private void retrieveChangelogAndPopulateIssues(Issue issueWithExpando) {
		logINFO(String.format("Retrieving changelog for %s", issueWithExpando.getKey()));
		
		Story story = new Story(issueWithExpando, config);
		addStatusChangeToStory(issueWithExpando, story);
		
		issues.add(story);
	}

	private void addStatusChangeToStory(Issue issueWithExpando, Story story) {
		for (ChangelogGroup changelogGroup : issueWithExpando.getChangelog()) {
			for (ChangelogItem changelogItem : changelogGroup.getItems()) {
				if(!isStatusChange(changelogItem)) continue;
				
				story.addStatusChange(new StatusChange(changelogItem, changelogGroup.getCreated()));
			}
		}
	}

	private boolean isStatusChange(ChangelogItem changelogItem) {
		return "status".equalsIgnoreCase(changelogItem.getField());
	}
}
