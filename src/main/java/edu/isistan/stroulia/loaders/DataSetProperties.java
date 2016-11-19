package edu.isistan.stroulia.loaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;


public class DataSetProperties implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -975444381728448751L;
	private Properties properties;
	
	public DataSetProperties () {
		properties = new Properties();
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
