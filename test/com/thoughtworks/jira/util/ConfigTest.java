package com.thoughtworks.jira.util;

import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.thoughtworks.jira.exception.InvalidConfigurationException;

public class ConfigTest {
	@Mock private Properties mockBundle;
	private Config config = new Config() {
		public void initResourceBundle(String propertiesPath) {};
	};
	
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
	
	@Test
	public void testGetFieldsShouldReturnNullIfThereIsNoFieldsEntry() {
		Mockito.stub(mockBundle.containsKey(Config.FIELDS)).toReturn(false);
		
		Assert.assertNull(config.getFields());
	}
	
	@Test
	public void testGetFieldsShouldReturnNullIfThereIsFieldsEntryButEmpty() {
		Mockito.stub(mockBundle.containsKey(Config.FIELDS)).toReturn(true);
		Mockito.stub(mockBundle.getProperty(Config.FIELDS)).toReturn("  ");
		
		Assert.assertNull(config.getFields());
	}
	
	@Test
	public void maxNumOfItemsShouldReturnNullIfNotSet() throws Exception {
		Mockito.stub(mockBundle.containsKey(Config.MAX_NUM_OF_ITEMS)).toReturn(false);
		Assert.assertNull(config.getMaxNumberOfItems());
	}
	
	@Test
	public void maxNumOfItemsShouldReturnNumOfItemsIfSet() throws Exception {
		Mockito.stub(mockBundle.containsKey(Config.MAX_NUM_OF_ITEMS)).toReturn(true);
		Mockito.stub(mockBundle.getProperty(Config.MAX_NUM_OF_ITEMS)).toReturn("5");
		Assert.assertEquals(new Integer(5), config.getMaxNumberOfItems());
	}
	
	@Test(expected=InvalidConfigurationException.class)
	public void maxNumOfItemsShouldThrowExceptionIfNotNumeric() {
		Mockito.stub(mockBundle.containsKey(Config.MAX_NUM_OF_ITEMS)).toReturn(true);
		Mockito.stub(mockBundle.getProperty(Config.MAX_NUM_OF_ITEMS)).toReturn("erro");
		config.getMaxNumberOfItems();
	}
	
	@Test
	public void testGetFieldsShouldReturnSetOfFieldsIfFieldsEntryHasData() {
		Mockito.stub(mockBundle.containsKey(Config.FIELDS)).toReturn(true);
		Mockito.stub(mockBundle.getProperty(Config.FIELDS)).toReturn("a,b,c");
		
		Set<String> fields = config.getFields();
		int expectedSize = 3;
		
		Assert.assertEquals(expectedSize, fields.size());
		Assert.assertTrue(fields.contains("a"));
		Assert.assertTrue(fields.contains("b"));
		Assert.assertTrue(fields.contains("c"));
	}
}
