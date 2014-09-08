package com.isistan.structure.similarity;

public interface ISchemaType {
	
	public float similarity(ISchemaType type);
	
	public float simpleTypeSimilarity(SchemaSimpleType type);
	
	public float complexTypeSimilarity(SchemaComplexType type);
}
