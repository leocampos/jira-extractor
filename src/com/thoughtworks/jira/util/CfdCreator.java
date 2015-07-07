package com.thoughtworks.jira.util;

import java.util.List;

import com.thoughtworks.jira.Story;

public class CfdCreator {
	private List<Story> stories;
	private List<String> statuses;

	public CfdCreator(List<Story> stories, List<String> statuses) {
		this.statuses = statuses;
		if(stories == null) throw new NullPointerException("Lista de histórias não pode ser nula");
		
		this.stories = stories;
	}
	
	public String generate() {
		StringBuilder data = new StringBuilder();
		
		buildHeader(data);
		generateDataForStories(data);
		
		return data.toString();
	}

	private void buildHeader(StringBuilder data) {
		data.append("name");
		statuses.forEach((status)->{
			data.append(";").append(status);
		});
		data.append("\n");
	}

	private void generateDataForStories(StringBuilder data) {
		stories.forEach((story)->{
			generateDataForStory(data, story);
		});
	}

	private void generateDataForStory(StringBuilder data, Story story) {
		data.append(story.getKey());
		statuses.forEach((status)->{
			data.append(";").append(story.getStageStart(status));
		});
		data.append("\n");
	}
}
