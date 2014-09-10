package com.isistan.loaders;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.isistan.stroulia.Runner;
import com.isistan.structure.similarity.ComplexTypeOrdering;
import com.isistan.structure.similarity.IOperation;
import com.isistan.structure.similarity.ISchemaType;
import com.isistan.structure.similarity.SchemaComplexType;
import com.isistan.structure.similarity.SchemaSimpleType;
import com.isistan.structure.similarity.SimpleOperation;

@SuppressWarnings("rawtypes")
public class TypeClassLoader implements ITypeLoader{
	
	private String classPath;
	
	public TypeClassLoader(String classPath) {
		this.classPath = classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}
	
	public String getClassPath() {
		return classPath;
	}
	
	@Override
	public Collection<IOperation> load(File file) {
		URL urls[] = new URL[1];
		Collection<IOperation> operations = new LinkedList<IOperation>();
		try {
			urls[0] = file.toURI().toURL();
			URLClassLoader classLoader = new URLClassLoader(urls);
			try {
				Class classType = classLoader.loadClass(classPath);
				Method[] meth = classType.getDeclaredMethods();
				for (int i = 0; i < meth.length; i++) {
					SimpleOperation currentOperation = new SimpleOperation();
					currentOperation.setName(meth[i].getName());
					Class [] parameterTypes = meth[i].getParameterTypes();
					for (int j = 0; j < parameterTypes.length; j++) {
						currentOperation.addParameter(getTypes(parameterTypes[j]));
					}
					operations.add(currentOperation);
				}
				
			try {
				classLoader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			} catch (ClassNotFoundException e) {
				Logger.getLogger(Runner.LOADER_LOG).fatal("Class Loader Error - missing class: " + classPath);
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return operations;
	}
	

	private ISchemaType getTypes(Class type) {
		ISchemaType returnType = null;
		if (type.isPrimitive() || type.getCanonicalName().equals(String.class.getCanonicalName())) {
			returnType = new SchemaSimpleType(PrimitiveType.valueOf(type.getSimpleName().toUpperCase()));
		}
		else if (!type.isEnum()){
			Field[] subTypes = type.getFields();
			SchemaComplexType complexType = new SchemaComplexType();
			complexType.setName(type.getSimpleName());
			complexType.setOrdering(ComplexTypeOrdering.SECUENCE);
			for (int i = 0; i < subTypes.length; i++) {
				complexType.addSubType(getTypes(subTypes[i].getType()));
			}
			returnType = complexType;
		}
		return returnType;
	}
}
