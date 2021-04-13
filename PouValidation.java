package validator;

import java.util.ArrayList;
import java.util.Locale;

import org.w3c.dom.Element;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

public class PouValidation {
	
	private Project project;
	private PouTypeHandler pou_type_handler;
	private DataTypeHandler data_type_handler;
	private VarListHandler var_list_handler;

	/**
     * Initialize the variable list handler class with the given pou interface element.
     * 
     * @param pou_interface
     */
    private void set_var_list_handler(Element pou_interface) {
    	
    	this.var_list_handler = new VarListHandler(pou_interface);

	}

    /**
     * Constructor
     * 
     * @param project
     */
	public PouValidation(Project project) {
		
		this.project = project;
		
		this.pou_type_handler = project.getPouTypeHandler();
		
		this.data_type_handler = project.getDataTypeHandler();
		
	}
    
	/**
	 * Validate the give project.
	 * 
	 */
	public void validate() {
        
        for (int i = 0; i < pou_type_handler.getNumberOfPouTypes() ; i++) {
        	
            Element pou = pou_type_handler.getPouTypeElement(i);
            
            validate_pou_name_attribute(pou);
            
            Element pouInterface = XMLUtilities.findChildElement(pou, "interface");
            
            // if interface element was found inside the pou element
            if (pouInterface != null) {
                
                validate_child_return_type(pou, pouInterface);
                
                validate_child_list_types(pou, pouInterface);
                
            }
        }
		
	}
	
	 /**
     * Validate pou element name attribute.
     * 
     * @param pou_element
     */
    private void validate_pou_name_attribute(Element pou_element) {
        
        String name_attribute = pou_element.getAttribute("name").trim();
        
        if (name_attribute.equals("")) {
        	
            Validator.createErrorNode(pou_element, "The pou name attribute cannot be empty.");
            
        } else if ( ! IECUtilities.isValidIdentifier(name_attribute)) {
        	
        	Validator.createErrorNode(pou_element, "The pou name attribute must be a valid identifier.");
         
        } else if (CommonLexer.isKeyword(name_attribute)) {
        	
        	Validator.createErrorNode(pou_element, "The pou name attribute cannot be a reserved word.");
           
        } else if (IECUtilities.isStandardPouName(name_attribute)){
        	
        	Validator.createErrorNode(pou_element, "A pou name attribute cannot have the same name as a standard POU.");
            
        } else if (project.getPouTypeHandler().isUserPou(name_attribute)) {
        	
        	Validator.createErrorNode(pou_element, "A pou name attribute cannot have the same name as a user dedined POU.");

        } else if (! project.getPouTypeHandler().isUniquePouTypeName(name_attribute)) {
        	
        	Validator.createErrorNode(pou_element, "Pou name attribute must be uniqune amongst all the pou elements.");
        	
        }
        
    }
    
    /**
     * Validate interface child element returnType.
     * 
     * @param pou
     * @param pou_interface
     */
    private void validate_child_return_type(Element pou, Element pou_interface) {
    	
    	Element returnType = XMLUtilities.findChildElement(pou_interface, "returnType");
        
        // if returnType element was found inside the parent interface element.
        if (returnType != null) {
        	
        	// The pouType of the interface element must only be of function type.
            if (! pou.getAttribute("pouType").equals("function")) {
            	
            	Validator.createErrorNode(pou, "Only pou elements with pouType function can have a return type.");
                
            } else {
            	
                // Get the child element of the returnType element ( must only be one )
                Element returnType_child = XMLUtilities.getSingleChildElement(returnType);
                
                validate_return_type_child_element(returnType_child);
            }
        }
    }
    
    /**
     * Validate the return type element.
     * 
     * @param return_type_child
     */
    private void validate_return_type_child_element(Element return_type_child) {
        
        String element_tag_name = return_type_child.getTagName();
        
        if (! IECUtilities.isAnyElementary(element_tag_name)) {
        	
        	if (element_tag_name.equals("derived")) {
        		
        		String attribute_name_value = return_type_child.getAttribute("name").trim();
        		
        		if (! data_type_handler.isUserType(attribute_name_value)) {
        			
        			Validator.createErrorNode(return_type_child, "Unknown data type name: " + attribute_name_value + ".");
                    
                }
        		
        	} else {
        		
        		Validator.createErrorNode(return_type_child, "Unknown return type child element:" + element_tag_name + ".");
        		
        	}
        }

    }
   
