package validator;

import org.w3c.dom.Element;

public class AddressUtilities {
	
	private static String full_address_with_bit_syntax = "[%][IQM][XBWDL]?([1-9][0-9]+|[1-9])[.0-7]?";
	private static String full_address = "[%][IQM][XBWD]([1-9][0-9]+|[1-9])";
	private static String partial_address = "[%][IQM][*]";
	
	/**
     * Check if the give address is of full type.
     * 
     * @param address_attribute
     * @return
     */
    public static boolean is_full_address(String address_attribute) {
    	
		boolean response = false;
		
		if (address_attribute.matches(full_address_with_bit_syntax)) {
			
			response = true;
			
		}
		
		return response;
		
	}

	
    /**
	 * Check if the given address is of partial type.
     * 
     * @param address_attribute
     * @return
     */
    public static boolean is_partial_address(String address_attribute) {
		
    	boolean response = false;
		
		if (address_attribute.matches(partial_address)) {
			
			response = true;
			
		}
		
		return response;
	}
    
	/**
     * Validate partial address attribute value.
     * 
     * @param variable_element
     */
    public static void validate_partial_address(Element variable_element) {
    	
    	Element parent_list = (Element) variable_element.getParentNode();

    	String constant_attribute = parent_list.getAttribute("constant").trim();

		if (constant_attribute != "false") {
			
			Validator.createErrorNode(variable_element, "A variable with a partial address can't have the attribute constant.");
			
		} else {
			
			Element initial_value_child = XMLUtilities.findChildElement(variable_element, "initialValue");
			
			if ( initial_value_child != null ) {
				
				Validator.createErrorNode(variable_element, "The variable with a partial address can't have initialValue child elements.");
				
			}
		}
		
    }
    
    /**
     * Validate full address attribute value.
     * 
     * @param variable_element
     */
    public static void validate_full_address(Element variable_element, String address_attribute) {
    	
    	String variable_type = VarListHandler.getVariableDataType(variable_element);
    	
    	if (variable_type.equals("bool")) {
    		
    		if (! address_attribute.matches(full_address_with_bit_syntax)) {
    			
    			Validator.createErrorNode(variable_element, "The address attribute can only be of bit syntax.");
    			
    		}
    		
    	} else {
    		
    		if (! address_attribute.matches(full_address)) {
    			
    			Validator.createErrorNode(variable_element, "The address attribute syntax is invalid.");
    			
    		}
    		
    	}
    }

}
