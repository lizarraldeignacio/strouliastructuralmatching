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
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridGain;

import com.isistan.loaders.DataSetLoader;
import com.isistan.loaders.StrouliaMatchingProperties;


public class Runner {

	public static final String LOADER_LOG = "LoaderLog";
	protected static final String LOADER_LOGFILE = "./loaderLog.txt";
	protected static final FileAppender fileAppender = new FileAppender();
	public static final String GRID_CACHE_NAME = "SimilarityCache";
	protected static final String STRUGLIA_PROPERTIES_FILE = "./strouliaProperties.xml";
	protected static Grid grid = null;

	public enum GridCacheObjects {
		PROPERTIES, SIMILARITY_BUFFER, DATASET_PROPERTIES, HITLIST_TABLE
	}
	
	@SuppressWarnings("unchecked")
	public static void main(final String[] args) throws IOException {
		try {
			grid = GridGain.start("default-config.xml");
			setupLoggers();
			FileInputStream strugliaPropertiesInStream;
			try {
				strugliaPropertiesInStream = new FileInputStream(new File(STRUGLIA_PROPERTIES_FILE));
				StrouliaMatchingProperties strugliaProperties = StrouliaMatchingProperties.instance();
				strugliaProperties.loadProperties(strugliaPropertiesInStream);
				grid.cache(GRID_CACHE_NAME).putx(GridCacheObjects.PROPERTIES, strugliaProperties);
			} catch (FileNotFoundException e) {
				Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - missing strugialProperties.xml");
			}
			new DataSetLoader().run();
			fileAppender.close();
			grid.stopNodes();
		} catch (GridException e) {
			e.printStackTrace();
		}
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