    /**
     * Validate interface child list types.
     * 
     * @param pou_type
     * @param pouInterface
     */
    private void validate_child_list_types(Element pou, Element pou_interface) {
    	
    	set_var_list_handler(pou_interface);
    	
    	validate_unsupported_list_types(pou_interface);
    	
        validate_list_type_attributes(pou_interface);
        
        for (int j = 0; j < var_list_handler.getNumberOfVariables(); j++) {
        	
        	// Interface Child element
            Element variable_element = var_list_handler.getVariableElement(j);
            
            validate_variable_name_attribute(variable_element);
        	
        	validate_variable_en(variable_element);
        	
        	validate_variable_eno(variable_element);
        	
        	validate_variable_address_attributes(variable_element, pou);
        	
        	validate_variable_child_type(variable_element, pou);
        	
        	validate_variable_child_initialValue(variable_element, pou);
             
        }
    }

	/**
     * Validate unssoported list_type elements.
     * 
     * @param interface_element
     */
    private void validate_unsupported_list_types(Element interface_element) {
    	
    	 ArrayList<Element> interface_element_childs = XMLUtilities.getChildrenElements(interface_element);
    	 
    	 for (int i = 0; i < interface_element_childs.size(); i++ ) {
    		 
    		 Element active = interface_element_childs.get(i);
    		 
    		 String active_element_tag_name = active.getTagName().trim();
    		 
    		 if (active_element_tag_name.matches("accessVars|globalVars")) {
    			 
    			 Element active_element_child = XMLUtilities.getSingleChildElement(active);
    			 
    			 if (active_element_child != null) {
    				 
    				 Validator.createErrorNode(active, "List types AccessVars or GlobalVars inside an interface element are only supported without child elements.");
    				 
    			 }
    			 
    		 }
    		 
    	 }
    }
    
    /**
     * Validate list element constant attribute.
     * 
     * @param interface_element
     */
    private void validate_list_type_attributes(Element interface_element) {
    	
    	ArrayList<Element> interface_element_childs = XMLUtilities.getChildrenElements(interface_element);
    	
    	for (int i = 0; i < interface_element_childs.size(); i++ ) {
   		 
    		Element active = interface_element_childs.get(i);
   		 		
   		 	String active_element_constant_attribute = active.getAttribute("constant").trim();
   		 		
   		 	if (active_element_constant_attribute != null) {
   		    	
   		 		if (active_element_constant_attribute.equals("true")) {
   		 			
   		 			String active_element_tag_name = active.getTagName().trim();
   	    			
   		 			if (! active_element_tag_name.matches("localVars|externalVars")) {
   	        	     	
   		 				Validator.createErrorNode(active, "Constant attribute value can only be false or null");
   		 			}
   	    			   			
   		 		}
   		 	}
    	}

    }

    /**
     * Validate the variable name attribute.
     * 
     * @param variable_element
     */
    private void validate_variable_name_attribute(Element variable_element) {
    	
    	String variable_element_name_attribute = variable_element.getAttribute("name").trim();
        
        if (variable_element_name_attribute.equals("")) {
        	
            Validator.createErrorNode(variable_element,"The variable name attribute cannot be empty.");
            
        } else if (!IECUtilities.isValidIdentifier(variable_element_name_attribute)) {
        	
        	Validator.createErrorNode(variable_element,"The variable name attribute must be a valid identifier.");
         
        } else if (CommonLexer.isKeyword(variable_element_name_attribute)) {
        	
        	Validator.createErrorNode(variable_element,"The variable name attribute cannot be a reserved word.");
           
        } else if (IECUtilities.isStandardPouName(variable_element_name_attribute)){
        	
        	Validator.createErrorNode(variable_element,"A variable name attribute cannot have the same name as a standard POU.");
            
        } else if (project.getPouTypeHandler().isUserPou(variable_element_name_attribute)) {
        	
        	Validator.createErrorNode(variable_element,"A variable name attribute cannot have the same name as a user dedined POU.");

        } else if (! var_list_handler.isUniqueVariableName(variable_element_name_attribute)) {
        	
        	Validator.createErrorNode(variable_element,"The variable name attribute is not unique.");
           
        }

    }
    
    /**
     * Validate EN variables.
     * 
     * @param variable_element
     */
    private void validate_variable_en(Element variable_element) {
    	
    	if (variable_element.getAttribute("name").trim().equals("EN")) {
    		
        	String variable_type = VarListHandler.getVariableDataType(variable_element);
        	
        	Element parent_node = (Element) variable_element.getParentNode();
        	
        	if (! parent_node.getTagName().equals("inputVars")) {
        		
        		Validator.createErrorNode(variable_element, "The variable parent node can only be an inputVar element.");
        		
        	} else {
        		
        		if (! variable_type.equals("boolean")) {
        			
            		Validator.createErrorNode(variable_element, "The variable must be of type boolean.");
            		
            	}
        		
        	}
        	
        }
    }
    
