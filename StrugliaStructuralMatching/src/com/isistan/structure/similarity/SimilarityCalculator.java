package com.isistan.structure.similarity;

import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Collections2;

public class SimilarityCalculator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2012752801233568847L;
	private ParameterCombination mostSimilarCombination;
	private TObjectByteHashMap<ISchemaType> byteMap;
	private TByteObjectHashMap<ISchemaType> reverseByteMap;
	
	public SimilarityCalculator() {
		
		byteMap = new TObjectByteHashMap<ISchemaType>();
		reverseByteMap = new TByteObjectHashMap<ISchemaType>();
	}

	private void getSimilarity(final ArrayList<ISchemaType> sourceTypes, final ArrayList<ISchemaType> initialTargetTypes, final ISchemaType sourceReturnType, final ISchemaType targetReturnType) {
		Collection<List<Byte>> permutations = Collections2.permutations((Collection<Byte>) byteArrayMapping(initialTargetTypes));
		HashSet<List<Byte>> permutationsSet = new HashSet<List<Byte>>();
		for (List<Byte> list : permutations) {
			if (permutationsSet.add(list)) {
				ParameterCombination combination = new ParameterCombination(sourceTypes, reverseByteArrayMapping(list), sourceReturnType, targetReturnType, 0);
				combination.calculateSimilarity();
				if (combination.getSimilarity() > mostSimilarCombination.getSimilarity()) {
					mostSimilarCombination = (ParameterCombination) combination.clone();
				}
			}
		}
	}
	
	private ArrayList<ISchemaType> reverseByteArrayMapping(List<Byte> list) {
		ArrayList<ISchemaType> reverseArray = new ArrayList<ISchemaType>();
		for (int i = 0; i < list.size(); i++) {
			reverseArray.add(reverseByteMap.get(list.get(i)));
		}
		return reverseArray;
	}
	
	private ArrayList<Byte> byteArrayMapping(ArrayList<ISchemaType> types) {
		byteMap.clear();
		reverseByteMap.clear();
		ArrayList<Byte> byteMapping = new ArrayList<Byte>(types.size());
		byte index = 0;
		for (int i = 0; i < types.size(); i++) {
			ISchemaType iSchemaType = types.get(i);
			if (!byteMap.contains(iSchemaType)) {
				byteMap.put(iSchemaType, index);
				reverseByteMap.put(index, iSchemaType);
				index++;
			}
			byteMapping.add(byteMap.get(iSchemaType));
		}
		return byteMapping;
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
