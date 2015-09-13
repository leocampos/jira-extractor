package com.thoughtworks.jira;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.thoughtworks.jira.util.CfdCreator;
import com.thoughtworks.jira.util.Config;

public class ClientTest {
	private Client client;
	
	@Mock
	private Config config;
	
	@Mock
	private Jira jira;
	
	@Mock
	private CfdCreator cfdCreator;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		client = new Client(jira, cfdCreator);
	}

	@Test
	public void retrieveStoriesWithChangelogShouldCallRetrieveStories() {
		client.retrieveStoriesWithChangelog();
		Mockito.verify(jira).retrieveStoriesWithChangelog();
	}

	@Test
	public void generateCSVShouldCallCfdCreator() {
		List<Story> stories = new ArrayList<>();
		Mockito.stub(jira.retrieveStoriesWithChangelog()).toReturn(stories);
		
		client.generateCSV();
		Mockito.verify(cfdCreator).generateCSVAndWriteToFile(stories);
	}
}
