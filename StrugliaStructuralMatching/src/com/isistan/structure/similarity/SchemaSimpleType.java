package com.isistan.structure.similarity;

import org.gridgain.grid.GridException;
import org.gridgain.grid.GridGain;

import com.isistan.loaders.PrimitiveType;
import com.isistan.loaders.StrouliaMatchingProperties;
import com.isistan.stroulia.Runner;

public class SchemaSimpleType implements ISchemaType{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8334051693339495352L;
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
		StrouliaMatchingProperties properties = null;
		try {
			properties = (StrouliaMatchingProperties) GridGain.grid().cache(Runner.GRID_CACHE_NAME).get(Runner.GridCacheObjects.PROPERTIES);
		} catch (GridException e) {
			e.printStackTrace();
		}
		if (properties == null) {
			throw new NullPointerException("Couldn't get properties structure");
		}
		return properties.getPrimitiveTypeCompatibility(this.type, type.getType());
	}

	@Override
	public float complexTypeSimilarity(SchemaComplexType type) {
		//Cuidado
		return type.simpleTypeSimilarity(this);
	}
	
	@Override
	public int hashCode() {
		return this.getType().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		ISchemaType type = (ISchemaType) obj;
		return type.getTypeName().equals(this.getTypeName());
	}
	
	@Override
	public String toString() {
		return new String("-------SimpleType-------" + System.lineSeparator() + "SimpleTypeName: " + type.toString() + System.lineSeparator());
	}

	@Override
	public String getTypeName() {
		return this.type.toString();
	}
}
