package com.isistan.structure.similarity;

import java.io.Serializable;
import java.util.ArrayList;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private ParameterCombination mostSimilarCombination;
	//private TObjectByteHashMap<ISchemaType> byteMap;
	//private TByteObjectHashMap<ISchemaType> reverseByteMap;
	private ParameterCombination partialSolution;
	//private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public SimilarityCalculator() {
		
		/*byteMap = new TObjectByteHashMap<ISchemaType>();
		reverseByteMap = new TByteObjectHashMap<ISchemaType>();*/
	}

	/*private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> initialTargetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		//final List<ISchemaType> targetTypes= /*initialTargetTypes.size() > 35? initialTargetTypes.subList(0, 35) : initialTargetTypes;
		if (initialTargetTypes.size() > 35) {
			return;
		}
		final LinkedList<TByteArrayList> permutations = Permutations.permuteUnique(byteArrayMapping(targetTypes));
		int size = permutations.size() > Runtime.getRuntime().availableProcessors() ? permutations.size() / Runtime.getRuntime().availableProcessors() : permutations.size();  
		List<List<TByteArrayList>> partitions = Lists.partition(permutations, size);
		List<Future> futures = new LinkedList<Future>();
		for (final List<TByteArrayList> part : partitions) {
			futures.add(executor.submit(new Runnable() {	
				@Override
				public void run() {
					for (TByteArrayList list : part) {
						ParameterCombination combination = new ParameterCombination(sourceTypes, reverseByteArrayMapping(list), sourceReturnType, targetReturnType, 0);
						combination.calculateSimilarity();
						if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
							mostSimilarCombination = (ParameterCombination) combination.clone();
						}
					}
				}
			}));
		}
		for (Future future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}*/
	
	
	private void getSimilarity(ArrayList<ISchemaType> sourceTypes, ArrayList<ISchemaType> targetTypes, ISchemaType sourceReturnType, ISchemaType targetReturnType) {
		if (sourceTypes.size() == 0) {
			ParameterCombination combination = (ParameterCombination) partialSolution.clone();
			combination.setSourceReturnType(sourceReturnType);
			combination.setTargetReturnType(targetReturnType);
			combination.calculateSimilarity();
			if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
				mostSimilarCombination = combination;
			}
			return;
		}
		ISchemaType targetType;
		ISchemaType sourceType = sourceTypes.remove(0);
		partialSolution.addSourceParameter(sourceType);
		
		for (int j = 0; j < targetTypes.size(); j++) {
			targetType = targetTypes.get(j);
			targetTypes.remove(j);
			partialSolution.addTargetParameter(targetType);
			getSimilarity(sourceTypes, targetTypes, sourceReturnType, targetReturnType);
			partialSolution.removeLastTargetParameter();
			targetTypes.add(j, targetType);
		}
		
		partialSolution.removeLastSourceParameter();
		sourceTypes.add(0, sourceType);
	}
	
	/*private ArrayList<ISchemaType> reverseByteArrayMapping(TByteArrayList mapping) {
		ArrayList<ISchemaType> reverseArray = new ArrayList<ISchemaType>();
		for (int i = 0; i < mapping.size(); i++) {
			reverseArray.add(reverseByteMap.get(mapping.getQuick(i)));
		}
		return reverseArray;
	}
	
	private TByteArrayList byteArrayMapping(List<ISchemaType> types) {
		byteMap.clear();
		reverseByteMap.clear();
		TByteArrayList byteMapping = new TByteArrayList(types.size());
		byte index = 0;
		for (int i = 0; i < types.size(); i++) {
			ISchemaType iSchemaType = types.get(i);
			if (!byteMap.contains(iSchemaType)) {
				byteMap.put(iSchemaType, index);
				reverseByteMap.put(index, iSchemaType);
				index++;
			}
			byteMapping.add(byteMap.get(iSchemaType));
		}
		return byteMapping;
	}*/
	
	public ParameterCombination getMaxSimilarity(final ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		partialSolution = new ParameterCombination();
		mostSimilarCombination= new ParameterCombination();
		if (sourceTypes.size() <= targetTypes.size()) {
			getSimilarity(sourceTypes, targetTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		else {
			getSimilarity(targetTypes, sourceTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		return mostSimilarCombination;
	}
	
	/*@Override
	protected void finalize() {
		executor.shutdownNow();
	}*/
}
