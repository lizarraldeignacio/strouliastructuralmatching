package com.isistan.stroulia;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.isistan.loaders.DataSetLoader;


public class Runner {

	public static final String LOADER_LOG = "LoaderLog";
	protected static final String LOADER_LOGFILE = "./loaderLog.txt";
	protected static final FileAppender fileAppender = new FileAppender();

	public static void main(final String[] args) throws IOException {
		setupLoggers();
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
