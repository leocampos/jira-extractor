package com.thoughtworks.jira;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.thoughtworks.jira.util.AuthenticationReader;
import com.thoughtworks.jira.util.Config;

public class Jira {
	private static final int NUM_THREADS = 6;
	
	private Config config;
	private AuthenticationReader authentication;
	private Expandos[] expandArr = new Expandos[] { Expandos.CHANGELOG };
	private List<Expandos> expand = Arrays.asList(expandArr);
	private List<Story> issues = new ArrayList<>();
	private ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

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
	
	private JiraRestClient getRestClient() {
		authentication.askForClientsLoginAndPassword();
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(config.getJiraUri(), authentication.getLogin(), authentication.getPassword());
	}

	private Iterator<Issue> retrieveIssues(JiraRestClient restClient) {
		return new JiraIssuesIterator(config, restClient.getSearchClient());
	}
	
	private void readChangelogFromEachIssueAndPopulateIssues(IssueRestClient issueClient, Iterator<Issue> issueIterator) {
		while(issueIterator.hasNext())
			retrieveChangeLogAndPopulateIssuesAsync(issueClient, issueIterator.next());
		
		awaitTermination();
	}

	private void retrieveChangeLogAndPopulateIssuesAsync(IssueRestClient issueClient, Issue issue) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				retrieveChangelogAndPopulateIssues(issueClient.getIssue(issue.getKey(), expand).claim());
			}
		});
	}
	
	private void retrieveChangelogAndPopulateIssues(Issue issueWithExpando) {
		logINFO(String.format("Retrieving changelog for %s", issueWithExpando.getKey()));
		
		Story story = new Story(issueWithExpando, config);
		addStatusChangeToStory(issueWithExpando, story);
		
		issues.add(story);
	}

	private void awaitTermination() {
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			logWarning("Some items didn't finish loading");
		}
	}

	private void addStatusChangeToStory(Issue issueWithExpando, Story story) {
		for (ChangelogGroup changelogGroup : issueWithExpando.getChangelog()) {
			addStatusChangeIfPresent(story, changelogGroup);
		}
	}

	private void addStatusChangeIfPresent(Story story, ChangelogGroup changelogGroup) {
		for (ChangelogItem changelogItem : changelogGroup.getItems()) {
			if(isNotStatusChange(changelogItem)) continue;
			
			story.addStatusChange(new StatusChange(changelogItem, changelogGroup.getCreated()));
		}
	}

	private boolean isNotStatusChange(ChangelogItem changelogItem) {
		return !isStatusChange(changelogItem);
	}

	private boolean isStatusChange(ChangelogItem changelogItem) {
		return "status".equalsIgnoreCase(changelogItem.getField());
	}
	
	private void logINFO(String msg) {
		config.getLogger().log(Level.INFO, msg);
	}
	
	private void logWarning(String msg) {
		config.getLogger().log(Level.WARNING, msg);
	}
}
