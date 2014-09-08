package com.isistan.structure.similarity;

import com.isistan.loaders.PrimitiveType;
import com.isistan.loaders.StrugliaMatchingProperties;

public class SchemaSimpleType implements ISchemaType{

	private PrimitiveType type;
	
	public SchemaSimpleType(PrimitiveType type) {
		this.type = type;
	}
	
	@Override
	public float similarity(ISchemaType type) {
		return type.simpleTypeSimilarity(this);
	}
	
	public PrimitiveType getType(){
		return type;
	}

	@Override
	public float simpleTypeSimilarity(SchemaSimpleType type) {
		return StrugliaMatchingProperties.instance().getPrimitiveTypeCompatibility(this.type, type.getType());
	}

	@Override
	public float complexTypeSimilarity(SchemaComplexType type) {
		//Cuidado
		return type.simpleTypeSimilarity(this);
	}
	
	@Override
	public String toString() {
		return new String("-------SimpleType-------" + System.lineSeparator() + "SimpleTypeName: " + type.toString() + System.lineSeparator());
	}
}
