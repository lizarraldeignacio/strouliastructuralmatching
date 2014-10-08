package com.isistan.structure.similarity;

import java.io.Serializable;

public interface ISchemaType extends Serializable{
	
	public float similarity(ISchemaType type);
	
	public float simpleTypeSimilarity(SchemaSimpleType type);
	
	public float complexTypeSimilarity(SchemaComplexType type);
}
