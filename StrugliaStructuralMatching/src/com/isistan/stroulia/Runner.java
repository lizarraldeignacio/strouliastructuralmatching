package com.isistan.stroulia;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.isistan.loaders.DataSetLoader;
import com.isistan.loaders.StrouliaMatchingProperties;


public class Runner {

	public static final String LOADER_LOG = "LoaderLog";
	protected static final String LOADER_LOGFILE = "./loaderLog.txt";
	protected static final FileAppender fileAppender = new FileAppender();
	protected static final String STRUGLIA_PROPERTIES_FILE = "./strouliaProperties.xml";

	public enum GridCacheObjects {
		PROPERTIES, SIMILARITY_BUFFER, DATASET_PROPERTIES, HITLIST_TABLE
	}
	
	public static void main(final String[] args) throws IOException {
		setupLoggers();
		FileInputStream strugliaPropertiesInStream;
		try {
			strugliaPropertiesInStream = new FileInputStream(new File(STRUGLIA_PROPERTIES_FILE));
			StrouliaMatchingProperties strugliaProperties = StrouliaMatchingProperties.instance();
			strugliaProperties.loadProperties(strugliaPropertiesInStream);
		} catch (FileNotFoundException e) {
			Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - missing strugialProperties.xml");
		}
		new DataSetLoader().run();
		fileAppender.close();
	}
	
		
	private static void setupLoggers() {
		try {
			fileAppender.setWriter(new BufferedWriter(new FileWriter(new File(LOADER_LOGFILE))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileAppender.setLayout(new PatternLayout());
		Logger.getLogger(LOADER_LOG).addAppender(fileAppender);
	}
}
