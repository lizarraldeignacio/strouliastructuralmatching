package com.isistan.loaders;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.gridgain.grid.GridGain;
import org.gridgain.grid.GridIllegalStateException;
import org.gridgain.grid.lang.GridCallable;

import com.isistan.structure.similarity.IOperation;
import com.isistan.structure.similarity.ParameterCombination;
import com.isistan.structure.similarity.SimpleOperation;


public class DataSetLoader implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -163857493039425244L;
	protected ExecutorService gridExecutor = GridGain.grid().compute().executorService();
	protected static final String LOADER_LOG = "DatasetLoader";
	protected static final String DATASET_PROPERTIES_FILE = "datasetProperties.xml";
	private static final String CANCELED_LOG_FILENAME = "report.txt";
	private StringBuffer tasksBuffer;
	
	public void run() {
		long startTime = System.nanoTime();
		tasksBuffer = new StringBuffer();
		FileInputStream datasetPropertiesInStream;
		final DataSetProperties datasetProperties = new DataSetProperties();
		StringBuffer similarityBuffer = new StringBuffer();
		try {
			datasetPropertiesInStream = new FileInputStream(new File(DATASET_PROPERTIES_FILE));
			datasetProperties.loadProperties(datasetPropertiesInStream);
			File inputDir = new File(datasetProperties.getProperty(DataSetProperty.INPUT_PATH));
			if (inputDir.list() == null) {
				  System.out.println("No files into the specified folder");
			}
			else {
				LinkedList<Future<String>> futures = new LinkedList<Future<String>>();
				System.out.println("Analyzing Folder: ");
				for (final File inputFile : inputDir.listFiles()) {
					if (inputFile.toString().endsWith(".txt")){													
						futures.add(
						gridExecutor.submit(new GridCallable<String>() {
							/**
							 * 
							 */
							private static final long serialVersionUID = 2789954077792873576L;

							@Override
							public String call() throws Exception {
								return executeInterfaceCompatibility(new File(datasetProperties.getProperty(DataSetProperty.RESOURCES_PATH)),
										new File(datasetProperties.getProperty(DataSetProperty.QUERY_PATH)), inputFile.toString());
							}
						}));
						}
				}
			
				for (Future<String> future : futures) {
					try {
						similarityBuffer.append(future.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - missing datasetProperties.xml");
		} catch (GridIllegalStateException e1) {
			e1.printStackTrace();
		}
		gridExecutor.shutdown();
		System.out.println("Writing Files...");
		FileWriter similarityResultsFile;
		FileWriter canceledTasksFile;
		try {
			similarityResultsFile = new FileWriter(new File(datasetProperties.getProperty(DataSetProperty.RESULTS_FILENAME)));
			BufferedWriter similarityResultsBuffer = new BufferedWriter(similarityResultsFile);
			canceledTasksFile = new FileWriter(new File(CANCELED_LOG_FILENAME));
			BufferedWriter canceledTasksBuffer = new BufferedWriter(canceledTasksFile);
			canceledTasksBuffer.write(tasksBuffer.toString());
			similarityResultsBuffer.write(similarityBuffer.toString());
			canceledTasksBuffer.close();
			similarityResultsBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		System.out.println("Done!");
		System.out.println("Total time (in minutes): " + (endTime-startTime)/(1000000000*60));
	}

	private String executeInterfaceCompatibility(File resourcesPath, File queryPath, String originalQuery) {
		StringBuffer similarityBuffer = new StringBuffer();
		System.out.println("Executing IC for query: " + originalQuery);
		String originalQueryName = originalQuery.substring(originalQuery.lastIndexOf(File.separator)+1,originalQuery.lastIndexOf("."));
		similarityBuffer.append(originalQueryName + ",");
		System.out.println("\t QueryName: " + originalQueryName);
		try {
		    BufferedReader br = new BufferedReader(new FileReader(originalQuery));
		    String line = br.readLine();
		    while (line != null) {
		    	if (!line.isEmpty()){
	    			String wsdlName = line.split(",")[0];
		    		String originalClassName  = "query." + Character.toUpperCase(originalQueryName.charAt(0)) + originalQueryName.substring(1);
					float maxSimilarity = calculateSimilarity(queryPath, resourcesPath, originalClassName, wsdlName);
					similarityBuffer.append(maxSimilarity + ",");
		    	}
		    	line = br.readLine();
		    }
    		similarityBuffer.append(originalQueryName + System.lineSeparator());
		    br.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return similarityBuffer.toString();
	}
	
	private float calculateSimilarity(File queryPath, File resourcesPath, String originalClassName, String candidateWSDLName) {
		System.out.println("Calling With: ");
		System.out.println("\t OriginalClass: " + originalClassName);
		System.out.println("\t CandidateWSDL: " + candidateWSDLName);
		TypeClassLoader classLoader = new TypeClassLoader(originalClassName);
		Collection<IOperation> classOperations = classLoader.load(queryPath);
		Iterator<IOperation> iterClassOp = classOperations.iterator();
		WSDLLoader loader = new WSDLLoader();
		File wsdlFile = new File(resourcesPath + File.separator + candidateWSDLName.toLowerCase());
		Collection<IOperation> wsdlOperations = loader.load(wsdlFile);
		float serviceSimilarityValue = 0;
		ExecutorService timeoutExecutor = Executors.newSingleThreadExecutor();
		if (wsdlOperations != null) {
			Iterator<IOperation> iterWSDLOp = wsdlOperations.iterator();
			if (iterClassOp.hasNext()) {
				final SimpleOperation queryOP = (SimpleOperation) iterClassOp.next();
				while(iterWSDLOp.hasNext()) {
					final IOperation targetOp = iterWSDLOp.next();
					ParameterCombination combination = null;
					try {
						combination = timeoutExecutor.submit(new Callable<ParameterCombination>() {
							@Override
							public ParameterCombination call() throws Exception {
								return queryOP.getMaxSimilarity(targetOp);
							}
						}).get(5, TimeUnit.MINUTES);
						tasksBuffer.append("Finished - Query: " + originalClassName + " Query operation: " + queryOP.getName() + " Service: " + candidateWSDLName + " Service operation: " + ((SimpleOperation)targetOp).getName() + "/n");
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
					} catch (TimeoutException e) {
						tasksBuffer.append("Canceled - Query: " + originalClassName + " Query operation: " + queryOP.getName() + " Service: " + candidateWSDLName + " Service operation: " + ((SimpleOperation)targetOp).getName() + "/n");
					} 
					//ParameterCombination combination = queryOP.getMaxSimilarity(targetOp);
					if ((combination != null) && (combination.getSimilarity() > serviceSimilarityValue)) {
						serviceSimilarityValue = combination.getSimilarity();
					}
				}
			}
		}
		
		return serviceSimilarityValue;
	}
}
