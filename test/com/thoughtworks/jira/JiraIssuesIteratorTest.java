package com.thoughtworks.jira;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import com.thoughtworks.jira.util.Config;

public class JiraIssuesIteratorTest {
	@Mock
	private SearchRestClient mockSearchClient;
	
	@Mock
	private Config mockConfig;
	
	@Mock
	private Promise<SearchResult> mockPromise;
	@Mock
	private Promise<SearchResult> mockPromisePageTwo;

	@Mock
	private SearchResult mockSearchResult;

	@Mock
	private SearchResult mockSearchResultPageTwo;

	@Mock
	private Iterator<Issue> mockIterator;
	
	private static final String JQL = "JQL";
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	private void prepareConfig(int pageSize) {
		Mockito.when(mockConfig.getJQL()).thenReturn(JQL);
		Mockito.when(mockConfig.getPageSize()).thenReturn(pageSize);
		Mockito.when(mockConfig.getFields()).thenReturn(null);
		Mockito.stub(mockConfig.getMaxNumberOfItems()).toReturn(null);
	}
	
	@Test
	public void mandatoryFieldsShouldBeAddedIfAnyIfPointedOut() throws Exception {
		Mockito.stub(mockConfig.getFields()).toReturn(new HashSet<String>(Arrays.asList(new String[]{"A"})));
		JiraIssuesIterator issuesIterator = new JiraIssuesIterator(mockConfig, mockSearchClient);
		
		Set<String> expectedFields = new HashSet<>(Arrays.asList(new String[]{"A","summary","issuetype","created","updated","project","status"}));
		
		Assert.assertTrue(issuesIterator.getFields().containsAll(expectedFields));
	}
	
	@Test
	public void mandatoryFieldsShouldNotBeAddedIfSpecialAllIsSet() throws Exception {
		assertSpecialFieldDoesNotIncludeMandatoryFields("*all");
	}
	
	@Test
	public void mandatoryFieldsShouldNotBeAddedIfSpecialNavigableIsSet() throws Exception {
		assertSpecialFieldDoesNotIncludeMandatoryFields("*navigable");
	}

	private void assertSpecialFieldDoesNotIncludeMandatoryFields(String specialField) {
		Mockito.stub(mockConfig.getFields()).toReturn(new HashSet<String>(Arrays.asList(new String[]{specialField})));
		JiraIssuesIterator issuesIterator = new JiraIssuesIterator(mockConfig, mockSearchClient);
		
		Set<String> expectedFields = new HashSet<>(Arrays.asList(new String[]{specialField}));
		
		Assert.assertTrue(issuesIterator.getFields().containsAll(expectedFields));
		Assert.assertTrue(issuesIterator.getFields().size() == 1);
	}

	@Test
	public void hasNextShouldReturnFalseIfTotalIsZero() {
		int pageSize = 10;
		int actualPage = 0;
		
		prepareConfig(pageSize);
		JiraIssuesIterator iterator = new JiraIssuesIterator(mockConfig, mockSearchClient);
		
		preparePageOne(pageSize, actualPage);
		
		prepareSearchResult(createList(0), 0);
		
		Assert.assertFalse(iterator.hasNext());
	}

	private void preparePageOne(int pageSize, int actualPage) {
		Mockito.when(mockSearchClient.searchJql(JQL, pageSize, actualPage * pageSize, null)).thenReturn(mockPromise);
		Mockito.when(mockPromise.claim()).thenReturn(mockSearchResult);
	}
	
	@Test
	public void maxNumberOfItemsShouldLimitIteration() throws Exception {
		Mockito.stub(mockConfig.getMaxNumberOfItems()).toReturn(5);
		JiraIssuesIterator iterator = new JiraIssuesIterator(mockConfig, mockSearchClient);
		iterator.setSearchResult(mockSearchResult);
		iterator.setIterator(mockIterator);
		Mockito.stub(mockIterator.hasNext()).toReturn(true);
		Mockito.stub(mockIterator.next()).toReturn(null);
		
		for (int i = 0; i < 5; i++)
			iterator.next();
		
		Assert.assertFalse(iterator.hasNext());
	}
	
	@Test
	public void hasNextShouldReturnTrueIfItIsTheEleventhItemInATotalOf19AndPaginationOfTen() {
		int pageSize = 10;
		int actualPage = 0;
		
		prepareConfig(pageSize);
		JiraIssuesIterator iterator = new JiraIssuesIterator(mockConfig, mockSearchClient);
		
		preparePageOne(pageSize, actualPage);
		prepareSearchResult(createList(pageSize), 19);
		
		Mockito.when(mockSearchClient.searchJql(JQL, pageSize, pageSize, null)).thenReturn(mockPromisePageTwo);
		Mockito.when(mockPromisePageTwo.claim()).thenReturn(mockSearchResultPageTwo);
		
		Mockito.when(mockSearchResultPageTwo.getTotal()).thenReturn(19);
		Mockito.when(mockSearchResultPageTwo.getIssues()).thenReturn(createList(1));
		
		for (int i = 0; i < pageSize; i++)
			iterator.next();
		
		Assert.assertTrue(iterator.hasNext());
	}
	
	@Test
	public void hasNextShouldReturnTrueIfTotalIsTenAndThisIsTenthIteration() {
		int pageSize = 10;
		int actualPage = 0;
		
		prepareConfig(pageSize);
		JiraIssuesIterator iterator = new JiraIssuesIterator(mockConfig, mockSearchClient);
		
		preparePageOne(pageSize, actualPage);
		
		ArrayList<Issue> list = createList(10);
		prepareSearchResult(list, list.size());
		
		for (int i = 0; i < 9; i++)
			list.iterator().next();
		
		Assert.assertTrue(iterator.hasNext());
	}

	private void prepareSearchResult(List<Issue> list, int total) {
		Mockito.when(mockSearchResult.getTotal()).thenReturn(total);
		Mockito.when(mockSearchResult.getIssues()).thenReturn(list);
	}

	private ArrayList<Issue> createList(int size) {
		ArrayList<Issue> list = new ArrayList<Issue>(size);
		for(int i = 0; i < size; i++)
			list.add(null);
		
		return list;
	}

}
