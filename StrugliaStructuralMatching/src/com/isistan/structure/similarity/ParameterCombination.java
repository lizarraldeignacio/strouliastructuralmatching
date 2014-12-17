package com.isistan.structure.similarity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ParameterCombination implements Cloneable, Comparable<ParameterCombination>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3017260158307953234L;
	private LinkedList<ISchemaType> sourceParameters;
	private LinkedList<ISchemaType> targetParameters;
	private ISchemaType sourceReturnType;
	private ISchemaType targetReturnType;
	private float similarity = 0;
	
	public ParameterCombination() {
		sourceParameters = new LinkedList<ISchemaType>();
		targetParameters = new LinkedList<ISchemaType>();
	}
	
	public ISchemaType getSourceReturnType() {
		return sourceReturnType;
	}

	public void setSourceReturnType(ISchemaType sourceReturnType) {
		this.sourceReturnType = sourceReturnType;
	}

	public ISchemaType getTargetReturnType() {
		return targetReturnType;
	}

	public void setTargetReturnType(ISchemaType targetReturnType) {
		this.targetReturnType = targetReturnType;
	}

	public ParameterCombination(List<ISchemaType> sourceParameters, List<ISchemaType> targetParameters, ISchemaType sourceReturnType, ISchemaType targetReturnType) {
		this.sourceParameters = new LinkedList<ISchemaType>(sourceParameters);
		this.targetParameters = new LinkedList<ISchemaType>(targetParameters);
		this.sourceReturnType = sourceReturnType;
		this.targetReturnType = targetReturnType;
	}
	

	public void calculateSimilarity() {
		Iterator<ISchemaType> sourceTypesIterator = sourceParameters.iterator();
		Iterator<ISchemaType> targetTypesIterator = targetParameters.iterator();
		similarity = 0;
		if (sourceReturnType != null && targetReturnType != null) {
			similarity += sourceReturnType.similarity(targetReturnType);
		}
		while (sourceTypesIterator.hasNext() && targetTypesIterator.hasNext()) {
			ISchemaType sType= sourceTypesIterator.next();
			ISchemaType tType= targetTypesIterator.next();
			similarity += sType.similarity(tType);
		}
	}
	
	public float getSimilarity() {
		return this.similarity;
	}
	
	public Collection<ISchemaType> getSourceParameters() {
		return sourceParameters;
	}
	
	
	public void addSourceParameter(ISchemaType parameter) {
		sourceParameters.add(parameter);
	}
	
	public void addFirstTargetParameter(ISchemaType parameter) {
		targetParameters.addFirst(parameter);
	}
	
	public void addFirstSourceParameter(ISchemaType parameter) {
		sourceParameters.addFirst(parameter);
	}
	
	public void addTargetParameter(ISchemaType parameter) {
		targetParameters.add(parameter);
	}
	
	public void removeLastTargetParameter() {
		targetParameters.removeLast();
	}
	
	public void removeLastSourceParameter() {
		sourceParameters.removeLast();
	}
	
	public ISchemaType popTargetParameter() {
		return targetParameters.pop();
	}
	
	public ISchemaType popSourceParameter() {
		return sourceParameters.pop();
	}
	
	public Collection<ISchemaType> getTargetParameters() {
		return targetParameters;
	}
	
	@Override
	public Object clone() {
		return new ParameterCombination(sourceParameters, targetParameters, sourceReturnType, targetReturnType);
	}

	@Override
	public int compareTo(ParameterCombination arg0) {
		if (this.similarity > arg0.getSimilarity()) {
			return 1;
		}
		else if (this.similarity < arg0.getSimilarity()) {
			return -1;
		}
		return 0;
	}
}
