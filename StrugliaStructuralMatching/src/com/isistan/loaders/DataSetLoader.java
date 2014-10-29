package com.isistan.loaders;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.isistan.structure.similarity.IOperation;
import com.isistan.structure.similarity.ParameterCombination;
import com.isistan.structure.similarity.SimpleOperation;


public class DataSetLoader {
	
	protected StringBuffer similarityBuffer = new StringBuffer();
	protected StringBuffer hitListBuffer = new StringBuffer();
	protected ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	protected static final String LOADER_LOG = "DatasetLoader";
	protected static final String STROULIA_PROPERTIES_FILE = "strouliaProperties.xml";
	protected static final String DATASET_PROPERTIES_FILE = "datasetProperties.xml";
	protected static Integer canceledComparisons = 0;
	protected static Integer totalComparisons = 0;
	
	public void run() {
		long startTime = System.nanoTime();
		FileInputStream strugliaPropertiesInStream;
		try {
			strugliaPropertiesInStream = new FileInputStream(new File(STROULIA_PROPERTIES_FILE));
			StrouliaMatchingProperties strugliaProperties = StrouliaMatchingProperties.instance();
			strugliaProperties.loadProperties(strugliaPropertiesInStream);
		} catch (FileNotFoundException e) {
			Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - missing strugialProperties.xml");
		}
		FileInputStream datasetPropertiesInStream;
		try {
			datasetPropertiesInStream = new FileInputStream(new File(DATASET_PROPERTIES_FILE));
			final DataSetProperties datasetProperties = DataSetProperties.instance();
			datasetProperties.loadProperties(datasetPropertiesInStream);
			File inputDir = new File(datasetProperties.getProperty(DataSetProperty.INPUT_PATH));
			if (inputDir.list() == null) {
				  System.out.println("No files into the specified folder");
			}
			else {
				LinkedList<Future<?>> futures = new LinkedList<Future<?>>();
				System.out.println("Analyzing Folder: ");
				for (final File inputFile : inputDir.listFiles()) {
					if (inputFile.toString().endsWith(".txt")){													
						futures.add(
						executor.submit(new Runnable() {
							@Override
							public void run() {
								executeInterfaceCompatibility(createHitlistTable(new File(datasetProperties.getProperty(DataSetProperty.HITLIST_PATH))
								, datasetProperties.getProperty(DataSetProperty.HITLIST_FILENAMES)), new File(datasetProperties.getProperty(DataSetProperty.RESOURCES_PATH)),
								new File(datasetProperties.getProperty(DataSetProperty.QUERY_PATH)), new File(datasetProperties.getProperty(DataSetProperty.ORIGINALS_PATH)), inputFile.toString());
							}
						}));
						}
					}
				for (Future<?> future : futures) {
					try {
						future.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - missing datasetProperties.xml");
		}
		executor.shutdownNow();
		System.out.println("Writing Files...");
		DataSetProperties datasetProperties = DataSetProperties.instance();
		try {
			FileWriter similarityResultsFile = new FileWriter(new File(datasetProperties.getProperty(DataSetProperty.RESULTS_FILENAME)));
			FileWriter hitListResultsFile = new FileWriter(new File(datasetProperties.getProperty(DataSetProperty.HITLIST_RESULTS_FILENAME)));
			BufferedWriter similarityResultsBuffer = new BufferedWriter(similarityResultsFile);
			BufferedWriter hitListResultsBuffer = new BufferedWriter(hitListResultsFile);
			similarityResultsBuffer.write(similarityBuffer.toString());
			hitListResultsBuffer.write(hitListBuffer.toString());
			similarityResultsBuffer.close();
			hitListResultsBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		System.out.println("Done!");
		System.out.println("Total comparisons: " + totalComparisons);
		System.out.println("Canceled comparisons: " + canceledComparisons);
		System.out.println("Canceled percentage: " + canceledComparisons/totalComparisons);
		System.out.println("Total time (in minutes): " + (endTime-startTime)/60000000);
	}
		

	private void executeInterfaceCompatibility(HashMap<String,String> hitlist, File resourcesPath, File queryPath, File originalsPath, String originalQuery) {
		StringBuffer similarityBuffer = new StringBuffer();
		StringBuffer hitListBuffer = new StringBuffer();
		HashSet<String> analizedWsdls = new HashSet<String>();
		System.out.println("Executing IC for query: " + originalQuery);
		String originalQueryName = originalQuery.substring(originalQuery.lastIndexOf(File.separator)+1,originalQuery.lastIndexOf("."));
		writeBuffer(similarityBuffer, originalQueryName + ",");
		writeBuffer(hitListBuffer, originalQueryName + ",");
		System.out.println("\t QueryName: " + originalQueryName);
		try{
			FileInputStream fis =new FileInputStream(originalQuery);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8"); 
		    BufferedReader br = new BufferedReader(isr);
		    String linea = br.readLine();
	    	linea = linea.replace("[", "");
	    	linea = linea.replace("]", "");
	    	linea = linea.trim();
	    	if (!linea.isEmpty()){
	    		String[] wsdlList = linea.split(",");
	    		for (String wsdl : wsdlList) {
	    			String auxWsdl = wsdl.substring(wsdl.lastIndexOf("/")+1);
		    		if (auxWsdl.contains("-0")|
		    			auxWsdl.contains("-1")){				    			
		    			auxWsdl = auxWsdl.substring(0,auxWsdl.lastIndexOf("-"));
		    		}
		    		String originalClassName  = "query." + originalQueryName;
    				String candidateWSDLName = auxWsdl.replace(".class", ".wsdl").toLowerCase();
					analizedWsdls.add(candidateWSDLName.toLowerCase().replace(".wsdl", ""));
					float maxSimilarity = calculateSimilarity(queryPath, resourcesPath, originalClassName, candidateWSDLName);
					writeResultsFileBuffers(maxSimilarity, hitlist.get(originalQueryName.toLowerCase()).toLowerCase().equals(candidateWSDLName.toLowerCase().replace(".wsdl", "")), originalQueryName, similarityBuffer, hitListBuffer);
    				}
    			}
    		String hit = hitlist.get(originalQueryName.toLowerCase()).toLowerCase();
    		if (!analizedWsdls.contains(hit)) {
    			try{
    				String originalClassName  = "query." + originalQueryName;
    				hit = hit.substring(0,1).toUpperCase()+hit.substring(1);
    				String candidateWSDLName = (hit + ".wsdl").toLowerCase();
    				System.out.println("Relevant service does not appear");
    				float maxSimilarity = calculateSimilarity(queryPath, originalsPath, originalClassName, candidateWSDLName);
    				writeResultsFileBuffers(maxSimilarity, true, originalQueryName, similarityBuffer, hitListBuffer);
    			}
    			catch (Exception e){
    				e.printStackTrace();
    			}
    		}
    		writeBuffer(similarityBuffer, originalQueryName + System.lineSeparator());
    		writeBuffer(hitListBuffer, originalQueryName + System.lineSeparator());
		    br.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		writeBuffer(this.similarityBuffer, similarityBuffer.toString());
		writeBuffer(this.hitListBuffer, hitListBuffer.toString());
	}
	
	private void writeBuffer(StringBuffer buffer, String data) {
		synchronized (buffer) {
			buffer.append(data);
		}
	}
	
	private void writeResultsFileBuffers(float maxSimilarity, boolean isHit, String originalQueryName, StringBuffer similarityBuffer, StringBuffer hitListBuffer) {
		writeBuffer(similarityBuffer, maxSimilarity + ",");
		if (isHit) {
			writeBuffer(hitListBuffer, "-1,");
		}
		else {
			writeBuffer(hitListBuffer, "0,");
		}
	}
	
	private float calculateSimilarity(File queryPath, File resourcesPath, String originalClassName, String candidateWSDLName) {
		System.out.println("Calling With: ");
		System.out.println("\t OriginalClass: " + originalClassName);
		System.out.println("\t CandidateWSDL: " + candidateWSDLName);
		TypeClassLoader classLoader = new TypeClassLoader(originalClassName);
		Collection<IOperation> classOperations = classLoader.load(queryPath);
		Iterator<IOperation> iterClassOp = classOperations.iterator();
		WSDLLoader loader = new WSDLLoader();
		File wsdlFile = new File(resourcesPath + File.separator + candidateWSDLName);
		Collection<IOperation> wsdlOperations = loader.load(wsdlFile);
		float serviceSimilarityValue = 0;
		if (wsdlOperations != null) {
			Iterator<IOperation> iterWSDLOp = wsdlOperations.iterator();
			if (iterClassOp.hasNext()) {
				SimpleOperation queryOP = (SimpleOperation) iterClassOp.next();
				while(iterWSDLOp.hasNext()) {
					IOperation targetOp = iterWSDLOp.next();
					ParameterCombination combination = queryOP.getMaxSimilarity(targetOp);
					if (combination != null) {
						synchronized (totalComparisons) {
							totalComparisons++;
						}
						serviceSimilarityValue = combination.getSimilarity() > serviceSimilarityValue ? combination.getSimilarity() : serviceSimilarityValue;
					}
					else { //If the calculation takes long time to finish, we cancel it and return 0.
						synchronized (canceledComparisons) {
							canceledComparisons++;
						}
						serviceSimilarityValue = 0;
						break;
					}
				}
			}
		}
		return serviceSimilarityValue;
	}
	
	
	
	private HashMap<String, String> createHitlistTable(File hitListDir, String hitListFileName) {		
		HashMap<String, String> aux = new HashMap<String, String>();
		for (String actualHitListDir : hitListDir.list()) {
			String actualHitListFile = hitListDir.getAbsolutePath() + File.separator +  actualHitListDir + File.separator + hitListFileName;
			String queryName = actualHitListDir.substring(actualHitListDir.toString().lastIndexOf("#") + 1);
			FileInputStream fis;
			try {
				fis = new FileInputStream(actualHitListFile);
				InputStreamReader isr;
				try {
					isr = new InputStreamReader(fis, "UTF8");
					BufferedReader br = new BufferedReader(isr);
				    String linea;
					try {
						linea = br.readLine();
						String hitName = "";
				    	
					    if (linea.contains("-")) {
					    	hitName = linea.substring(linea.lastIndexOf(File.separator)+1,linea.lastIndexOf("-"));
					    }
					    else{
					    	hitName = linea.substring(linea.lastIndexOf(File.separator)+1,linea.lastIndexOf("."));
					    }
					    aux.put(queryName.toLowerCase(), hitName);
					    br.close();
					} catch (IOException e) {
						Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - i/o error in hitlist file: " + actualHitListFile);
					}    
				} catch (UnsupportedEncodingException e) {
					Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - unknown hitlist file encoding");
				} 
			} catch (FileNotFoundException e) {
				Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - missing " + actualHitListFile +  " hitlist file");
			}
		}
		return aux;
	}
}
