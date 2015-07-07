package com.thoughtworks.jira.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class FileUtil {
	private Config config;

	public FileUtil(Config config) {
		this.config = config;
	}

	public void writeToFile(String text) {
		generateCsvFile(text);
	}

	private void generateCsvFile(String text) {
		try(FileWriter writer = new FileWriter(config.getOutputPath())) {
			writer.append(text);

			writer.flush();
		} catch (IOException e) {
			config.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
