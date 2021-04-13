package validator;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MainEntry {

	public static void main(String[] args) {
		
		Project project = null;
		
		File xml_file = new File("src/assets/test.xml");
		
		File xsd_file = new File("src/assets/tc6_xml_v201.xsd");
		
		 try {            
	            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	            
	            dbFactory.setValidating(true);
	            
	            dbFactory.setNamespaceAware(true);
	            
	            dbFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
	                     "http://www.w3.org/2001/XMLSchema");
	     
	            dbFactory.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource", xsd_file);
	            
	            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	            
	            XMLErrorLogger eh = new XMLErrorLogger();
	            
	            dBuilder.setErrorHandler(eh);
	            
	            Document doc = dBuilder.parse(xml_file);
	            
	            if (eh.everythingOk()) {
	            	
	               doc.getDocumentElement().normalize();
	               
	               project = new Project();
	               
	               project.setDom(doc);
	               
	               project.init();
	               
	               project.setFile(xml_file);
	               
	               Validator.validateProject(project);
	               
	            } else {
	               
	            	UIelements.println("Could not open XML file. Make sure that it conforms to the PLCOpen TC6 Schema definition");
	               
	            	UIelements.println("and that the root element contains the namespace declaration xmlns=\"http://www.plcopen.org/xml/tc6_0201\".");
	            	
		 		}
	        }
		 
		 catch (IOException | ParserConfigurationException | DOMException | SAXException e){
	        	
	            UIelements.reportException(e);
	            
	        }

	}

}
