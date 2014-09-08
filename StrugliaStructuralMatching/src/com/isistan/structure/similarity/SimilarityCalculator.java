package com.isistan.structure.similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimilarityCalculator {
	
	private List<ParameterCombination> combinations;
	private ParameterCombination partialSolution;
	private ParameterCombination mostSimilarCombination;
	
	
	public SimilarityCalculator() {
		combinations = new LinkedList<ParameterCombination>();
	}
	
	public Collection<ParameterCombination> similarityRank(ParameterCombination initialCombination) {
		ArrayList<ISchemaType> sourceTypes = new ArrayList<ISchemaType>(initialCombination.getSourceParameters());
		ArrayList<ISchemaType> targetTypes = new ArrayList<ISchemaType>(initialCombination.getTargetParameters());
		combinations.clear();
		partialSolution = new ParameterCombination();
		if (sourceTypes.size() <= targetTypes.size()) {
			getRank(sourceTypes, targetTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		else {
			getRank(targetTypes, sourceTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
		}
		Collections.sort(combinations, Collections.reverseOrder());
		return combinations;
	}
	
	
	
	private void getRank(ArrayList<ISchemaType> sourceTypes, ArrayList<ISchemaType> targetTypes, ISchemaType sourceReturnType, ISchemaType targetReturnType){
		if (sourceTypes.size() == 0) {
			ParameterCombination combination = (ParameterCombination) partialSolution.clone();
			combination.setSourceReturnType(sourceReturnType);
			combination.setTargetReturnType(targetReturnType);
			combination.calculateSimilarity();
			combinations.add(combination);
			return;
		}
		ISchemaType targetType;
		ISchemaType sourceType = sourceTypes.remove(0);
		partialSolution.addSourceParameter(sourceType);
		
		for (int j = 0; j < targetTypes.size(); j++) {
			targetType = targetTypes.get(j);
			targetTypes.remove(j);
			partialSolution.addTargetParameter(targetType);
			getRank(sourceTypes, targetTypes, sourceReturnType, targetReturnType);
			partialSolution.removeLastTargetParameter();
			targetTypes.add(j, targetType);
		}
		
		partialSolution.removeLastSourceParameter();
		sourceTypes.add(0, sourceType);
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
		ExecutorService excecutor = Executors.newSingleThreadExecutor();
		Runnable timedTask = new Runnable() {
			@Override
			public void run() {
				if (sourceTypes.size() <= targetTypes.size()) {
					getSimilarity(sourceTypes, targetTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
				}
				else {
					getSimilarity(targetTypes, sourceTypes, initialCombination.getSourceReturnType(), initialCombination.getTargetReturnType());
				}
			}
		};
		try {
			excecutor.submit(timedTask).get(3, TimeUnit.MINUTES);
			excecutor.shutdownNow();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			Logger.getAnonymousLogger().log(Level.WARNING, "TAREA CANCELADA - TIEMPO AGOTADO");
			//e.printStackTrace();
			return null;
		}
		return mostSimilarCombination;
	}
}
