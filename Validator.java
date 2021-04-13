package validator;

import org.w3c.dom.*;

public class Validator {
    
    public static String errorMessage;
    public static String infoValue;
    
    public static String getErrorMessage() {
        return errorMessage;
    }
    
    public static String getInfoValue() {
        return infoValue;
    }
    
    /**
     * Create an error node on the given element with the given message value.
     * 
     * @param elem
     * @param msg
     */
    public static void createErrorNode(Element element, String message) {
        
        Document dom = element.getOwnerDocument();
        
        Element err = dom.createElement("SidValidationError");
        
        Attr attr = dom.createAttribute("message");
        
        System.out.println(message);
        
        attr.setValue(message);
        
        err.setAttributeNode(attr);
        
        element.appendChild(err);
            
    }
    
    /**
     * Create an info node on the given element with the given value.
     * 
     * @param element
     * @param value.
     */
    public static void createInfoNode(Element element, String value) {
        
        Document dom = element.getOwnerDocument();
        
        Element info = dom.createElement("SidValidationError");
        
        Attr attr = dom.createAttribute("value");
        
        attr.setValue(value);
        
        info.setAttributeNode(attr);
        
        element.appendChild(info);
            
    }
    
    /**
     * Validate the given project
     * 
     * @param project
     */
    public static void validateProject(Project project) {
        
    	// Get root document element
        Element root = project.getDom().getDocumentElement();
        
        validate_content_header((Element)root.getElementsByTagName("contentHeader").item(0));
        
        new DataTypeValidation(project).validate();
        
        new PouValidation(project).validate();
        
        new ConfigurationValidation(project).validate();

    }
    
    /**
     * Validate the content header name attribute.
     * 
     * @param contentHeader
     * @return
     */
    private static void validate_content_header(Element contentHeader) {
        
        String content_header = contentHeader.getAttribute("name").trim();
        
        // if the name is empty create the appropriate error node attribute.
        if (content_header.equals("")) {
        	
            createErrorNode(contentHeader,"Attribute \"name\" of element \"project\\contentHeader\" cannot be empty.");
            
        }
        
    }
    
}
