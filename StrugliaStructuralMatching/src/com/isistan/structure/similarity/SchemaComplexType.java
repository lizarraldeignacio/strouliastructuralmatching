package com.isistan.structure.similarity;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.isistan.loaders.StrouliaMatchingProperties;
import com.isistan.loaders.StrouliaPropertyName;

public class SchemaComplexType implements ISchemaType{
	
	private String name;
	private String namespace;
	private ComplexTypeOrdering ordering;
	private Collection<ISchemaType> subTypes;
	

	public ComplexTypeOrdering getOrdering() {
		return ordering;
	}

	public void setOrdering(ComplexTypeOrdering ordering) {
		this.ordering = ordering;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SchemaComplexType() {
		subTypes = new LinkedList<ISchemaType>();
	}
	
	public void addSubType(ISchemaType type) {
		subTypes.add(type);
	}
	
	public Collection<ISchemaType> getSubTypes() {
		return new LinkedList<ISchemaType>(subTypes);
	}
	
	@Override
	public float similarity(ISchemaType type) {
		return complexTypeSimilarity(this);
	}

	@Override
	public float simpleTypeSimilarity(SchemaSimpleType simpleType) {
		float max = 0;
		//Máximo de comparar el tipo simple contra los demás
		for (ISchemaType type : subTypes) {
			float similarity = type.simpleTypeSimilarity(simpleType);
			if (similarity > max) {
				max = similarity;
			}
		}
		return max;
	}

	@Override
	public float complexTypeSimilarity(SchemaComplexType complexType) {
		//Máximo de la suma de las comparaciones entre todos los sub tipos
		Collection<ISchemaType> externalTypeSubTypes = complexType.getSubTypes();
		Iterator<ISchemaType> externalTypeIterator = externalTypeSubTypes.iterator();
		Iterator<ISchemaType> internalTypeIterator = subTypes.iterator();
		float maxSimilarityArray[] = new float[subTypes.size()];
		float max = 0;
		
		//Inicializo el array en 0
		for (int i = 0; i < maxSimilarityArray.length; i++)
			maxSimilarityArray[i] = 0;
		
		int i = 0;
		//Obtengo los valores de máxima similitud entre tipos
		while (internalTypeIterator.hasNext()) {
			ISchemaType internalSubType = internalTypeIterator.next();
			while (externalTypeIterator.hasNext()) {
				ISchemaType externalSubType = externalTypeIterator.next();
				float similarity = internalSubType.similarity(externalSubType);
				if (similarity > maxSimilarityArray[i])
					maxSimilarityArray[i] = similarity;
			}
			i++;
		}
		

		//Calcúlo el valor final de similitud sumando la máxima similitud de cada tipo
		for (i = 0; i < maxSimilarityArray.length; i++)
			max += maxSimilarityArray[i];
		
		Float bonus = Float.parseFloat(StrouliaMatchingProperties.instance().getProperty(StrouliaPropertyName.ORDERING_BONUS));
		if (this.ordering != null) {
			return this.ordering.equals(complexType.getOrdering()) ? max + bonus : max;
		}
		else {
			return max;
		}
		
	}
	
	@Override
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append("*******ComplexType*******" + System.lineSeparator());
		string.append("ComplexTypeName: " + this.name + System.lineSeparator());
		string.append("ComplexTypeNamespace: " + this.namespace + System.lineSeparator());
		string.append("ComplexTypeOrdering: " + this.ordering.toString() + System.lineSeparator());
		string.append("*********Subtypes********" + System.lineSeparator());
		for (ISchemaType subtype : subTypes) {
			string.append(subtype.toString());
		}
		string.append("*********EndSubtypes********" + System.lineSeparator());
		return string.toString();
	}
}
