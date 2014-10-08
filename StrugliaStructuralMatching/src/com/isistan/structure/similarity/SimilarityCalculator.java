package com.isistan.structure.similarity;

import com.google.common.collect.*;
import com.isistan.stroulia.Runner;
import com.isistan.stroulia.Runner.GridCacheObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.gridgain.grid.GridException;
import org.gridgain.grid.GridGain;
import org.gridgain.grid.cache.GridCache;
import org.gridgain.grid.lang.GridRunnable;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private ExecutorService gridExecutor;
	private GridCache<GridCacheObjects, Object> gridCache;
	private static final long GRID_JOB_SIZE = 1000000;
	
	
	
	public SimilarityCalculator() {
		gridCache = GridGain.grid().cache(Runner.GRID_CACHE_NAME);
		gridExecutor = GridGain.grid().compute().executorService();
	}

	@SuppressWarnings({ "serial", "rawtypes" })
	private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> targetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		Collection<List<ISchemaType>> permutations = Collections2.permutations(targetTypes);
		List<List<List<ISchemaType>>> partitions = this.<List<ISchemaType>>partitions(permutations, GRID_JOB_SIZE);
		List<Future> futures = new LinkedList<>();
		for (List<List<ISchemaType>> partition : partitions) {
			final List<List<ISchemaType>> part = new LinkedList<List<ISchemaType>>((Collection<List<ISchemaType>>) partition);
			futures.add(gridExecutor.submit(new GridRunnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					for (List<ISchemaType> list : part) {
						ParameterCombination combination = new ParameterCombination(sourceTypes, list.subList(0, sourceTypes.size()), sourceReturnType, targetReturnType);
						combination.calculateSimilarity();
						try {
							gridCache.lock(GridCacheObjects.MOST_SIMILAR_COMBINATION, 0);
							ParameterCombination mostSimilarCombination = (ParameterCombination) gridCache.get(GridCacheObjects.MOST_SIMILAR_COMBINATION);
							if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
								gridCache.replace(GridCacheObjects.MOST_SIMILAR_COMBINATION, combination.clone());
							}
							gridCache.unlock(GridCacheObjects.MOST_SIMILAR_COMBINATION);
						} catch (GridException e) {
							e.printStackTrace();
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
		}
		if ((i % size != 0)) {
			partitions.add(element);
		}
		return partitions;
	}
		
	@SuppressWarnings("unchecked")
	public ParameterCombination getMaxSimilarity(final ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		try {
			gridCache.lock(GridCacheObjects.MOST_SIMILAR_COMBINATION, 0);
			gridCache.putx(GridCacheObjects.MOST_SIMILAR_COMBINATION, new ParameterCombination());
			gridCache.unlock(GridCacheObjects.MOST_SIMILAR_COMBINATION);
		} catch (GridException e1) {
			e1.printStackTrace();
		}
		if (sourceTypes.size() <= targetTypes.size()) {
			getSimilarity(sourceTypes, targetTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		else {
			getSimilarity(targetTypes, sourceTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		ParameterCombination mostSimilarCombination;
		try {
			gridCache.lock(GridCacheObjects.MOST_SIMILAR_COMBINATION, 0);
			mostSimilarCombination = (ParameterCombination) gridCache.get(GridCacheObjects.MOST_SIMILAR_COMBINATION);
			gridCache.unlock(GridCacheObjects.MOST_SIMILAR_COMBINATION);
			return mostSimilarCombination;

		} catch (GridException e) {
			e.printStackTrace();
		} 
		return null;
	}
}
