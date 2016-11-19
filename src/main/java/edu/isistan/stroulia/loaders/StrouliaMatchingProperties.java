package edu.isistan.stroulia.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

public class StrouliaMatchingProperties implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8874737367656499686L;
	private static StrouliaMatchingProperties instance = new StrouliaMatchingProperties();
	private Properties properties; 
	
	private StrouliaMatchingProperties () {
		properties = new Properties();
	}
	
	public static StrouliaMatchingProperties instance() {
		return instance;
	}
	
	public void loadProperties(InputStream inStream) {
		try {
			properties.loadFromXML(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(StrouliaPropertyName property) {	
		return properties.getProperty(property.toString().toLowerCase());
	}
	
	public float getPrimitiveTypeCompatibility(PrimitiveType type1, PrimitiveType type2) {
		String property = properties.getProperty(type1.toString().toLowerCase() + "_to_" + type2.toString().toLowerCase());
		return property != null ? Float.parseFloat(property) : 0;
	}
}
