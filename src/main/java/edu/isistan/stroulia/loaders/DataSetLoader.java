package edu.isistan.stroulia.loaders;
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

import org.apache.log4j.Logger;

import edu.isistan.stroulia.structure.similarity.IOperation;
import edu.isistan.stroulia.structure.similarity.ParameterCombination;
import edu.isistan.stroulia.structure.similarity.SimpleOperation;


public class DataSetLoader implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -163857493039425244L;
	protected static final String LOADER_LOG = "DatasetLoader";
	protected static final String DATASET_PROPERTIES_FILE = "datasetProperties.xml";
	
	public void run() {
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
				System.out.println("Analyzing Folder: ");
				for (final File inputFile : inputDir.listFiles()) {
					if (inputFile.toString().endsWith(".txt")){													
						 similarityBuffer.append(executeInterfaceCompatibility(new File(datasetProperties.getProperty(DataSetProperty.RESOURCES_PATH)),
										new File(datasetProperties.getProperty(DataSetProperty.QUERY_PATH)), inputFile.toString()));
						}
				}
			}
		} catch (FileNotFoundException e) {
			Logger.getLogger(LOADER_LOG).fatal("Dataset Loader Error - missing datasetProperties.xml");
		}
		System.out.println("Writing Files...");
		FileWriter similarityResultsFile;
		try {
			similarityResultsFile = new FileWriter(new File(datasetProperties.getProperty(DataSetProperty.RESULTS_FILENAME)));
			BufferedWriter similarityResultsBuffer = new BufferedWriter(similarityResultsFile);
			similarityResultsBuffer.write(similarityBuffer.toString());
			similarityResultsBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
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
					similarityBuffer.append(Float.toString(maxSimilarity) + ",");
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
		if (wsdlOperations != null) {
			Iterator<IOperation> iterWSDLOp = wsdlOperations.iterator();
			if (iterClassOp.hasNext()) {
				final SimpleOperation queryOP = (SimpleOperation) iterClassOp.next();
				while(iterWSDLOp.hasNext()) {
					final IOperation targetOp = iterWSDLOp.next();
					ParameterCombination combination = queryOP.getMaxSimilarity(targetOp);
					if ((combination != null) && (combination.getSimilarity() > serviceSimilarityValue)) {
						serviceSimilarityValue = combination.getSimilarity();
					}
				}
			}
		}
		
		return serviceSimilarityValue;
	}
}
