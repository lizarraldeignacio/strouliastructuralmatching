package com.isistan.structure.similarity;

import com.isistan.util.Permutations;

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
	private ExecutorService executor = Executors.newFixedThreadPool(6);
	
	public SimilarityCalculator() {
		
	}

	private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> targetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		final LinkedList<ArrayList<ISchemaType>> permutations = /*Permutations.permuteUnique(targetTypes)*/null;
		for (List<ISchemaType> list : permutations) {
			ParameterCombination combination = new ParameterCombination(sourceTypes, list, sourceReturnType, targetReturnType, 0);
			combination.calculateSimilarity();
			//System.out.println(combination.getSimilarity());
			if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
				mostSimilarCombination = (ParameterCombination) combination.clone();
			}
		}
		/*final int size = permutations.size() > 6 ? permutations.size()/6 : permutations.size();
		List<Future> futures = new LinkedList<Future>();
		if (permutations.size() <= 6) {
			futures.add(executor.submit(new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					for (int i = 0 ; i < permutations.size(); i++) {
						ParameterCombination combination = new ParameterCombination(sourceTypes, permutations.get(i), sourceReturnType, targetReturnType, 0);
						combination.calculateSimilarity();
						if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
							mostSimilarCombination = (ParameterCombination) combination.clone();
						}
					}
						
				}
			}));
		}
		else {
			for (int i = 0; i < 6; i++) {
				final int startIndex = i * size;
				final int endIndex = i == 5 ? permutations.size() : (i + 1) * size;
				futures.add(executor.submit(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						int start = startIndex;
						int end = endIndex;
						for (int i = start ; i < end; i++) {
							ParameterCombination combination = new ParameterCombination(sourceTypes, permutations.get(i), sourceReturnType, targetReturnType, 0);
							combination.calculateSimilarity();
							if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
								mostSimilarCombination = (ParameterCombination) combination.clone();
							}
						}
							
					}
				}));
			}
		}
		for (Future f : futures)
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
		}*/
	}
	
	/*private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> targetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		final Object[] permutations = Collections2.permutations(targetTypes).toArray();
		final int size = permutations.length > 6 ? permutations.length/6 : permutations.length;
		List<Future> futures = new LinkedList<Future>();
		if (size < 6) {
			futures.add(executor.submit(new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					for (int i = 0 ; i < size; i++) {
						ParameterCombination combination = new ParameterCombination(sourceTypes, (List<ISchemaType>) permutations[i], sourceReturnType, targetReturnType);
						combination.calculateSimilarity();
						if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
							mostSimilarCombination = (ParameterCombination) combination.clone();
						}
					}
						
				}
			}));
		}
		else {
			for (int i = 0; i < 6; i++) {
				final int startIndex = i * size;
				final int endIndex = i == 5 ? permutations.length : (i + 1) * size;
				futures.add(executor.submit(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						int start = startIndex;
						int end = endIndex;
						for (int i = start ; i < end; i++) {
							ParameterCombination combination = new ParameterCombination(sourceTypes, (List<ISchemaType>) permutations[i], sourceReturnType, targetReturnType);
							combination.calculateSimilarity();
							if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
								mostSimilarCombination = (ParameterCombination) combination.clone();
							}
						}
							
					}
				}));
			}
		}
		for (Future f : futures)
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
		}
	}*/
	
	
	/*private <T> List<List<T>> partitions(Collection<T> collection, long size) {
		List<List<T>> partitions = new LinkedList<List<T>>();
		List<T> element = new LinkedList<T>();
		int i = 0;
		Iterator<T> iter = collection.iterator();
		while (iter.hasNext()) {
			element.add(iter.next());
			i++;
			if (i % size == 0) {
				element = new LinkedList<T>();
				partitions.add(element);
			}
		}
		if ((i % size != 0)) {
			partitions.add(element);
		}
		return partitions;
	}*/
	
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
}
