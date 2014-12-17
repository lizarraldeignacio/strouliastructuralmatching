package com.isistan.structure.similarity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private List<ParameterCombination> combinations;
	private ParameterCombination partialSolution;
	private ParameterCombination mostSimilarCombination;
	
	public SimilarityCalculator() {
		
	}
	
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
	
	public ParameterCombination getMaxSimilarity(final ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		combinations.clear();
		partialSolution = new ParameterCombination();
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
