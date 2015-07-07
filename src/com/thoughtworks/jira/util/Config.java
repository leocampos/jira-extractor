package com.thoughtworks.jira.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.thoughtworks.jira.exception.InvalidConfigurationException;

public class Config {
	private static final String CSV_SEPARATOR_KEY = "csv_separator";
	private static final String OUTPUT_PATH_KEY = "output_path";
	private static final String DATE_FORMAT_KEY = "date_format";
	
	private ResourceBundle configBundle = ResourceBundle.getBundle("config");
	private final Logger log = Logger.getLogger("Jira-extractor");
	private DateTimeFormatter formatter = null;
	private String csvSeparator;
	private String outputPath;
	
	public void setConfig(ResourceBundle config) {
		this.configBundle = config;
	}
	
	public DateTimeFormatter getDateTimeFormatter() {
		if(formatter == null) {
			String dateFormat = "dd/MM/yyyy HH:mm";
			
			if(configBundle.containsKey(DATE_FORMAT_KEY))
				dateFormat = configBundle.getString(DATE_FORMAT_KEY);
			
			formatter = DateTimeFormat.forPattern(dateFormat);
		}
		
		return formatter;
	}

	public URI getJiraUri() {
		return URI.create(configBundle.getString("jira_url"));
	}
	
	public String getJQL() {
		return configBundle.getString("jql");
	}

	public List<String> getStatusList() {
		validateStatusListConfiguration();
		
		return createStatusListUppercased();
	}

	private ArrayList<String> createStatusListUppercased() {
		ArrayList<String> returnList = new ArrayList<>();
		
		for (String status : configBundle.getString("status_list").split(", *"))
			returnList.add(status.toUpperCase());
		
		return returnList;
	}

	private void validateStatusListConfiguration() {
		if((!configBundle.containsKey("status_list")) || configBundle.getString("status_list").isEmpty())
			throw new InvalidConfigurationException("A lista de status 'status_list' não foi encontrada no arquivo de properties.");
	}

	public Logger getLogger() {
		return log;
	}

	public String getCSVSeparator() {
		if(csvSeparator == null) {
			csvSeparator = ";";
			if(configBundle.containsKey(CSV_SEPARATOR_KEY))
				csvSeparator = configBundle.getString(CSV_SEPARATOR_KEY);
		}
		
		return csvSeparator;
	}

	public String getOutputPath() {
		if(outputPath == null) {
			outputPath = "./cfd.csv";
			if(configBundle.containsKey(OUTPUT_PATH_KEY))
				outputPath = configBundle.getString(OUTPUT_PATH_KEY);
		}
		
		return outputPath;
	}

	public FileUtil getFileUtil() {
		return new FileUtil(this);
	}
}
