package com.isistan.loaders;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class DataSetProperties {
	private static DataSetProperties instance = new DataSetProperties();
	private Properties properties;
	
	private DataSetProperties () {
		properties = new Properties();
	}
	
	public static DataSetProperties instance() {
		return instance;
	}
	
	public void loadProperties(InputStream inStream) {
		try {
			properties.loadFromXML(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(DataSetProperty property) {	
		return properties.getProperty(property.toString().toLowerCase());
	}
}
