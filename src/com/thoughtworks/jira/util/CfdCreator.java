package com.thoughtworks.jira.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

import org.joda.time.DateTime;

import com.thoughtworks.jira.StatusChange;
import com.thoughtworks.jira.Story;

public class CfdCreator {
	private List<String> statuses;
	private Config config;

	public CfdCreator(Config config) {
		this.config = config;
		statuses = config.getStatusList();
	}

	private void calculateDates(Story story) {
		HashMap<String, DateTime> dates = new HashMap<>();
		
		for (StatusChange statusChange : story.getChangelog()) {
			if(dates.containsKey(statusChange.getTo().toUpperCase())) continue;
			dates.put(statusChange.getTo().toUpperCase(), statusChange.getChangeDate());
		}
		
		story.setDates(dates);
	}
	
	/**
	 * Stories go back and forth in the process, they jump some steps etc<br/>
	 * This messes up the data, so we need to treat them.<br/>
	 * <br/>
	 * When steps are jumped, we need to fill these steps with dates<br/>
	 * When they go back, we need to pop the former stages from the stack, like they had never been there in the first place.<br/>
	 * @return cloned story list with reestructed changelog for each story
	 */
	public List<Story> restructureDataForCFD(List<Story> stories) {
		List<Story> restructuredStories = new ArrayList<Story>(stories.size());
		
		stories.forEach((story) -> {
			Story clonedStory = cloneAndReestructureStory(story);
			restructuredStories.add(clonedStory);
			calculateDates(clonedStory);
		});
		
		return restructuredStories;
	}
	
	private Story cloneAndReestructureStory(Story story) {
		Story clonedStory = story.clone();
		
		addFirstStageToChangelog(clonedStory);
		addIntermediateStepsIfThereIsGap(clonedStory);
		removeStepsWhenItGoesBackwards(clonedStory);
		
		return clonedStory;
	}
	
	private void removeStepsWhenItGoesBackwards(Story story) {
		if(story.getChangelog().isEmpty()) return;
		Stack<StatusChange> changes = new Stack<>();
		
		StatusChange formerChange = story.getChangelog().get(0);
		changes.add(formerChange);
		
		for (int i = 1; i < story.getChangelog().size(); i++) {
			StatusChange nextChange = story.getChangelog().get(i);
			
			if(isSecondStatusFormerThanFirst(formerChange.getTo(), nextChange.getTo())) {
				StatusChange poppedChange = changes.pop();
				while(!changes.isEmpty()) {
					if(poppedChange.getTo().toUpperCase().equals(nextChange.getTo().toUpperCase())) {
						nextChange = poppedChange;
						break;
					}
					
					poppedChange = changes.pop();
					if(changes.isEmpty())
						nextChange = poppedChange;
				}
			}
			
			formerChange = nextChange;
			changes.add(nextChange);
		}
		
		story.getChangelog().clear();
		changes.forEach((statusChange)->{
			story.addStatusChange(statusChange);
		});
	}
	
	private boolean isSecondStatusFormerThanFirst(String originStatus, String comparedStatus) {
		int indexOfActualStatus = config.getStatusList().indexOf(originStatus.toUpperCase());
		int indexOfComparedStatus = config.getStatusList().indexOf(comparedStatus.toUpperCase());
		
		return indexOfComparedStatus < indexOfActualStatus;
	}
	
	private void addIntermediateStepsIfThereIsGap(Story story) {
		if(story.getChangelog().isEmpty()) return;
		
		StatusChange formerStep = story.getChangelog().get(0);
		ArrayList<StatusChange> withIntermediateSteps = new ArrayList<>();
		withIntermediateSteps.add(formerStep);
		
		
		for (int i = 1; i < story.getChangelog().size(); i++) {
			StatusChange statusChange = story.getChangelog().get(i);
			
			String to = statusChange.getTo().toUpperCase();
			String from = formerStep.getTo().toUpperCase();
			int indexOfNewChange = config.getStatusList().indexOf(to);
			int indexOfFormerChange = config.getStatusList().indexOf(from);
			
			int jumpSize = indexOfNewChange - indexOfFormerChange;
			if(jumpSize > 1) {
				for (int j = 0; j < jumpSize - 1; j++) {
					withIntermediateSteps.add(new StatusChange(config.getStatusList().get(++indexOfFormerChange), statusChange.getChangeDate()));
				}
			}
			
			formerStep = statusChange;
			withIntermediateSteps.add(statusChange);
		}
		
		story.setChangelog(withIntermediateSteps);
	}

	private void addFirstStageToChangelog(Story story) {
		story.getChangelog().add(0, new StatusChange(getFirstStage(), story.getCreationDate()));
	}
	
	private String getFirstStage() {
		return config.getStatusList().get(0).toUpperCase();
	}

	public String generate(List<Story> stories) {
		config.getLogger().log(Level.INFO, "Generating CFD Data");
		
		StringBuilder data = new StringBuilder();
		
		buildHeader(data);
		generateDataForStories(stories, data);
		
		config.getLogger().log(Level.INFO, "CFD Data generated");
		
		return data.toString();
	}

	public void buildHeader(StringBuilder data) {
		data.append("name");
		
		appendStatusesToHeader(data);
		appendFieldsToHeader(data);
		
		data.append("\n");
	}

	private void appendFieldsToHeader(StringBuilder data) {
		if(config.getFields() == null) return;
		
		config.getFields().forEach((field) -> {
			data.append(";").append(field);
		});
	}

	private void appendStatusesToHeader(StringBuilder data) {
		statuses.forEach((status)->{
			data.append(";").append(status);
		});
	}

	private void generateDataForStories(List<Story> stories, StringBuilder data) {
		restructureDataForCFD(stories).forEach((story)->{
			generateDataForStory(data, story);
		});
	}
	
	public void generateCSVAndWriteToFile(List<Story> stories) {
		logINFO("Writing CFD data to file");
		
		if(stories == null) {
			logINFO("No data to write.");
			
			return;
		}
		
		config.getFileUtil().writeToFile(generate(stories));
	}

	private void logINFO(String msg) {
		config.getLogger().log(Level.INFO, msg);
	}

	private void generateDataForStory(StringBuilder data, Story story) {
		data.append(story.getKey());
		appendStatusesDatesToEachLine(data, story);
		appendFieldsToEachLine(data, story);
		
		data.append("\n");
	}

	private void appendFieldsToEachLine(StringBuilder data, Story story) {
		config.getFields().forEach((field) -> {
			data.append(config.getCSVSeparator()).append(story.getFieldValueByName(field));
		});
	}

	private void appendStatusesDatesToEachLine(StringBuilder data, Story story) {
		statuses.forEach((status)->{
			DateTime stageStart = story.getStageStart(status);
			
			data.append(config.getCSVSeparator()).append(stageStart == null? "" : config.getDateTimeFormatter().print(stageStart));
		});
	}
}
