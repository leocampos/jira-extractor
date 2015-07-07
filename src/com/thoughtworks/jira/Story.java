package com.thoughtworks.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.joda.time.DateTime;

import com.thoughtworks.jira.util.Config;

public class Story {
	private String key;
	private List<StatusChange> changelog;
	private Config config;
	private HashMap<String, DateTime> dates;
	private String status ;
	private DateTime creationDate;

	public Story(String key, Config config) {
		this.key = key;
		this.config = config;
	}

	public String getKey() {
		return key;
	}
	
	public DateTime getStageStart(String status) {
		if(dates == null)
			calculateDates();
		
		return dates.get(status.toUpperCase());
	}

	private void calculateDates() {
		if(status != null) return;
		
		dates = new HashMap<>();
		
		restructureChangelog();
		this.status = findActualStatus();
		dates.put(getFirstStage(), creationDate);
		if(changelog.isEmpty())
			return;
		
		for (StatusChange statusChange : changelog) {
			if(dates.containsKey(statusChange.getTo().toUpperCase())) continue;
			if(isLaterStatus(statusChange.getTo())) continue;
			
			dates.put(statusChange.getTo().toUpperCase(), statusChange.getChangeDate());
		}
	}

	private String getFirstStage() {
		return config.getStatusList().get(0).toUpperCase();
	}

	public void restructureChangelog() {
		getChangelog().add(0, new StatusChange(getFirstStage(), creationDate));
		
		addIntermediateStepsIfThereIsGap();
		removeStepsWhenItGoesBackwards();
	}

	private void removeStepsWhenItGoesBackwards() {
		if(getChangelog().isEmpty()) return;
		Stack<StatusChange> changes = new Stack<>();
		
		StatusChange formerChange = changelog.get(0);
		changes.add(formerChange);
		
		for (int i = 1; i < changelog.size(); i++) {
			StatusChange nextChange = changelog.get(i);
			
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
		
		changelog = new ArrayList<>();
		changes.forEach((statusChange)->{
			changelog.add(statusChange);
		});
	}

	private void addIntermediateStepsIfThereIsGap() {
		if(getChangelog().isEmpty()) return;
		
		StatusChange formerStep = getChangelog().get(0);
		ArrayList<StatusChange> withIntermediateSteps = new ArrayList<>();
		withIntermediateSteps.add(formerStep);
		
		
		for (int i = 1; i < changelog.size(); i++) {
			StatusChange statusChange = changelog.get(i);
			
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
		
		changelog = withIntermediateSteps;
	}

	private boolean isLaterStatus(String comparedStatus) {
		return isComparedStatusLaterThanOrigin(status, comparedStatus);
	}
	
	private boolean isComparedStatusLaterThanOrigin(String originStatus, String comparedStatus) {
		int indexOfActualStatus = config.getStatusList().indexOf(originStatus.toUpperCase());
		int indexOfComparedStatus = config.getStatusList().indexOf(comparedStatus.toUpperCase());
		
		return indexOfComparedStatus > indexOfActualStatus;
	}
	
	private boolean isSecondStatusFormerThanFirst(String originStatus, String comparedStatus) {
		int indexOfActualStatus = config.getStatusList().indexOf(originStatus.toUpperCase());
		int indexOfComparedStatus = config.getStatusList().indexOf(comparedStatus.toUpperCase());
		
		return indexOfComparedStatus < indexOfActualStatus;
	}

	private String findActualStatus() {
		List<StatusChange> changelogList = getChangelog();
		if(changelogList.isEmpty())
			return getFirstStage();
		
		return changelogList.get(changelogList.size() - 1).getTo().toUpperCase();
	}

	public String getStatus() {
		if(status == null)
			calculateDates();
		
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
}
