package com.thoughtworks.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.thoughtworks.jira.util.Config;

public class Story implements Cloneable {
	private String key;
	private List<StatusChange> changelog;
	private Config config;
	private HashMap<String, DateTime> dates;
	private String status ;
	private DateTime creationDate;
	private Map<String, String> fields = null;
	private Issue issueWithExpando;

	public Story(String key, Config config) {
		this.key = key;
		this.config = config;
	}

	public Story(Issue issueWithExpando, Config config) {
		this(issueWithExpando.getKey(), config);
		this.issueWithExpando = issueWithExpando;

		setCreationDate(issueWithExpando.getCreationDate());
	}

	public String getKey() {
		return key;
	}
	
	public DateTime getStageStart(String status) {
		if(dates == null || dates.isEmpty() || !dates.containsKey(status.toUpperCase())) return null;
		return dates.get(status.toUpperCase());
	}

	private String getFirstStage() {
		return config.getStatusList().get(0).toUpperCase();
	}

	private String findActualStatus() {
		List<StatusChange> changelogList = getChangelog();
		if(changelogList.isEmpty())
			return getFirstStage();
		
		return changelogList.get(changelogList.size() - 1).getTo().toUpperCase();
	}

	public String getStatus() {
		if(status == null)
			status = findActualStatus();
	
		return status;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public List<StatusChange> getChangelog() {
		if(changelog == null)
			changelog = new ArrayList<StatusChange>();
		
		return changelog;
	}

	public void setChangelog(List<StatusChange> changelog) {
		this.changelog = changelog;
	}

	public void addStatusChange(StatusChange statusChange) {
		getChangelog().add(statusChange);
	}
	
	@Override
	public String toString() {
		System.out.println(String.format("Actual status: %s", getStatus()));
		StringBuilder builder = new StringBuilder(key).append("\n");
		for (StatusChange statusChange : getChangelog()) {
			builder.append(String.format("de %s para %s em %s", statusChange.getFrom(), statusChange.getTo(), statusChange.getChangeDate()));
		}
		
		return builder.toString();
	}

	public void setCreationDate(DateTime creationDate) {
		this.creationDate = creationDate;
	}

	public DateTime getCreationDate() {
		return creationDate;
	}

	public void setDates(HashMap<String, DateTime> dates) {
		this.dates = dates;
	}

	public Map<String, String> getFields() {
		fillFieldsInfoAndCreateFieldsIfEmpty();
		
		return fields ;
	}

	private void fillFieldsInfoAndCreateFieldsIfEmpty() {
		if(fields != null) return;
		
		fields = new HashMap<String, String>();
			
		fillFieldsInfo();
	}

	private void fillFieldsInfo() {
		if(config.getFields() == null) return;
		if(issueWithExpando == null) return;
		if(issueWithExpando.getFields() == null) return;
		
		config.getFields().forEach((fieldName) -> {
			IssueField issueField = issueWithExpando.getFieldByName(fieldName);
			if(issueField != null) {
				String value = issueField.getValue() == null ? "" : issueField.getValue().toString();
				fields.put(issueField.getName(), value);
			}
		});
	}

	@Override
	public Story clone() {
		Story clonedStory = new Story(getKey(), config);
		clonedStory.setCreationDate(getCreationDate());
		clonedStory.setChangelog(new ArrayList<StatusChange>(getChangelog()));
		cloneFields(clonedStory);

		return clonedStory;
	}

	private void cloneFields(Story clonedStory) {
		Map<String, String> clonedFields = new HashMap<String, String>();
		clonedFields.putAll(getFields());
		clonedStory.setFields(clonedFields);
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public String getFieldValueByName(String fieldName) {
		if(!getFields().containsKey(fieldName)) return "";
		
		return getFields().get(fieldName);
	}
}
