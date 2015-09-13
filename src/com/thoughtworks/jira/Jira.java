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
		config.getLogger().log(Level.INFO, "Starting to retrive stories.");
		
		try(JiraRestClient restClient = getRestClient()) {
			SearchResult claim = restClient.getSearchClient().searchJql(config.getJQL(), 1000, 0, null).claim();
			readChangelogFromEachIssue(restClient.getIssueClient(), claim.getIssues());
		} catch (Exception e) {
			config.getLogger().log( Level.SEVERE, e.toString(), e);
		}
		
		return Collections.unmodifiableList(issues);
	}
	
	private JiraRestClient getRestClient() {
		authentication.askForClientsLoginAndPassword();
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(config.getJiraUri(), authentication.getLogin(), authentication.getPassword());
	}

	private void readChangelogFromEachIssue(IssueRestClient issueClient, Iterable<Issue> issues) {
		for (Issue issue : issues)
			retrieveChangelog(issueClient.getIssue(issue.getKey(), expand).claim());
	}

	private void retrieveChangelog(Issue issueWithExpando) {
		config.getLogger().log(Level.INFO, String.format("Retrieving changelog for %s", issueWithExpando.getKey()));
		
		Story data = new Story(issueWithExpando.getKey(), config);
		data.setCreationDate(issueWithExpando.getCreationDate());
		issues.add(data);
		
		for (ChangelogGroup changelogGroup : issueWithExpando.getChangelog()) {
		    for (ChangelogItem changelogItem : changelogGroup.getItems()) {
		    	if(!"status".equals(changelogItem.getField())) continue;
		    	
		    	data.addStatusChange(new StatusChange(changelogItem, changelogGroup.getCreated()));
		    }
		}
	}
}
