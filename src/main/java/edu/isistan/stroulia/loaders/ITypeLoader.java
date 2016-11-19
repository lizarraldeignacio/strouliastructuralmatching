package edu.isistan.stroulia.loaders;

import java.io.File;
import java.util.Collection;

import edu.isistan.stroulia.structure.similarity.IOperation;

public interface ITypeLoader {
	
	public Collection<IOperation> load(File file);

}
