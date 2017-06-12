package com.actualize.mortgage.transformx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class UCDXMLTransformer {
    private static final Logger LOGGER = Logger.getLogger(UCDXMLTransformer.class.getName());

    final static String LINE_NUMBER_KEY_NAME = "lineNumber";
    private static Validator validator = null;
    private static final String ucdXsd = "UCD_Spec_1.3.xsd";
    private static Integer MAX_RECURSIVE_LOAD=30;
    private Integer LOOP=0;
    /*public static void main(String[] args) throws Exception {
        
        LOGGER.log(Level.INFO, "Start time: " + new Date().toString());
        // Parse command line arguments
        if (args.length != 1)
            LOGGER.info("Usage: UCDXMLTransformer <SuperXML with path>");

        File superXmlFile = new File(args[0]);
        // Read superXmlFile file
        LOGGER.log(Level.INFO, "Reading file '" + superXmlFile + "'...");
        File ucdFileName = new File(superXmlFile.getPath().replaceAll(".xml", "_UCD.xml"));

        UCDXMLTransformer transformer = new UCDXMLTransformer();
        EvaluateXmlNodes evaluateXmlNodes = new EvaluateXmlNodes();
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(superXmlFile);
        doc.getDocumentElement().removeAttribute("xsi:schemaLocation");
        doc.getDocumentElement().setAttribute("xmlns:gse", "http://www.datamodelextension.org");
        doc= transformer.getUCDXmlDocument(doc, ucdFileName);

        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ucdFileName);
        doc = evaluateXmlNodes.removeFeePaidToType(doc);
        removeEmptyNodes(doc);
        ucdFileName = transformer.createUCDXMLFile(doc, ucdFileName);
    }

*/    public Document getUCDXmlDocument(final Document doc, final File ucdFileName) throws TransformerException, IOException {
		InputStream inputStream = null;
	try {
            Document outputDoc = doc;
            inputStream = convertToInputStream(outputDoc);
            List<UCDError> exceptions = findMissingElements(outputDoc);
            Map<Integer,UCDError> errors = new HashMap<>();
            for (UCDError ucdError : exceptions) {
                if(!errors.containsKey(ucdError.getLineNumber()))
                    errors.put(ucdError.getLineNumber(), ucdError);
            }
            if(!exceptions.isEmpty()){
                outputDoc = readXML(inputStream);
                if (outputDoc.hasChildNodes()) {
                    
                    removeInvalidNodes(outputDoc.getChildNodes(), errors);
                    File ucdXmlFile = createUCDXMLFile(outputDoc, ucdFileName);
                    if(MAX_RECURSIVE_LOAD>LOOP++)
                        getUCDXmlDocument(outputDoc, ucdXmlFile);
                }
            }
            return outputDoc;
        } catch (IOException | SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        finally
        {
        	if(null != inputStream)
        		inputStream.reset();
        }
        return null;
    }

    public File createUCDXMLFile(final Document document, final File file) throws TransformerException {
        // Make a transformer factory to create the Transformer
        TransformerFactory tFactory = TransformerFactory.newInstance();
       // tFactory.setAttribute("indent-number", 2);

        // Make the Transformer
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // Mark the document as a DOM (XML) source
        DOMSource source = new DOMSource(document);

        // Say where we want the XML to go
        StreamResult result = new StreamResult(file);

        // Write the XML to file
        transformer.transform(source, result);
        return file;
    }

    private List<UCDError> findMissingElements(final Document doc) {
        final List<UCDError> exceptions = new ArrayList<>();
        try {
            InputStream inputStream = convertToInputStream(doc);
            if (validator == null) {
                //For Command path input file path
                //String filepathTxt = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toString()).getParent()+"\\"+ucdXsd;
            	
            	Resource resource = new ClassPathResource("/lib/"+ucdXsd);
            	File xsdFile = resource.getFile();
            	
                //For Eclipse input file path
                String filepathTxt = "/lib/"+ucdXsd;
                filepathTxt = filepathTxt.replaceAll("file:\\\\", "");
                validator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdFile).newValidator();
            }
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    exceptions.add(convertError(exception));
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    exceptions.add(convertError(exception));
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    exceptions.add(convertError(exception));
                }
            });
            validator.validate(new StreamSource(inputStream));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return exceptions;
    }

    private UCDError convertError(final SAXParseException e) {
        UCDError error = new UCDError();
        error.setLineNumber(e.getLineNumber());
        error.setError(e.getMessage());
        if (e.getMessage().contains("attribute") || e.getMessage().contains("Attribute")) {
            error.setAttribute(true);
        } else {
            error.setElement(true);
        }
        return error;
    }

    private void removeInvalidNodes(final NodeList nodeList, Map<Integer,UCDError> errors) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                // get node name and value
                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes
                    removeInvalidNodes(tempNode.getChildNodes(), errors);
                }
                Integer lineNumber = (Integer) tempNode.getUserData(LINE_NUMBER_KEY_NAME);
                if (errors.containsKey(lineNumber)) {

                    UCDError error = errors.get(lineNumber);
                    LOGGER.info("lineNumber :" + lineNumber);
                    LOGGER.info("Error message :" + error.getError());

                    if (error.isElement()) {
                        LOGGER.info("Removed node name:" + tempNode.getNodeName());
                        if ("OTHER".equalsIgnoreCase(tempNode.getParentNode().getNodeName()) && tempNode.getParentNode().getChildNodes().getLength() == 1) {
                            Node otherNode = tempNode.getParentNode().getParentNode();
                            otherNode.getParentNode().removeChild(otherNode);
                        } else {
                            tempNode.getParentNode().removeChild(tempNode);
                        }
                    } else {
                        String attribute = "";
                        String errorMsg = error.getError();
                        String[] errorMsgArray = errorMsg.split("Attribute '");
                        if(errorMsgArray.length>1) {
                            String[] attributeArrayFromMsg = errorMsgArray[1].split("'(.*)");
                            attribute = attributeArrayFromMsg[0];
                            if(!attribute.isEmpty()) {
                                NamedNodeMap attributes = tempNode.getAttributes();
                                attributes.removeNamedItem(attribute);
                            }
                        }
                        LOGGER.info("Removed attribute name:" + error.getError());

                    }
                }
            }
        }
    }

    private Document readXML(final InputStream is) throws IOException, SAXException {
        final Document doc;
        SAXParser parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
        }

        final Stack<Element> elementStack = new Stack<Element>();
        final StringBuilder textBuffer = new StringBuilder();
        final DefaultHandler handler = new DefaultHandler() {
            private Locator locator;

            @Override
            public void setDocumentLocator(final Locator locator) {
                // Save the locator, so that it can be used later for line
                // tracking when traversing nodes.
                this.locator = locator;
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                addTextIfNeeded();
                final Element el = doc.createElement(qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    el.setAttribute(attributes.getQName(i), attributes.getValue(i));
                }
                el.setUserData(LINE_NUMBER_KEY_NAME, Integer.valueOf(this.locator.getLineNumber()), null);
                elementStack.push(el);
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) {
                addTextIfNeeded();
                final Element closedEl = elementStack.pop();
                if (elementStack.isEmpty()) { // Is this the root element?
                    doc.appendChild(closedEl);
                } else {
                    final Element parentEl = elementStack.peek();
                    parentEl.appendChild(closedEl);
                }
            }

            @Override
            public void characters(final char ch[], final int start, final int length) throws SAXException {
                textBuffer.append(ch, start, length);
            }

            // Outputs text accumulated under the current node
            private void addTextIfNeeded() {
                if (textBuffer.length() > 0) {
                    final Element el = elementStack.peek();
                    final Node textNode = doc.createTextNode(textBuffer.toString());
                    el.appendChild(textNode);
                    textBuffer.delete(0, textBuffer.length());
                }
            }
        };
        parser.parse(is, handler);
        return doc;
    }

    private InputStream convertToInputStream(final Document doc) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(doc);
        Result outputTarget = new StreamResult(outputStream);
        try {
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
    
    
    public static void removeEmptyNodes(Node node) {

        // Grab all children of node
        NodeList childnodes = node.getChildNodes();

        // Save all children nodes into list that doesn't change
        List<Node> immutable = new LinkedList<Node>();
        for (int i = 0; i < childnodes.getLength(); i++)
            immutable.add(childnodes.item(i));

        // Recursive through list that doesn't change
        for (Node child : immutable)
            removeEmptyNodes(child);

        boolean emptyElementNode = node.getNodeType() == Node.ELEMENT_NODE && node.getChildNodes().getLength() == 0
                && node.getAttributes().getLength() == 0;
        boolean emptyTextNode = node.getNodeType() == Node.TEXT_NODE && node.getNodeValue().trim().isEmpty();

        // Remove node if empty
        if (emptyElementNode || emptyTextNode)
            node.getParentNode().removeChild(node);
    }
}
