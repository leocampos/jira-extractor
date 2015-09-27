package com.thoughtworks.jira.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.thoughtworks.jira.StatusChange;
import com.thoughtworks.jira.Story;
import com.thoughtworks.jira.TestUtil;

public class CfdCreatorTest {
	private static final String READY_FOR_DEV = "Ready for Dev";
	private static final String IN_PROGRESS = "In Progress";
	private static final String QA = "QA";
	private static final String DONE = "Done";
	private static final String BACKLOG = "Backlog";
	
	private DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
	private CfdCreator creator;
	
	@Mock
	private Config mockConfig;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.stub(mockConfig.getStatusList()).toReturn(Arrays.asList(new String[]{BACKLOG.toUpperCase(),READY_FOR_DEV.toUpperCase(),IN_PROGRESS.toUpperCase(),QA.toUpperCase(),DONE.toUpperCase()}));
		Mockito.stub(mockConfig.getDateTimeFormatter()).toReturn(formatter);
		Mockito.stub(mockConfig.getCSVSeparator()).toReturn(";");
		Mockito.stub(mockConfig.getLogger()).toReturn(Logger.getLogger("TESTE"));
		
		creator = new CfdCreator(mockConfig);
	}
	
	@Test
	public void testFindStatusChangelogShouldReturnLastFromListOfStatus() {
		Story story = new Story("TESTE", mockConfig);
		story.addStatusChange(new StatusChange(QA, new DateTime()));
		story.addStatusChange(new StatusChange(READY_FOR_DEV, new DateTime()));
		
		List<Story> stories = new ArrayList<Story>();
		stories.add(story);
		List<Story> reestructuredList = createAndRestructure(stories);
		
		assertEquals(READY_FOR_DEV.toUpperCase(), reestructuredList.get(0).getStatus());
	}

	private List<Story> createAndRestructure(List<Story> stories) {
		List<Story> reestructuredList = creator.restructureDataForCFD(stories);
		return reestructuredList;
	}
	
	@Test
	public void testGetStageStartShouldReturnCreationDateForFirstState() {
		Story story = new Story("TESTE", mockConfig);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		DateTime dt = formatter.parseDateTime("05/11/2010 11:12:13");
		
		story.setCreationDate(dt);
		story.addStatusChange(new StatusChange(QA, new DateTime()));
		story.addStatusChange(new StatusChange(READY_FOR_DEV, new DateTime()));
		
		List<Story> stories = new ArrayList<Story>();
		stories.add(story);
		List<Story> reestructuredList = createAndRestructure(stories);
		
		assertEquals(dt, reestructuredList.get(0).getStageStart(BACKLOG));
	}
	
	@Test
	public void testRestructureChangelogShouldAddIntermediateSteps() {
		Story story = new Story("TESTE", mockConfig);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
		story.setCreationDate(formatter.parseDateTime("10/06/2015 13:48"));
		DateTime readyForDevFirst = formatter.parseDateTime("10/06/2015 16:00");
		
		story.addStatusChange(new StatusChange(READY_FOR_DEV, readyForDevFirst));
		story.addStatusChange(new StatusChange(DONE, formatter.parseDateTime("01/07/2015 10:30")));
		
		List<Story> stories = new ArrayList<Story>();
		stories.add(story);
		
		assertEquals(5, createAndRestructure(stories).get(0).getChangelog().size());
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
		
		List<Story> stories = new ArrayList<Story>();
		stories.add(story);
		List<Story> reestructuredList = createAndRestructure(stories);
		Story restructuredStory = reestructuredList.get(0);
		
		assertEquals(BACKLOG.toUpperCase(), restructuredStory.getChangelog().get(0).getTo().toUpperCase());
		assertEquals(READY_FOR_DEV.toUpperCase(), restructuredStory.getChangelog().get(1).getTo().toUpperCase());
		assertEquals(IN_PROGRESS.toUpperCase(), restructuredStory.getChangelog().get(2).getTo().toUpperCase());
		assertEquals(QA.toUpperCase(), restructuredStory.getChangelog().get(3).getTo().toUpperCase());
		assertEquals(DONE.toUpperCase(), restructuredStory.getChangelog().get(4).getTo().toUpperCase());
		
		assertEquals(BACKLOG, creationDate, restructuredStory.getChangelog().get(0).getChangeDate());
		assertEquals(IN_PROGRESS, inProgressDate, restructuredStory.getChangelog().get(1).getChangeDate());
		assertEquals(READY_FOR_DEV, inProgressDate, restructuredStory.getChangelog().get(2).getChangeDate());
		assertEquals(QA, qaDate, restructuredStory.getChangelog().get(3).getChangeDate());
		assertEquals(DONE, doneDate, restructuredStory.getChangelog().get(4).getChangeDate());
	}
	
	@Test
	public void testGenerateShouldCreateHeaderAndLines() {
		List<Story> stories = new ArrayList<>();
		Map<String, String> fields = TestUtil.createMap("A","AV","B", "BV");
		prepareFields(fields);

		Story story1 = new Story("História 1", mockConfig);
		story1.setCreationDate(parseDate("30/06/2015 10:30"));
		story1.addStatusChange(new StatusChange(READY_FOR_DEV, formatter.parseDateTime("01/07/2015 10:30")));
		story1.addStatusChange(new StatusChange(IN_PROGRESS, formatter.parseDateTime("02/07/2015 10:30")));
		story1.setFields(fields);
		stories.add(story1);
		
		Story story2 = new Story("História 2", mockConfig);
		story2.setCreationDate(parseDate("05/07/2015 10:30"));
		story2.setFields(fields);
		stories.add(story2);
		
		assertEquals("name;BACKLOG;READY FOR DEV;IN PROGRESS;QA;DONE;A;B\nHistória 1;30/06/2015 10:30;01/07/2015 10:30;02/07/2015 10:30;;;AV;BV\nHistória 2;05/07/2015 10:30;;;;;AV;BV\n", creator.generate(stories));
	}

	private void prepareFields(Map<String, String> fields) {
		Mockito.stub(mockConfig.getFields()).toReturn(fields.keySet());
	}
	
	@Test
	public void buildHeaderShouldListStatusesFollowedByFields() throws Exception {
		HashSet<String> fields = new HashSet<String>();
		fields.add("summary");
		fields.add("size");
		
		Mockito.stub(mockConfig.getFields()).toReturn(fields);
		StringBuilder data = new StringBuilder();
		creator.buildHeader(data);
		
		assertEquals("name;BACKLOG;READY FOR DEV;IN PROGRESS;QA;DONE;summary;size\n", data.toString());
	}

	private DateTime parseDate(String date) {
		return formatter.parseDateTime(date);
	}
}
