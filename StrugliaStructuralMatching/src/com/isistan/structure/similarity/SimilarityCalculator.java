package com.isistan.structure.similarity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import com.isistan.util.Permutations;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private ParameterCombination mostSimilarCombination;
	
	public SimilarityCalculator() {
		
	}

	private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> initialTargetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		ISchemaType[] targetTypes = new ISchemaType[initialTargetTypes.size()];
		initialTargetTypes.toArray(targetTypes);
		Permutations<ISchemaType> permIter = new Permutations<ISchemaType>(targetTypes);
		while (permIter.hasNext()) {
			ParameterCombination combination = new ParameterCombination(sourceTypes, Arrays.asList(permIter.next()), sourceReturnType, targetReturnType, 0);
			combination.calculateSimilarity();
			if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
				mostSimilarCombination = (ParameterCombination) combination.clone();
			}
		}
	}
	
	public ParameterCombination getMaxSimilarity(final ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		mostSimilarCombination = new ParameterCombination();
		if (sourceTypes.size() <= targetTypes.size()) {
			getSimilarity(sourceTypes, targetTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		else {
			getSimilarity(targetTypes, sourceTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		return mostSimilarCombination;
	}
}
