package com.isistan.structure.similarity;

import java.util.Collection;

public interface IOperation {

	public ParameterCombination getMaxSimilarity(IOperation operation);
	public Collection<ISchemaType> getParameters();
}
