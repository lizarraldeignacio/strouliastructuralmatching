package com.isistan.loaders;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridGain;
import org.gridgain.grid.GridIllegalStateException;
import org.gridgain.grid.cache.GridCache;
import org.gridgain.grid.cache.GridCacheTx;
import org.gridgain.grid.lang.GridRunnable;

import com.isistan.stroulia.Runner;
import com.isistan.stroulia.Runner.GridCacheObjects;
import com.isistan.structure.similarity.IOperation;
import com.isistan.structure.similarity.ParameterCombination;
import com.isistan.structure.similarity.SimpleOperation;


public class DataSetLoader implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -163857493039425244L;
	protected ExecutorService gridExecutor = GridGain.grid().compute().executorService();
	protected ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	protected static final String LOADER_LOG = "DatasetLoader";
	protected static final String DATASET_PROPERTIES_FILE = "datasetProperties.xml";
	
	@SuppressWarnings("unchecked")
	public void run() {
		long startTime = System.nanoTime();
		try {
			GridGain.grid().cache(Runner.GRID_CACHE_NAME).putx(Runner.GridCacheObjects.SIMILARITY_BUFFER, new StringBuffer());
			GridGain.grid().cache(Runner.GRID_CACHE_NAME).putx(Runner.GridCacheObjects.HITLIST_BUFFER, new StringBuffer());
		} catch (GridIllegalStateException e1) {
			e1.printStackTrace();
		} catch (GridException e1) {
			e1.printStackTrace();
		}
		FileInputStream datasetPropertiesInStream;
		final DataSetProperties datasetProperties = new DataSetProperties();
		try {
			datasetPropertiesInStream = new FileInputStream(new File(DATASET_PROPERTIES_FILE));
			datasetProperties.loadProperties(datasetPropertiesInStream);
			final Map<String, String> hitListTable = createHitlistTable(new File(datasetProperties.getProperty(DataSetProperty.HITLIST_PATH)), datasetProperties.getProperty(DataSetProperty.HITLIST_FILENAMES));
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
						gridExecutor.submit(new GridRunnable() {
							/**
							 * 
							 */
							private static final long serialVersionUID = 2789954077792873576L;

							@Override
							public void run() {
								executeInterfaceCompatibility(hitListTable
								, new File(datasetProperties.getProperty(DataSetProperty.RESOURCES_PATH)),
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
		} catch (GridIllegalStateException e1) {
			e1.printStackTrace();
		}
		System.out.println("Writing Files...");
		try {
			FileWriter similarityResultsFile = new FileWriter(new File(datasetProperties.getProperty(DataSetProperty.RESULTS_FILENAME)));
			FileWriter hitListResultsFile = new FileWriter(new File(datasetProperties.getProperty(DataSetProperty.HITLIST_RESULTS_FILENAME)));
			BufferedWriter similarityResultsBuffer = new BufferedWriter(similarityResultsFile);
			BufferedWriter hitListResultsBuffer = new BufferedWriter(hitListResultsFile);
			StringBuffer similarityBuffer = (StringBuffer) GridGain.grid().cache(Runner.GRID_CACHE_NAME).get(GridCacheObjects.SIMILARITY_BUFFER);
			StringBuffer hitListBuffer = (StringBuffer) GridGain.grid().cache(Runner.GRID_CACHE_NAME).get(GridCacheObjects.HITLIST_BUFFER);
			similarityResultsBuffer.write(similarityBuffer.toString());
			hitListResultsBuffer.write(hitListBuffer.toString());
			similarityResultsBuffer.close();
			hitListResultsBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GridIllegalStateException e) {
			e.printStackTrace();
		} catch (GridException e) {
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		System.out.println("Done!");
		System.out.println("Total time (in minutes): " + (endTime-startTime)/(1000000*60));
	}
		
	@Override
	public void finalize() {
		gridExecutor.shutdownNow();
	}

	private void executeInterfaceCompatibility(Map<String,String> hitlist, File resourcesPath, File queryPath, File originalsPath, String originalQuery) {
		StringBuffer similarityBuffer = new StringBuffer();
		StringBuffer hitListBuffer = new StringBuffer();
		HashSet<String> analizedWsdls = new HashSet<String>();
		System.out.println("Executing IC for query: " + originalQuery);
		String originalQueryName = originalQuery.substring(originalQuery.lastIndexOf(File.separator)+1,originalQuery.lastIndexOf("."));
		similarityBuffer.append(originalQueryName + ",");
		hitListBuffer.append(originalQueryName + ",");
		System.out.println("\t QueryName: " + originalQueryName);
		try {
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
    				String candidateWSDLName = auxWsdl.replace(".class", ".wsdl");
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
    				String candidateWSDLName = hit + ".wsdl";
    				System.out.println("Relevant service does not appear");
    				float maxSimilarity = calculateSimilarity(queryPath, originalsPath, originalClassName, candidateWSDLName);
    				writeResultsFileBuffers(maxSimilarity, true, originalQueryName, similarityBuffer, hitListBuffer);
    			}
    			catch (Exception e){
    				e.printStackTrace();
    			}
    		}
    		similarityBuffer.append(originalQueryName + System.lineSeparator());
    		hitListBuffer.append(originalQueryName + System.lineSeparator());
		    br.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		writeBuffer(GridCacheObjects.SIMILARITY_BUFFER, similarityBuffer.toString());
		writeBuffer(GridCacheObjects.HITLIST_BUFFER, hitListBuffer.toString());
	}
	
	@SuppressWarnings("unchecked")
	private void writeBuffer(GridCacheObjects object, String data) {
		GridCache<GridCacheObjects, Object> cache = GridGain.grid().cache(Runner.GRID_CACHE_NAME);
		try (GridCacheTx tx = cache.txStart()) {
		    StringBuffer buffer = (StringBuffer) cache.get(object);
		    buffer.append(data);
		    cache.putx(object, buffer);
		    tx.commit();
		}
		catch (GridException e) {
			e.printStackTrace();
		}
	}
	
	private void writeResultsFileBuffers(float maxSimilarity, boolean isHit, String originalQueryName, StringBuffer similarityBuffer, StringBuffer hitListBuffer) {
		similarityBuffer.append(maxSimilarity + ",");
		if (isHit) {
			hitListBuffer.append("-1,");
		}
		else {
			hitListBuffer.append("0,");
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
		File wsdlFile = new File(resourcesPath + File.separator + candidateWSDLName.toLowerCase());
		Collection<IOperation> wsdlOperations = loader.load(wsdlFile);
		float serviceSimilarityValue = 0;
		List<Future<Float>> futures = new LinkedList<Future<Float>>();
		if (wsdlOperations != null) {
			Iterator<IOperation> iterWSDLOp = wsdlOperations.iterator();
			if (iterClassOp.hasNext()) {
				final SimpleOperation queryOP = (SimpleOperation) iterClassOp.next();
				while(iterWSDLOp.hasNext()) {
					final IOperation targetOp = iterWSDLOp.next();
					futures.add(executor.submit(new Callable<Float>() {
						@Override
						public Float call() throws Exception {
							ParameterCombination combination = queryOP.getMaxSimilarity(targetOp);
							return combination != null ? combination.getSimilarity() : 0;
						}
					}));
				}
				for (Future<Float> future : futures) {
					try {
						if (future.get() > serviceSimilarityValue)
							serviceSimilarityValue = future.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					}
				}
			}
			
		return serviceSimilarityValue;
	}
	
	
	
	private Map<String, String> createHitlistTable(File hitListDir, String hitListFileName) {
		System.out.println(hitListDir);
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
