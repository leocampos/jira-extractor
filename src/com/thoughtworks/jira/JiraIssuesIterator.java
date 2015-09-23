package com.thoughtworks.jira;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import com.thoughtworks.jira.util.Config;

public class JiraIssuesIterator implements Iterator<Issue> {
	private SearchRestClient searchClient;
    private int pageSize;
    private String jql;
    private Set<String> fields;

    private int actualPage = 0;
    private int issueTotal;
    private Iterator<Issue> iterator;

    private SearchResult searchResult;
    
    public JiraIssuesIterator(Config config, SearchRestClient searchClient) {
		this.searchClient = searchClient;
		this.jql = config.getJQL();
		this.pageSize = config.getPageSize();
		this.fields = config.getFields();
    }


    @Override
    public boolean hasNext() {
    	setupIfFirstTime();
    	
    	if(hasNextInActualPage()) return true;
    	if(isLastPage()) return false;
    	
    	turnPage();
    	search();
    	
        return hasNext();
    }

	private void turnPage() {
		actualPage++;
	}

	private boolean isLastPage() {
		return lastIndexForPage() > issueTotal;
	}

	private int lastIndexForPage() {
		return (actualPage + 1) * pageSize;
	}

	private void setupIfFirstTime() {
		if(searchResult == null) {
    		search();
    		issueTotal = searchResult.getTotal();
    	}
	}

    @Override
    public Issue next() {
    	if(hasNext())
    		return iterator.next();
    	
        throw new NoSuchElementException();
    }


	private boolean hasNextInActualPage() {
		return iterator.hasNext();
	}

    private void search() {
        Promise<SearchResult> searchJql = searchClient.searchJql(jql, pageSize, actualPage * pageSize, fields);
		searchResult = searchJql.claim();
        iterator = searchResult.getIssues().iterator();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
