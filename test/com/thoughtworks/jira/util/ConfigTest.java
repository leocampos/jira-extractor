package com.thoughtworks.jira.util;

import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.thoughtworks.jira.InvalidConfigurationException;

public class ConfigTest {
	@Mock private ResourceBundle mockBundle;
	private Config config = new Config();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		config.setConfig(mockBundle);
	}
	
	@Test(expected=InvalidConfigurationException.class)
	public void testGetStatusListShouldThrowExceptionIfEmpty() {
		Mockito.stub(mockBundle.containsKey("status_list")).toReturn(false);
		
		config.getStatusList();
	}
}
