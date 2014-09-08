package com.isistan.loaders;

import java.io.File;
import java.util.Collection;

import com.isistan.structure.similarity.IOperation;

public interface ITypeLoader {
	
	public Collection<IOperation> load(File file);

}
