package com.thoughtworks.jira;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.thoughtworks.jira.util.Config;

public class StoryTest {
	private static final String READY_FOR_DEV = "Ready for Dev";
	private static final String IN_PROGRESS = "In Progress";
	private static final String QA = "QA";
	private static final String DONE = "Done";
	private static final String BACKLOG = "Backlog";
	
	@Mock
	private Config mockConfig;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.stub(mockConfig.getStatusList()).toReturn(Arrays.asList(new String[]{BACKLOG.toUpperCase(),READY_FOR_DEV.toUpperCase(),IN_PROGRESS.toUpperCase(),QA.toUpperCase(),DONE.toUpperCase()}));
	}
	
	@Test
	public void testFindStatusWithoutChangelogShouldReturnFirstFromListOfStatus() {
		Mockito.stub(mockConfig.getStatusList()).toReturn(Arrays.asList(new String[]{BACKLOG}));
		Story story = new Story("TESTE", mockConfig);
		
		assertEquals("BACKLOG", story.getStatus());
	}
	
	@Test
	public void testFindStatusChangelogShouldReturnLastFromListOfStatus() {
		Story story = new Story("TESTE", mockConfig);
		story.addStatusChange(new StatusChange(QA, new DateTime()));
		story.addStatusChange(new StatusChange(READY_FOR_DEV, new DateTime()));
		
		story.restructureChangelog();
		
		assertEquals(READY_FOR_DEV.toUpperCase(), story.getStatus());
	}
	
	@Test
	public void testGetStageStartShouldReturnCreationDateForFirstState() {
		Story story = new Story("TESTE", mockConfig);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		DateTime dt = formatter.parseDateTime("05/11/2010 11:12:13");
		
		story.setCreationDate(dt);
		story.addStatusChange(new StatusChange(QA, new DateTime()));
		story.addStatusChange(new StatusChange(READY_FOR_DEV, new DateTime()));
		
		assertEquals(dt, story.getStageStart(BACKLOG));
	}
	
	@Test
	public void testRestructureChangelogShouldAddIntermediateSteps() {
		Story story = new Story("TESTE", mockConfig);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
		story.setCreationDate(formatter.parseDateTime("10/06/2015 13:48"));
		DateTime readyForDevFirst = formatter.parseDateTime("10/06/2015 16:00");
		
		story.addStatusChange(new StatusChange(READY_FOR_DEV, readyForDevFirst));
		story.addStatusChange(new StatusChange(DONE, formatter.parseDateTime("01/07/2015 10:30")));
		
		story.restructureChangelog();
		
		assertEquals(5, story.getChangelog().size());
	}
	
	@Test
	public void testRestructureChangelogShouldRemoveStepsThatGoBackwards() {
		Story story = new Story("TESTE", mockConfig);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
		DateTime creationDate = formatter.parseDateTime("10/06/2015 13:48");
		DateTime inProgressDate = formatter.parseDateTime("18/06/2015 10:58");
		DateTime qaDate = formatter.parseDateTime("18/06/2015 19:30");
		DateTime doneDate = formatter.parseDateTime("01/07/2015 10:30");
		story.setCreationDate(creationDate);
		
		story.addStatusChange(new StatusChange(READY_FOR_DEV, formatter.parseDateTime("10/06/2015 16:00")));
		story.addStatusChange(new StatusChange(IN_PROGRESS, formatter.parseDateTime("10/06/2015 17:14")));
		story.addStatusChange(new StatusChange(READY_FOR_DEV, formatter.parseDateTime("10/06/2015 17:21")));
		story.addStatusChange(new StatusChange(IN_PROGRESS, formatter.parseDateTime("10/06/2015 17:21")));
		story.addStatusChange(new StatusChange(BACKLOG, inProgressDate));
		story.addStatusChange(new StatusChange(IN_PROGRESS, inProgressDate));
		story.addStatusChange(new StatusChange(QA, qaDate));
		story.addStatusChange(new StatusChange(DONE, doneDate));
		
		story.restructureChangelog();
		
		assertEquals(BACKLOG.toUpperCase(), story.getChangelog().get(0).getTo().toUpperCase());
		assertEquals(READY_FOR_DEV.toUpperCase(), story.getChangelog().get(1).getTo().toUpperCase());
		assertEquals(IN_PROGRESS.toUpperCase(), story.getChangelog().get(2).getTo().toUpperCase());
		assertEquals(QA.toUpperCase(), story.getChangelog().get(3).getTo().toUpperCase());
		assertEquals(DONE.toUpperCase(), story.getChangelog().get(4).getTo().toUpperCase());
		
		assertEquals(BACKLOG, creationDate, story.getChangelog().get(0).getChangeDate());
		assertEquals(IN_PROGRESS, inProgressDate, story.getChangelog().get(1).getChangeDate());
		assertEquals(READY_FOR_DEV, inProgressDate, story.getChangelog().get(2).getChangeDate());
		assertEquals(QA, qaDate, story.getChangelog().get(3).getChangeDate());
		assertEquals(DONE, doneDate, story.getChangelog().get(4).getChangeDate());
	}
}
