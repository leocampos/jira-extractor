package com.thoughtworks.jira;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.thoughtworks.jira.util.Config;

public class StoryTest {
	@Mock
	private Config mockConfig;
	
	@Mock
	private Issue mockIssue;
	
	private static final String BACKLOG = "Backlog";
	private Story story;
	
	@Before public void setup() {
		MockitoAnnotations.initMocks(this);
		story = new Story(mockIssue, mockConfig);
	}
	
	@Test
	public void testCloneShouldCloneFieldsAndChangelog() throws Exception {
		story.addStatusChange(new StatusChange());
		story.addStatusChange(new StatusChange());
		story.addStatusChange(new StatusChange());
		story.setFields(TestUtil.createMap("KEY", "VALUE"));
		
		Story clonedStory = story.clone();
		
		assertNotSame(story, clonedStory);
		assertNotSame(clonedStory.getChangelog(), story.getChangelog());
		assertTrue(clonedStory.getChangelog().size() == story.getChangelog().size());
		assertNotSame(story.getFields(), clonedStory.getFields());
		assertTrue(clonedStory.getFields().size() == story.getFields().size());
	}
	
	@Test
	public void testFindStatusWithoutChangelogShouldReturnFirstFromListOfStatus() {
		Mockito.stub(mockConfig.getStatusList()).toReturn(Arrays.asList(new String[]{BACKLOG}));
		
		assertEquals("BACKLOG", story.getStatus());
	}
	
	@Test
	public void testGetFieldsShouldReturnEmptySetIfNoFields() {
		assertEmptyMap(story.getFields());
	}
	
	@Test
	public void testGetFieldsShouldReturnMap() {
		List<IssueField> issueFields = new ArrayList<IssueField>();
		String name1 = "NAME1";
		String name2 = "NAME2";
		
		String value1 = "VALUE1";
		String value2 = "VALUE2";
		
		issueFields.add(new IssueField("ID1", name1, "TYPE", value1));
		issueFields.add(new IssueField("ID2", name2, "TYPE", value2));
		
		Mockito.stub(mockConfig.getFields()).toReturn(new HashSet<String>(Arrays.asList(new String[]{name1, name2})));
		Mockito.stub(mockIssue.getFields()).toReturn(issueFields);
		Mockito.when(mockIssue.getFieldByName(name1)).thenReturn(issueFields.get(0));
		Mockito.when(mockIssue.getFieldByName(name2)).thenReturn(issueFields.get(1));
		
		Map<String, String> fields = story.getFields();
		Assert.assertNotNull(fields);
		
		Assert.assertTrue(fields.containsKey(name1));
		Assert.assertTrue(fields.containsKey(name2));
		Assert.assertEquals(value1, fields.get(name1));
		Assert.assertEquals(value2, fields.get(name2));
	}
	
	@Test
	public void getFieldNameShouldReturnEmptyIfThereIsNoSuchField() throws Exception {
		Assert.assertEquals("", story.getFieldValueByName("AMARELO"));
	}
	
	@Test
	public void getFieldNameShouldReturnCorrespondingValue() throws Exception {
		story.setFields(TestUtil.createMap("KEY", "VALUE"));
		Assert.assertEquals("VALUE", story.getFieldValueByName("KEY"));
	}

	private void assertEmptyMap(Map<String, String> fields) {
		Assert.assertNotNull(fields);
		Assert.assertTrue(fields instanceof Map);
		Assert.assertTrue(fields.isEmpty());
	}
}
