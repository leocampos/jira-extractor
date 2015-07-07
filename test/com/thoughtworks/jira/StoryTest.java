package com.thoughtworks.jira;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;

import com.thoughtworks.jira.util.Config;

public class StoryTest {
	@Mock
	private Config mockConfig;
	
	private static final String BACKLOG = "Backlog";
	
	@Before public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testFindStatusWithoutChangelogShouldReturnFirstFromListOfStatus() {
		Mockito.stub(mockConfig.getStatusList()).toReturn(Arrays.asList(new String[]{BACKLOG}));
		Story story = new Story("TESTE", mockConfig);
		
		assertEquals("BACKLOG", story.getStatus());
	}
}
