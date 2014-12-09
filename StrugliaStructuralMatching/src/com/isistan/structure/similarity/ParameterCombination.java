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
	private List<ISchemaType> sourceParameters;
	private Object[] targetParameters;
	private ISchemaType sourceReturnType;
	private ISchemaType targetReturnType;
	private float similarity = 0;
	
	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public void setSourceParameters(List<ISchemaType> sourceParameters) {
		this.sourceParameters = sourceParameters;
	}

	/*public void setTargetParameters(List<ISchemaType> targetParameters) {
		this.targetParameters = targetParameters;
	}*/

	public void setTargetParameters(Object[] targetParameters) {
		
		this.targetParameters = targetParameters;
	}
	
	public ParameterCombination() {
		sourceParameters = new LinkedList<ISchemaType>();
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

	public ParameterCombination(List<ISchemaType> sourceParameters, List<ISchemaType> targetParameters, ISchemaType sourceReturnType, ISchemaType targetReturnType, float similarity) {
		this.sourceParameters = new LinkedList<ISchemaType>(sourceParameters);
		this.targetParameters = targetParameters.toArray();
		this.sourceReturnType = sourceReturnType;
		this.targetReturnType = targetReturnType;
		this.similarity = similarity;
	}
	
	public ParameterCombination(List<ISchemaType> sourceParameters, Object[] targetParameters, ISchemaType sourceReturnType, ISchemaType targetReturnType, float similarity) {
		this.sourceParameters = new LinkedList<ISchemaType>(sourceParameters);
		this.targetParameters = targetParameters;
		this.sourceReturnType = sourceReturnType;
		this.targetReturnType = targetReturnType;
		this.similarity = similarity;
	}
	
	
	public void calculateSimilarity() {
		Iterator<ISchemaType> sourceTypesIterator = sourceParameters.iterator();
		similarity = 0;
		if (sourceReturnType != null && targetReturnType != null) {
			similarity += sourceReturnType.similarity(targetReturnType);
		}
		int i = 0;
		while (sourceTypesIterator.hasNext() && targetParameters.length < i) {
			ISchemaType sType= sourceTypesIterator.next();
			ISchemaType tType= (ISchemaType) targetParameters[i];
			similarity += sType.similarity(tType);
			i++;
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
	
	
	public Object[] getTargetParameters() {
		return targetParameters;
	}
	
	@Override
	public Object clone() {
		return new ParameterCombination(sourceParameters, targetParameters, sourceReturnType, targetReturnType, this.similarity);
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