    /**
     * Validate ENO variables.
     * 
     * @param variable_element
     */
    private void validate_variable_eno(Element variable_element) {
    	
    	if (variable_element.getAttribute("name").trim().equals("ENO")) {
    		
        	String variable_type = VarListHandler.getVariableDataType(variable_element);
        	
        	Element parent_node = (Element) variable_element.getParentNode();
        	
        	if (! parent_node.getTagName().equals("outputVars")) {
        		
        		Validator.createErrorNode(variable_element, "The variable parent node can only be an outputVars element.");
        		
        	} else {
        		
        		if (! variable_type.equals("boolean")) {
        			
            		Validator.createErrorNode(variable_element, "The variable must be of type boolean.");
            		
            	}
        		
        	}
        	
        }
    }
    
    /**
     * Validate the variables attributes.
     * 
     * @param variable_element
     * @param pou
     */
    private void validate_variable_address_attributes(Element variable_element, Element pou) {
    	
    	String address_attribute = variable_element.getAttribute("address").trim();

    	String variable_type = VarListHandler.getVariableDataType(variable_element);
    	
    	Element variable_parent = (Element) variable_element.getParentNode();
    	
    	String pou_type = pou.getAttribute("pouType").trim();

    	if (address_attribute != null && variable_parent.getTagName().trim().equals("localVars") 
    								  && pou_type.matches("program|functionBlock")) {

    		if (! address_attribute.equals("")) {
    			
    			if (IECUtilities.isStandardFunctionBlockName(variable_type)) {
            		
            		Validator.createErrorNode(variable_element, "The variable type can't be of function block type.");
            		
            	} else if (pou_type.matches("program|functionBlock") && AddressUtilities.is_partial_address(address_attribute)) {

            		AddressUtilities.validate_partial_address(variable_element);
                	
            	} else if (pou_type.equals("program") && AddressUtilities.is_full_address(address_attribute)) {
            		
            		AddressUtilities.validate_full_address(variable_element, address_attribute);
        			
        		}
    		}

    	}

    }
    
    /**
     * Validate the variable child types.
     * 
     * @param variable_element
     * @param pou witch is the parent pou element of the node.
     */
    private void validate_variable_child_type(Element variable_element, Element pou) {
    	
    	Element type_child_element = XMLUtilities.findChildElement(variable_element, "type");
    	
    	// Can only containt one element
    	type_child_element = XMLUtilities.getSingleChildElement(type_child_element);
    	
    	String child_element_tag_name = type_child_element.getTagName();
    	
    	if ( ! IECUtilities.isAnyElementary(child_element_tag_name) ) {
        	
    		// If derived name attribute must be a user defined type.
            if (child_element_tag_name.equals("derived")) {
            	
            	child_element_tag_name = type_child_element.getAttribute("name").trim();
                
                if (child_element_tag_name.equals("")) {
                	
                	Validator.createErrorNode(type_child_element, "Variable child type name of user defined type cannot be empty.");
                	
                } else {
                    
                    if (! data_type_handler.isUserType(child_element_tag_name) && ! IECUtilities.isStandardFunctionBlockName("child_element_tag_name")) {
                    	
                    	Validator.createErrorNode(type_child_element, "Unknown variable type name: " + child_element_tag_name + ".");
                    	
                    } else {
                    	
                    	child_element_tag_name = child_element_tag_name.toUpperCase(Locale.ENGLISH);
                        
                        String pou_name_attribute = pou.getAttribute("name").trim().toUpperCase(Locale.ENGLISH);
                        
                        if (child_element_tag_name.equals(pou_name_attribute)) {
                        	
                        	Validator.createErrorNode(type_child_element, "A derived type child cannot have the same name as it pou parent element.");

                        }
                    }
                }    
                
            } else {
            	
            	Validator.createErrorNode(type_child_element, "Unsupported variable child type: " + child_element_tag_name + ".");
            	
            }
        }
    	
    }
    
