package com.isistan.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.ow2.easywsdl.schema.api.All;
import org.ow2.easywsdl.schema.api.ComplexType;
import org.ow2.easywsdl.schema.api.Element;
import org.ow2.easywsdl.schema.api.Restriction;
import org.ow2.easywsdl.schema.api.Sequence;
import org.ow2.easywsdl.schema.api.SimpleType;
import org.ow2.easywsdl.schema.api.Type;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.BindingOperation;
import org.ow2.easywsdl.wsdl.api.Output;
import org.ow2.easywsdl.wsdl.api.Part;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl;
import org.xml.sax.InputSource;

import com.isistan.stroulia.Runner;
import com.isistan.structure.similarity.ComplexTypeOrdering;
import com.isistan.structure.similarity.IOperation;
import com.isistan.structure.similarity.ISchemaType;
import com.isistan.structure.similarity.SchemaComplexType;
import com.isistan.structure.similarity.SchemaSimpleType;
import com.isistan.structure.similarity.SimpleOperation;

public class WSDLLoader implements ITypeLoader{

	private HashMap<String, ISchemaType> matchedTypes;
	
	public WSDLLoader() {
		matchedTypes = new HashMap<String, ISchemaType>();
	}
	
	@Override
	public Collection<IOperation> load(File file) {
		try {
			WSDLReader reader;
			reader = WSDLFactory.newInstance().newWSDLReader();			
			try {
				FileInputStream wsdlInStream;
				try {
					wsdlInStream = new FileInputStream(file);
					DescriptionImpl desc = (DescriptionImpl) reader.read(new InputSource(wsdlInStream));
					if (desc.getBindings().size() > 0) {
						List<BindingOperation> bindingOperations = desc.getBindings().get(0).getBindingOperations();
						Collection <IOperation> operations = new LinkedList<IOperation>();
					 	for (BindingOperation bindingOperation : bindingOperations) {
							QName inputMessageName = bindingOperation.getOperation().getInput().getMessageName();
							List<Part> inputMessageParts = desc.getMessage(inputMessageName).getParts();
							Output output = bindingOperation.getOperation().getOutput();
							Collection<ISchemaType> inputTypes = new LinkedList<ISchemaType>();
							Element inputElement = null;
							if (inputMessageParts != null && inputMessageParts.size() > 0 ) {
								if (inputMessageParts.get(0) != null) {
									if (desc.getTypes() != null) {
										inputElement = inputMessageParts.get(0).getElement();
									}
								}
							}
							
							SimpleOperation operation = new SimpleOperation();
							operation.setName(bindingOperation.getQName().getLocalPart());
							//Los parametros vienen encapsulados en un tipo complejo
							if (inputMessageParts.size() == 1 && inputElement != null) {
								inputTypes = getEncapsulatedTypes(inputElement);
								operation.addAllParameters(inputTypes);

							}
							else if (inputMessageParts.size() > 1) {
								Iterator<Part> messagePartsIterator = inputMessageParts.iterator();
								while (messagePartsIterator.hasNext()) {
									Type type = messagePartsIterator.next().getType();
									if (type != null) {
										ISchemaType schemaType = this.getUniqueTypes(type);
										inputTypes.add(schemaType);
									}
								}
								operation.addAllParameters(inputTypes);
							}
							
							//El retorno viene encapsulado en un tipo complejo
							if (output != null) {
								QName outputMessageName = output.getMessageName();
								List<Part> outputMessageParts = desc.getMessage(outputMessageName).getParts();
								Collection<ISchemaType> outputTypes = new LinkedList<ISchemaType>();
								Element outputElement = null;
								if (outputMessageParts != null && outputMessageParts.size() > 0 ) {
									if (outputMessageParts.get(0) != null) {
										if (desc.getTypes() != null) {
											outputElement = outputMessageParts.get(0).getElement();
										}
									}
								}
								if (outputElement != null) {
									outputTypes = getEncapsulatedTypes(outputElement);
									Iterator<ISchemaType> outIter = outputTypes.iterator();
									if (outIter.hasNext())
										operation.setReturnType(outIter.next());
								}
							}
							
							operations.add(operation);
					 	}
					 	return operations;
					}
					try {
						wsdlInStream.close();	
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 	
				} catch (FileNotFoundException|org.ow2.easywsdl.wsdl.api.WSDLException|java.lang.ClassCastException e) {
					if (e instanceof FileNotFoundException) {
						Logger.getLogger(Runner.LOADER_LOG).fatal("WSDL Loader Error - missing wsdl: " + file.getAbsolutePath());
					}
					else if (e instanceof org.ow2.easywsdl.wsdl.api.WSDLException) {
						Logger.getLogger(Runner.LOADER_LOG).fatal("WSDL Loader Error - parser error: " + e.getMessage() + " " + file.getAbsolutePath());
					}
					else if (e instanceof java.lang.ClassCastException) {
						Logger.getLogger(Runner.LOADER_LOG).fatal("WSDL Loader Error - invalid wsdl: " + e.getMessage() + " " + file.getAbsolutePath());
					}
					return null;
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
			}
			catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} catch (WSDLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ISchemaType getUniqueTypes(Type type) {
		matchedTypes.clear();
		return getTypes(type);
	}
	
	
	private Collection<ISchemaType> getEncapsulatedTypes(Element elem) {
		Collection<ISchemaType> inputTypes = new LinkedList<ISchemaType>();
		if (elem.getType() instanceof ComplexType) {
			ComplexType parameters = (ComplexType) elem.getType();
			Sequence sec = parameters.getSequence();
			All all = parameters.getAll();
			List<Element> elements = null;
			elements = sec != null ? sec.getElements() : all != null ? all.getElements() : null;
			if (elements != null) {
				for (Element element : elements) {
					ISchemaType type = this.getUniqueTypes(element.getType());
					if (type != null) {
						inputTypes.add(type);
					}
				}
			}
		}
		else {
			ISchemaType type = this.getUniqueTypes(elem.getType());
			if (type != null) {
				inputTypes.add(type);
			}
		}
		return inputTypes;
	}
	
	
	private ISchemaType getTypes(Type type) {
		ISchemaType returnType = null;
		if (type != null) {
			if (type instanceof SimpleType) {
				//Retorno el tipo simple
				SimpleType simpleType = (SimpleType) type;
				Restriction restriction = (Restriction) simpleType.getRestriction();
				String primitiveTypeName;
				primitiveTypeName = (simpleType.getQName() != null) ? simpleType.getQName().getLocalPart().toUpperCase() : "";
				if (simpleType.getRestriction() != null && !restriction.getBase().getLocalPart().toUpperCase().equals("ANYSIMPLETYPE")) {
					primitiveTypeName = restriction.getBase().getLocalPart().toUpperCase();
				}
				try {
					PrimitiveType primitiveType = PrimitiveType.valueOf(primitiveTypeName);
					if (primitiveType != null) {
						returnType = new SchemaSimpleType(primitiveType);
					}
				}
				catch (IllegalArgumentException e) {
					Logger.getLogger(Runner.LOADER_LOG).fatal("WSDL Loader Error - primitive type not found: " + primitiveTypeName);
				}
			}
			else if (type instanceof ComplexType) {
				String complexTypeName = type.getQName() != null? type.getQName().getLocalPart() : null;
				if(complexTypeName != null && matchedTypes.containsKey(complexTypeName.toLowerCase())) {
					if (complexTypeName != null) {
						returnType = matchedTypes.get(complexTypeName.toLowerCase());
					}
				}
				else if (complexTypeName != null && !matchedTypes.containsKey(complexTypeName)) {
					ComplexType complexType = (ComplexType) type;
					SchemaComplexType schemaComplexType = new SchemaComplexType();
					if (complexTypeName != null) {
						matchedTypes.put(complexTypeName.toLowerCase(), schemaComplexType);
					}
					QName complexName = complexType.getQName();
					schemaComplexType.setName(complexName == null ? "" : complexName.getLocalPart());
					Sequence sec = complexType.getSequence();
					All all = complexType.getAll();
					List<Element> elements = null;
					if (sec != null) {
						schemaComplexType.setOrdering(ComplexTypeOrdering.SECUENCE);
						elements = sec.getElements();
					}
					else if (all != null) {
						schemaComplexType.setOrdering(ComplexTypeOrdering.ALL);
						elements = all.getElements();
					}
					/**else {
						
						 * CASO EN EL QUE NO HAY ORDEN
						 
					}*/
					if (elements != null) {
						for (Element element : elements) {
							ISchemaType subType = this.getTypes(element.getType());
							if (subType != null) {
								schemaComplexType.addSubType(subType);
							}
						}
					}
					returnType = schemaComplexType;
				}
			}
		}
		return returnType;
	}
}
