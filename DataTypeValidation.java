package validator;

import java.util.Locale;

import org.w3c.dom.Element;

public class DataTypeValidation {
	
	private Project project;
	private DataTypeHandler data_type_handler;
	private InitialValueParser initial_value_parser;
	
	/**
	 * Constructor
	 * 
	 * @param project
	 */
	public DataTypeValidation(Project project) {
		
		this.project = project;
		
		this.data_type_handler = project.getDataTypeHandler();
		
		this.initial_value_parser = project.getInitialValueParser();
		
	}
	
	/**
	 * Validate data type elements of the given project.
	 * 
	 */
	public void validate() {
	        
		initial_value_parser.resetConstants();

	    for (int i = 0; i < data_type_handler.getNumberOfDataTypes(); i++) {
	        	
	    	Element data_type_element = data_type_handler.getDataTypeElement(i);        
	            
	    	validate_data_type_name_attribute(data_type_element);
	    
	    	validate_base_type(data_type_element);
	    	
	    	validate_initial_value(data_type_element);
	    
	    }
	}

	/**
     * Validate the data types name attribute.
     * 
     * @param data_type_name
     */
    private void validate_data_type_name_attribute(Element data_type_element) { 
    	
    	String data_type_name = data_type_element.getAttribute("name").trim();

        if (data_type_name.trim().equals("")) {
        	
        	Validator.createErrorNode(data_type_element, "The data type name cannot be empty.");
            
        } else if (!IECUtilities.isValidIdentifier(data_type_name)) {
        	
        	Validator.createErrorNode(data_type_element, "The data type name must be a valid identifier.");
           
        } else if (CommonLexer.isKeyword(data_type_name)) {
        	
        	Validator.createErrorNode(data_type_element, "The data type name cannot be a reserved word.");
            
        } else if (IECUtilities.isStandardPouName(data_type_name)){
        	
        	Validator.createErrorNode(data_type_element, "A data type cannot have the same name as a standard POU.");
            
        } else if (project.getPouTypeHandler().isUserPou(data_type_name)) {
            
        	Validator.createErrorNode(data_type_element, "A data type cannot have the same name as a user defined POU.");
            
        } else if (! data_type_handler.isUniqueTypeName(data_type_name)) {
        	
        	Validator.createErrorNode(data_type_element, "The data type name is not unique.");
            
        }
        
    }
    
    /**
     * Base type validations.
     * 
     * @param base_type_element
     */
    private void validate_base_type(Element data_type_element) {
        
    	// The data_type_element must contain only one baseType Element.
	    Element base_type_element = XMLUtilities.findChildElement(data_type_element, "baseType");
	    
	    // Base_type_element must contain only one child element.
	    base_type_element = XMLUtilities.getSingleChildElement(base_type_element);
        
        String s = base_type_element.getTagName();
        
        if ( ! IECUtilities.isAnyElementary(s) ) {
        	
            if (s.equals("derived")) {
            	
                s = base_type_element.getAttribute("name").trim(); //We know that the attribute exists (XML schema)
                
                if (s.equals("")) {
                	
                	Validator.createErrorNode(base_type_element, "Base type name of user defined type cannot be empty.");
                	
                } else {
                    
                    if (! data_type_handler.isUserType(s)) {
                    	
                    	Validator.createErrorNode(base_type_element, "Unknown base type name: " + s + ".");
                    	
                    } else {
                    	
                        s = s.toUpperCase(Locale.ENGLISH);
                        
                        //We know that the following attribute exists (XML Schema)
                        String dataTypeName = ((Element)base_type_element.getParentNode().getParentNode()).getAttribute("name");
                        
                        dataTypeName = dataTypeName.trim().toUpperCase(Locale.ENGLISH);
                        
                        if (s.equals(dataTypeName)) {
                        	
                        	Validator.createErrorNode(base_type_element, "A user defined type cannot have the same name as its base type.");

                        }
                    }
                }    
                
            } else {
            	
            	Validator.createErrorNode(base_type_element, "Unsupported base type: " + s + ".");

            }
        }
        
        
        
    }
    
    /**
     * Validate initialValue child Element.
     * 
     * @param data_type_element
     */
	private void validate_initial_value(Element data_type_element) {
		
		Element initialValue = XMLUtilities.findChildElement(data_type_element, "initialValue");
    	
    	if (initialValue != null) {
    		
    		Element initialValue_child = XMLUtilities.getSingleChildElement(initialValue);
                            
    		if (!initialValue_child.getTagName().equals("simpleValue")) {
                            	
    			Validator.createErrorNode(initialValue_child,"Unsupported type of initial value.");

    		} else {
    			
    			 boolean validationResponse = validate_simple_value(initialValue_child, data_type_element);
                                
    			if (validationResponse) {
                                	
    				Validator.createInfoNode(initialValue_child, Validator.infoValue);
                                    
    			} else {
                                	
    				Validator.createErrorNode(initialValue_child, Validator.errorMessage);
    			}
    		}
    	}
		
	}
    
    /**
     * Simple initial value validation.
     * 
     * @param initialValue element
     */
    private boolean validate_simple_value(Element simple_value, Element data_type) {
        
        initial_value_parser.clear();
        
        String svalue = simple_value.getAttribute("value");
        
        boolean retval = initial_value_parser.parseExpr(svalue);
        
        if (!retval) {
        	
            Validator.createErrorNode(simple_value, initial_value_parser.getErrorMessage());
            
        } else {
            //The calling routine must have made sure that initial_value name attribute is either an IEC type
            //or an existing user type, not necessarily correctly defined.
            String baseTypeName = data_type.getAttribute("name").trim().toUpperCase(Locale.ENGLISH);
            
            if (data_type_handler.isUserType(baseTypeName)) {
            	
                baseTypeName = data_type_handler.getBaseType(baseTypeName);
                
            }
            
            if (!baseTypeName.equals("")) {

				if (initial_value_parser.hasExternalDependencies()) {
                    //This can happen when validating the initial value of a
                    //variable and not of a data type.
                    Validator.infoValue = "";
                    
                } else {
                	
                    String javaValue = initial_value_parser.makeJavaExp(baseTypeName);
                    
                    if (javaValue == null) {
                    	
                        Validator.errorMessage = "The initial value \"" + initial_value_parser.getValue() + "\" is not appropriate for data type " + baseTypeName;
                        retval = false;                    
                    } else {
                    	Validator.infoValue = javaValue;
                    }
                }
            } else {
                //If baseTypeName.equals("") then some anchestor of targetTypeName is not a well-defined data type. 
                //We can't set the infoValue because we can't check whether the provided value is appropriate for the
                //given data type. Initially we thougth of not reporting the error here, since it is reported in validateBaseType.
                //But if the user fixes the base type definition then the error will be eliminated there
                //and there will still be no initial value here. This may confuse the compilation process. Therefore,
                //we prefer to add an error here as well.
            	Validator.errorMessage = "Can't check suitability of the initial value. The base type is not well defined.";
                retval = false;
            }
        }
        return retval;
        
    }
	

}