    /**
     * Validate child initialValue element.
     * 
     * @param variable_element
     * @param pou witch is the parent pou element of the node.
     */
    private void validate_variable_child_initialValue(Element variable_element, Element pou) {
    	
    	Element initialValue = XMLUtilities.findChildElement(variable_element, "initialValue");
    	
    	// Max one element.
    	if (initialValue != null) {
    		
    		Element variable_parent = (Element) variable_element.getParentNode();
    		
    		if (variable_parent.getTagName().matches("inOutVars|externalVars")) {
    			
    			Validator.createErrorNode(variable_element, "Variable with a initialValue child element can't have inOutVars or externalVars parent elements.");
    			
    		} else {
    		
    			String variable_type = VarListHandler.getVariableDataType(variable_element);
    			
    			Element initialValue_child = XMLUtilities.getSingleChildElement(initialValue);
    		
    			if (IECUtilities.isAnyElementary(variable_type)) {

    				if (initialValue_child.getTagName().equals("simpleValue")) {
    					
    					boolean validationResponse = validate_simple_value(initialValue);
        				
    					if (validationResponse) {
                    	
	    					Validator.createInfoNode(initialValue, Validator.infoValue);
	                                    
	    				} else {
	                                	
	    					Validator.createErrorNode(initialValue, Validator.errorMessage);
	    				}
    					
    				} else {
    				
    					Validator.createErrorNode(initialValue, "Variable of any type can only have a simpleValue as intialValues child element.");
    				
    				}
    			
    			} else if (IECUtilities.isStandardFunctionBlockName(variable_type)) {
    			
    				if (initialValue_child.getTagName().equals("structValue")) {
    					
    					validate_struct_value(initialValue_child, variable_element);
 
    				} else {
    				
    					Validator.createErrorNode(initialValue, "Variable of function block type can only have structValue as intialValues child element.");
    				
    				}
    			
    			}
    		}
    	}

    }
    
    /**
     * Simple initial value validation.
     * 
     * @param initial_value
     * @return
     */
    private boolean validate_simple_value(Element initial_value) {
        
        InitialValueParser initialValueParser = project.getInitialValueParser();
        
        initialValueParser.clear();
        
        String svalue = initial_value.getAttribute("value");
        
        boolean retval = initialValueParser.parseExpr(svalue);
        
        if (!retval) {
        	
            Validator.createErrorNode(initial_value, initialValueParser.getErrorMessage());
            
        } else {

            String baseTypeName = initial_value.getAttribute("name").trim().toUpperCase(Locale.ENGLISH);
            
            if (data_type_handler.isUserType(baseTypeName)) {
            	
                baseTypeName = data_type_handler.getBaseType(baseTypeName);
                
            }
            
            if (!baseTypeName.equals("")) {

				if (initialValueParser.hasExternalDependencies()) {

                    Validator.infoValue = "";
                    
                } else {
                	
                    String javaValue = initialValueParser.makeJavaExp(baseTypeName);
                    
                    if (javaValue == null) {
                    	
                        Validator.errorMessage = "The initial value \"" + initialValueParser.getValue() + "\" is not appropriate for data type " + baseTypeName;
                        retval = false;                    
                    } else {
                    	Validator.infoValue = javaValue;
                    }
                }
            } else {
                Validator.errorMessage = "Can't check suitability of the initial value. The base type is not well defined.";
                
                retval = false;
            }
        }
        return retval;
        
    }

    /**
     * Validate the strucValue element.
     * 
     * @param initialValue_child
     * @param variable_element
     */
    private void validate_struct_value(Element initialValue_child, Element variable_element) {
    	
    	ArrayList<Element> children_elements = XMLUtilities.getChildrenElements(initialValue_child);
    	
    	if (children_elements.size() > 0) {
    		
    		for (int i = 0; i < children_elements.size(); i++) {
        		
    			Element active = children_elements.get(i);
    			
    			if (active.getTagName().equals("value")) {
    				
    				String member_attribute = active.getAttribute("member").trim();
    				
    				if (member_attribute != null) {
    					
    					Element variable_parent = (Element) variable_element.getParentNode();
    					
    					String variable_parent_tag = variable_parent.getTagName().trim();
    					
    					Element value_child_element = XMLUtilities.getSingleChildElement(active);
    					
    					if (variable_parent_tag.matches("inOutVars|externalVars")) {
    						
    						Validator.createErrorNode(initialValue_child, "Member attribute can't be inOutVars or externalVars");
    						
    					}
    					
    					if (! IECUtilities.isStandardFunctionBlockName(member_attribute)) {
    						
    						if (value_child_element.getTagName().equals("simpleValue")) {
        	    				
        	    				boolean validationResponse = validate_simple_value(value_child_element);
        	    				
        	    				if (validationResponse) {
        	                    	
        		    				Validator.createInfoNode(value_child_element, Validator.infoValue);
        		                                    
        		    			} else {
        		                                	
        		    				Validator.createErrorNode(value_child_element, Validator.errorMessage);
        		    			}
        	    				
        	    			} else {
        	    				
        	    				Validator.createErrorNode(value_child_element, "Value element must contain a simpleValue element.");
        	    				
        	    			}
    						
    					} else {
    						
    						if (! value_child_element.getTagName().equals("structValue")) {
        	    				
        	    				Validator.createErrorNode(value_child_element, "Value element must contain a structValue element.");
            	    		}
    						
    					}
    						
    				} else {
    					Validator.createErrorNode(active, "The member attribute is required.");	
    				}
    			} else {
    				Validator.createErrorNode(active, "Valid child are only value elements.");	
    			}	
        	}	
    	}
    }
    
}
