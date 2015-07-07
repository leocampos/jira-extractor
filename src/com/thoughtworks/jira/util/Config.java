package com.thoughtworks.jira.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.thoughtworks.jira.InvalidConfigurationException;

public class Config {
	private ResourceBundle configBundle = ResourceBundle.getBundle("config");
	private final Logger log = Logger.getLogger("Jira-extractor");
	
	public void setConfig(ResourceBundle config) {
		this.configBundle = config;
	}

	public URI getJiraUri() {
		return URI.create(configBundle.getString("jira_url"));
	}
	
	public String getJQL() {
		return configBundle.getString("jql");
	}

	public List<String> getStatusList() {
		validateStatusListConfiguration();
		
		ArrayList<String> returnList = new ArrayList<>();
		String[] listOfStatus = configBundle.getString("status_list").split(", *");
		for (String status : listOfStatus) {
			returnList.add(status.toUpperCase());
		}
		
		return returnList;
	}

	private void validateStatusListConfiguration() {
		if((!configBundle.containsKey("status_list")) || configBundle.getString("status_list").isEmpty())
			throw new InvalidConfigurationException("A lista de status 'status_list' não foi encontrada no arquivo de properties.");
	}

	public Logger getLogger() {
		return log;
	}
}
