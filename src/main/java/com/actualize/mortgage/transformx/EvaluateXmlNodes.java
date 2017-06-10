package com.actualize.mortgage.transformx;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EvaluateXmlNodes {

    private static final Logger LOGGER = Logger.getLogger(EvaluateXmlNodes.class.getName());
    
    private XPathFactory xpf = null;
    private XPath xpath = null;
    private List<String> feeTypes = Arrays.asList("RecordingFeeForMortgage","RecordingFeeForDeed","RecordingFeeTotal");
    private final static String OTHERCOSTS ="OtherCosts";  
    public EvaluateXmlNodes() {
        xpf = XPathFactory.newInstance();
        xpath = xpf.newXPath();
    }
    public Document removeFeePaidToType(Document document){
        XPathExpression expr;
        try {
            expr = xpath.compile("//MESSAGE/DOCUMENT_SETS/DOCUMENT_SET/DOCUMENTS/DOCUMENT/DEAL_SETS/DEAL_SET/DEALS/DEAL/LOANS/LOAN/FEE_INFORMATION/FEES/FEE/FEE_DETAIL");
            Object  result = expr.evaluate(document, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            for (int i = 0; i < nodes.getLength(); i++) {
                NodeList childNodes = nodes.item(i).getChildNodes();
                Node feePaidToType = null;
                Node integratedDisclosureSectionType = null;
               for (int j = 0; j < childNodes.getLength(); j++) {
                   Node node = childNodes.item(j);
                   //[USB UCD-7] REMOVE FEEPAIDTOTYPE FROM SECTION E
                   if("mismo:FeePaidToType".equalsIgnoreCase(node.getNodeName())){
                       feePaidToType = node;
                   }
                   if("mismo:FeeType".equalsIgnoreCase(node.getNodeName())){
                       if(feeTypes.contains(node.getTextContent()) && feePaidToType!=null){
                           feePaidToType.getParentNode().removeChild(feePaidToType);
                       }
                   }
                   //[USB UCD-8] REMOVE OPTIONALCOSTINDICATOR
                   if("mismo:IntegratedDisclosureSectionType".equalsIgnoreCase(node.getNodeName())){
                	   integratedDisclosureSectionType = node;
                   }
                   if(null != integratedDisclosureSectionType && "mismo:OptionalCostIndicator".equalsIgnoreCase(node.getNodeName())){
                	   if(!OTHERCOSTS.equalsIgnoreCase(integratedDisclosureSectionType.getTextContent())){
                		   node.getParentNode().removeChild(node);
                	   }
                   }
                }
            }
            return document;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        return document;
    }
}
