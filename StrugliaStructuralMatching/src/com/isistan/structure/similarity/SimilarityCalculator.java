package com.isistan.structure.similarity;

import com.google.common.collect.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private ParameterCombination mostSimilarCombination;
	
	public SimilarityCalculator() {
		
	}

	private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> targetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		Collection<List<ISchemaType>> permutations = Collections2.permutations(targetTypes);
		for (List<ISchemaType> list : permutations) {
			ParameterCombination combination = new ParameterCombination(sourceTypes, list.subList(0, sourceTypes.size()), sourceReturnType, targetReturnType);
			combination.calculateSimilarity();
			if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
				mostSimilarCombination = (ParameterCombination) combination.clone();
			}
		}
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
