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
	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private Permutations<Object> permutator;
	//private static final int PERMUTATIONS_BLOCK_SIZE = 10000;
	
	public SimilarityCalculator() {
		
	}

	/*private <E> ArrayList<Object[]> permuteUnique(E[] list) {
		Permutations p = new Permutations(list);
		ArrayList<Object[]> permutations = new ArrayList<Object[]>();
		while (p.hasNext()) { 
			permutations.add(p.next().clone());
		}
		return permutations;
	}
	
	private synchronized <E> ArrayList<Object[]> getNextNPermutations(int n) {
		ArrayList<Object[]> permutations = new ArrayList<Object[]>();
		int currentPermutation = 0;
		while (permutator.hasNext() && currentPermutation < n) { 
			permutations.add(permutator.next().clone());
			currentPermutation++;
		}
		return permutations;
	}*/
	
	private void getSimilarity(final List<ISchemaType> sourceTypes, Object[] targetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		permutator = new Permutations<>(targetTypes);
		/*ArrayList<Object[]> permutations = getNextNPermutations(PERMUTATIONS_BLOCK_SIZE);
		int size = permutations.size();
		while (permutations.size() > 0) {
			permutations= getNextNPermutations(PERMUTATIONS_BLOCK_SIZE);
			size+= permutations.size();
		}
		System.out.println("Tamaño Final: " + size);*/
		List<Future<?>> futures = new LinkedList<Future<?>>();
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			futures.add(executor.submit(new Runnable() {	
				@Override
				public void run() {
					ParameterCombination combination = new ParameterCombination();
					Object[] permutation = null;
					synchronized (permutator) {
						if (permutator.hasNext())
							permutation = permutator.next();
					}
					
					while (permutation != null) {
							combination.setSourceParameters(sourceTypes);
							combination.setTargetParameters(permutation);
							combination.setSourceReturnType(sourceReturnType);
							combination.setTargetReturnType(targetReturnType);
							combination.calculateSimilarity();
							synchronized (this) {
								if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
									mostSimilarCombination = (ParameterCombination) combination.clone();
								}
							}
							synchronized (permutator) {
								if (permutator.hasNext())
									permutation = permutator.next();
								else
									permutation = null;
							}
					}
					/*ArrayList<Object[]> permutations = getNextNPermutations(PERMUTATIONS_BLOCK_SIZE);
					while (permutations.size() > 0) {
						for (Object[] array : permutations) {
							combination.setSourceParameters(sourceTypes);
							combination.setTargetParameters(array);
							combination.setSourceReturnType(sourceReturnType);
							combination.setTargetReturnType(targetReturnType);
							combination.calculateSimilarity();
							if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
								mostSimilarCombination = (ParameterCombination) combination.clone();
							}
						}
						permutations = getNextNPermutations(PERMUTATIONS_BLOCK_SIZE);
					}*/
				}
			}));
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
	
	public ParameterCombination getMaxSimilarity(final ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final Object[] targetTypes = initialCombination.getTargetParameters();
		mostSimilarCombination= new ParameterCombination();
		if (sourceTypes.size() <= targetTypes.length) {
			getSimilarity(sourceTypes, targetTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		else {
			final List<ISchemaType> target = new LinkedList<ISchemaType>();
			for(int i = 0; i < targetTypes.length; i++){
				target.add((ISchemaType) targetTypes[i]);
			}
			getSimilarity(target, sourceTypes.toArray(), initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		return mostSimilarCombination;
	}
	
	@Override
	protected void finalize() {
		executor.shutdownNow();
	}
}
