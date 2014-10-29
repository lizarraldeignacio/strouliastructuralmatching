package com.isistan.structure.similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.isistan.util.Permutations;

public class SimilarityCalculator {
	
	private List<ParameterCombination> combinations;
	private ParameterCombination mostSimilarCombination;
	
	
	public SimilarityCalculator() {
		combinations = new LinkedList<ParameterCombination>();
	}

	private void getSimilarity(ArrayList<ISchemaType> sourceTypes, ArrayList<ISchemaType> initialTargetTypes, ISchemaType sourceReturnType, ISchemaType targetReturnType) {
			ISchemaType[] targetTypes = new ISchemaType[initialTargetTypes.size()];
			initialTargetTypes.toArray(targetTypes);
			Permutations<ISchemaType> permIter = new Permutations<ISchemaType>(targetTypes);
			while (permIter.hasNext()) {
				ParameterCombination combination = new ParameterCombination(sourceTypes, Arrays.asList(permIter.next()), sourceReturnType, targetReturnType);
				combination.calculateSimilarity();
				if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
					mostSimilarCombination = (ParameterCombination) combination.clone();
				}
			}
	}
	
	public ParameterCombination getMaxSimilarity(final ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		combinations.clear();
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
