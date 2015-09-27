package com.thoughtworks.jira;

import java.util.HashMap;
import java.util.Map;

public class TestUtil {

	public static Map<String, String> createMap(String... keyValues) {
		Map<String, String> fields = new HashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			fields.put(keyValues[i], keyValues[i + 1]);
		}

		return fields;
	}

}
