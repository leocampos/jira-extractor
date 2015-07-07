package com.thoughtworks.jira;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.thoughtworks.jira.util.AuthenticationReader;
import com.thoughtworks.jira.util.CfdCreator;
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
	
	private JiraRestClient getRestClient() {
		authentication.askForClientsLoginAndPassword();
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(config.getJiraUri(), authentication.getLogin(), authentication.getPassword());
	}

	public static void main(String[] args) {
		Config config = new Config();
		Jira jira = new Jira(config, new AuthenticationReader());
		
		List<Story> stories = jira.retrieveStoriesWithChangelog();
		
		System.out.println(new CfdCreator(stories, config.getStatusList()).generate());
	}

	private List<Story> retrieveStoriesWithChangelog() {
		try(JiraRestClient restClient = getRestClient()) {
			SearchResult claim = restClient.getSearchClient().searchJql(config.getJQL()).claim();
			readChangelogFromEachIssue(restClient.getIssueClient(), claim.getIssues());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Collections.unmodifiableList(issues);
	}

	private void readChangelogFromEachIssue(IssueRestClient issueClient, Iterable<Issue> issues) {
		for (Issue issue : issues) {
			Promise<Issue> promissedIssueWithExpando = issueClient.getIssue(issue.getKey(), expand);
			Issue issueWithExpando = promissedIssueWithExpando.claim();
			retrieveChangelog(issueWithExpando);
		}
	}

	private void retrieveChangelog(Issue issueWithExpando) {
		Story data = new Story(issueWithExpando.getKey(), config);
		data.setCreationDate(issueWithExpando.getCreationDate());
		issues.add(data);
		
		for (ChangelogGroup changelogGroup : issueWithExpando.getChangelog()) {
		    for (ChangelogItem changelogItem : changelogGroup.getItems()) {
		    	if(!"status".equals(changelogItem.getField())) continue;
		    	
		    	data.addStatusChange(new StatusChange(changelogItem, changelogGroup.getCreated()));
		    }
		}
		
		data.restructureChangelog();
	}
}
