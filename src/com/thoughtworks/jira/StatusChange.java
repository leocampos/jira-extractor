package com.thoughtworks.jira;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.domain.ChangelogItem;

public class StatusChange {
	private String statusFrom, statusTo;
	private DateTime changeDate;
	
	public StatusChange(ChangelogItem changelogItem, DateTime changeDate) {
		statusFrom = changelogItem.getFromString();
		statusTo = changelogItem.getToString();
		this.changeDate = changeDate;
		
	}

	public StatusChange() {
	}

	public StatusChange(String statusTo, DateTime changeDate) {
		this.statusTo = statusTo;
		this.changeDate = changeDate;
	}

	public String getFrom() {
		return statusFrom;
	}
	
	public void setStatusFrom(String statusFrom) {
		this.statusFrom = statusFrom;
	}
	
	public String getTo() {
		return statusTo;
	}
	
	public void setStatusTo(String statusTo) {
		this.statusTo = statusTo;
	}
	
	public DateTime getChangeDate() {
		return changeDate;
	}
	
	public void setChangeDate(DateTime changeDate) {
		this.changeDate = changeDate;
	}
	
	@Override
	public String toString() {
		return getTo().toUpperCase();
	}
}
