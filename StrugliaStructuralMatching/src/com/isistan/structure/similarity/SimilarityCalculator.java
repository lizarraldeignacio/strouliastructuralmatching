package com.isistan.structure.similarity;

import com.google.common.collect.Lists;
import com.isistan.util.Permutations;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private ParameterCombination mostSimilarCombination;
	private TObjectByteHashMap<ISchemaType> byteMap;
	private TByteObjectHashMap<ISchemaType> reverseByteMap;
	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public SimilarityCalculator() {
		byteMap = new TObjectByteHashMap<ISchemaType>();
		reverseByteMap = new TByteObjectHashMap<ISchemaType>();
	}

	@SuppressWarnings("rawtypes")
	private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> initialTargetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		final List<ISchemaType> targetTypes= /*initialTargetTypes.size() > 35? initialTargetTypes.subList(0, 35) :*/ initialTargetTypes;
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
	}
	
	private ArrayList<ISchemaType> reverseByteArrayMapping(TByteArrayList mapping) {
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
	}
	
	public ParameterCombination getMaxSimilarity(final ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		mostSimilarCombination= new ParameterCombination();
		if (sourceTypes.size() <= targetTypes.size()) {
			getSimilarity(sourceTypes, targetTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		else {
			getSimilarity(targetTypes, sourceTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		return mostSimilarCombination;
	}
	
	@Override
	protected void finalize() {
		executor.shutdownNow();
	}
}
