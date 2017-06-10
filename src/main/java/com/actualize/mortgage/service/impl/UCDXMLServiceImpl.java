/**
 * 
 */
package com.actualize.mortgage.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import com.actualize.mortgage.transformx.EvaluateXmlNodes;
import com.actualize.mortgage.transformx.UCDXMLTransformer;


/**
 * @author sboragala
 *
 */
public class UCDXMLServiceImpl {

	public String createClosingDisclosureUCDXML(InputStream inputXmlStream) throws Exception {
		
		 Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputXmlStream);
		 UCDXMLTransformer transformer = new UCDXMLTransformer();
	     EvaluateXmlNodes evaluateXmlNodes = new EvaluateXmlNodes();
	     OutputStream outputStream = null;
 
	        doc.getDocumentElement().removeAttribute("xsi:schemaLocation");
	        doc.getDocumentElement().setAttribute("xmlns:gse", "http://www.datamodelextension.org");
	        
	        try{

			// write the inputStream to a FileOutputStream
			outputStream =  new FileOutputStream(new File(getClass().getClassLoader().getResource("targetFile.xml").getFile()));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputXmlStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
	        }
			finally {
				if (outputStream != null) {
					try {
						// outputStream.flush();
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
	        
	        File ucdFileName = new File(getClass().getClassLoader().getResource("targetFile.xml").getFile());
	        doc= transformer.getUCDXmlDocument(doc, ucdFileName);

	        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ucdFileName);
	        doc = evaluateXmlNodes.removeFeePaidToType(doc);
	        transformer.removeEmptyNodes(doc);
	        ucdFileName = transformer.createUCDXMLFile(doc, ucdFileName);
	        
		return FileUtils.readFileToString(ucdFileName, "UTF-8");
	}

}
