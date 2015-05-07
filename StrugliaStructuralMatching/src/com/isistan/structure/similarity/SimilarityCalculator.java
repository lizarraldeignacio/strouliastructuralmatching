package com.isistan.structure.similarity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.html.parser.TagElement;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private ParameterCombination partialSolution;
	private ParameterCombination mostSimilarCombination;
	private HashSet<List<ISchemaType>> partialCombinations;
	
	public SimilarityCalculator() {
		partialCombinations = new HashSet<List<ISchemaType>>();
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
			while (partialCombinations.contains(partialSolution.getTargetParameters()) && j < targetTypes.size()) {
				partialSolution.removeLastTargetParameter();
				targetTypes.add(j, targetType);
				j++;
				partialSolution.addTargetParameter(targetTypes.get(j));
				targetTypes.remove(j);
			}
			if (j >= targetTypes.size()) {
				break;
			}
			partialCombinations.add(new LinkedList<ISchemaType>(partialSolution.getTargetParameters()));
			getSimilarity(sourceTypes, targetTypes, sourceReturnType, targetReturnType);
			partialSolution.removeLastTargetParameter();
			if (j > targetTypes.size()) {
				targetTypes.add(targetType);
			}
			else {
				targetTypes.add(j, targetType);
			}
		}
		
		partialSolution.removeLastSourceParameter();
		sourceTypes.add(0, sourceType);
	}
	
	public ParameterCombination getMaxSimilarity(ParameterCombination initialCombination) {
		final ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		final ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		partialCombinations.clear();
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
