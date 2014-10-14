package com.isistan.structure.similarity;

import com.google.common.collect.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private long JOB_SIZE = 100000;
	
	public SimilarityCalculator() {
		
	}

	/*private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> targetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		Collection<List<ISchemaType>> permutations = Collections2.permutations(targetTypes);
		for (List<ISchemaType> list : permutations) {
			ParameterCombination combination = new ParameterCombination(sourceTypes, list.subList(0, sourceTypes.size()), sourceReturnType, targetReturnType);
			combination.calculateSimilarity();
			if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
				mostSimilarCombination = (ParameterCombination) combination.clone();
			}
		}
	}*/
	
	private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> targetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		Collection<List<ISchemaType>> permutations = Collections2.permutations(targetTypes);
		List<List<List<ISchemaType>>> partitions = this.<List<ISchemaType>>partitions(permutations, JOB_SIZE);
		List<Future> futures = new LinkedList<>();
		for (List<List<ISchemaType>> partition : partitions) {
			final List<List<ISchemaType>> part = new LinkedList<List<ISchemaType>>((Collection<List<ISchemaType>>) partition);
			futures.add(executor.submit(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
						public void run() {
							for (List<ISchemaType> list : part) {
								ParameterCombination combination = new ParameterCombination(sourceTypes, list.subList(0, sourceTypes.size()), sourceReturnType, targetReturnType);
								combination.calculateSimilarity();
								if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
									mostSimilarCombination = (ParameterCombination) combination.clone();
								}
							}
						}
					}));
		 		}
		for (Future f : futures)
		try {
			f.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	
	private <T> List<List<T>> partitions(Collection<T> collection, long size) {
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
			if ((i % size != 0)) {
				partitions.add(element);
			}
		}
		return partitions;
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
}
