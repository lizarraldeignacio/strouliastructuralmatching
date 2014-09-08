package com.isistan.structure.similarity;

import java.util.Collection;
import java.util.LinkedList;

public class SimpleOperation implements IOperation{

	private String name;
	private ISchemaType returnType;
	private Collection<ISchemaType> parameters;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SimpleOperation() {
		parameters = new LinkedList<ISchemaType>();
	}
	
	public void setReturnType(ISchemaType type) {
		returnType = type;
	}
	
	public void addParameter(ISchemaType type) {
		parameters.add(type);
	}
	
	public void addAllParameters(Collection<ISchemaType> types) {
		parameters = types;
	}
	
	public ISchemaType getReturnType() {
		return returnType;
	}
	
	public Collection<ISchemaType> getParameters() {
		return new LinkedList<ISchemaType>(parameters);
	}
	
	
	@Override
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append("OperationName: " + name + System.lineSeparator());
		string.append("#######OperationReturnType#######" + System.lineSeparator());
		string.append(returnType.toString());
		string.append("#######EndOperationReturnType#######" + System.lineSeparator());
		string.append("#######OperationParameterTypes#######" + System.lineSeparator());
		for (ISchemaType parameter : parameters) {
			string.append(parameter.toString());
		}
		string.append("#######EndOperationParameterTypes#######" + System.lineSeparator());
		return string.toString();
	}

	@Override
	public Collection<ParameterCombination> similarityRank(IOperation operation) {
		ParameterCombination initialCombination = new ParameterCombination((LinkedList<ISchemaType>)parameters, (LinkedList<ISchemaType>)operation.getParameters(), returnType, ((SimpleOperation)operation).getReturnType());
		SimilarityCalculator calculator = new SimilarityCalculator();
		return calculator.similarityRank(initialCombination);
	}
	
	@Override
	public ParameterCombination getMaxSimilarity(IOperation operation) {
		ParameterCombination initialCombination = new ParameterCombination((LinkedList<ISchemaType>)parameters, (LinkedList<ISchemaType>)operation.getParameters(), returnType, ((SimpleOperation)operation).getReturnType());
		SimilarityCalculator calculator = new SimilarityCalculator();
		return calculator.getMaxSimilarity(initialCombination);
	}
}