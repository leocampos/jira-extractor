package com.thoughtworks.jira.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.thoughtworks.jira.exception.InvalidConfigurationException;

public class Config {
	private static final String CSV_SEPARATOR_KEY = "csv_separator";
	private static final String OUTPUT_PATH_KEY = "output_path";
	private static final String DATE_FORMAT_KEY = "date_format";

	private Properties configBundle = new Properties();
	private final Logger log = Logger.getLogger("Jira-extractor");
	private DateTimeFormatter formatter = null;
	private String csvSeparator;
	private String outputPath;
	
	public Config() {
		initResourceBundle();
	}

	public void initResourceBundle() {
		try {
			File jarPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
			String propertiesPath = jarPath.getParentFile().getAbsolutePath();
			configBundle.load(new FileInputStream(propertiesPath + "./config.properties"));
		} catch (IOException e1) {
			getLogger().log(Level.SEVERE, e1.getMessage(), e1);
		}
	}

	public DateTimeFormatter getDateTimeFormatter() {
		if (formatter == null) {
			String dateFormat = "dd/MM/yyyy HH:mm";

			if (configBundle.containsKey(DATE_FORMAT_KEY))
				dateFormat = configBundle.getProperty(DATE_FORMAT_KEY);

			formatter = DateTimeFormat.forPattern(dateFormat);
		}

		return formatter;
	}

	public URI getJiraUri() {
		return URI.create(configBundle.getProperty("jira_url"));
	}

	public String getJQL() {
		return configBundle.getProperty("jql");
	}

	public List<String> getStatusList() {
		validateStatusListConfiguration();

		return createStatusListUppercased();
	}

	private ArrayList<String> createStatusListUppercased() {
		ArrayList<String> returnList = new ArrayList<>();

		for (String status : configBundle.getProperty("status_list").split(", *"))
			returnList.add(status.toUpperCase());

		return returnList;
	}

	private void validateStatusListConfiguration() {
		if ((!configBundle.containsKey("status_list"))
				|| configBundle.getProperty("status_list").isEmpty())
			throw new InvalidConfigurationException(
					"A lista de status 'status_list' não foi encontrada no arquivo de properties.");
	}

	public Logger getLogger() {
		return log;
	}

	public String getCSVSeparator() {
		if (csvSeparator == null) {
			csvSeparator = ";";
			if (configBundle.containsKey(CSV_SEPARATOR_KEY))
				csvSeparator = configBundle.getProperty(CSV_SEPARATOR_KEY);
		}

		return csvSeparator;
	}

	public String getOutputPath() {
		if (outputPath == null) {
			outputPath = "./cfd.csv";
			if (configBundle.containsKey(OUTPUT_PATH_KEY))
				outputPath = configBundle.getProperty(OUTPUT_PATH_KEY);
		}

		return outputPath;
	}

	public FileUtil getFileUtil() {
		return new FileUtil(this);
	}

	public void setConfig(Properties configBundle) {
		this.configBundle = configBundle;
	}
}
