/**
 * 
 */
package com.actualize.mortgage.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.actualize.mortgage.service.impl.UCDXMLServiceImpl;
/**
 * @author sboragala
 *
 */
@RestController
@RequestMapping("actualize/transformx/services/ucd/")
public class UCDTrimAPIImpl {
	
	@RequestMapping(value = "{version}/trim", method = { RequestMethod.POST }, produces = "application/xml")
    public String generateUCDXml(@RequestParam String version, @RequestBody String xmldoc) throws Exception {
		UCDXMLServiceImpl ucdxmlServiceImpl = new UCDXMLServiceImpl();
		InputStream inputXmlStream = new ByteArrayInputStream(xmldoc.getBytes(StandardCharsets.UTF_8));
		return ucdxmlServiceImpl.createClosingDisclosureUCDXML(inputXmlStream);
    }

	@RequestMapping(value = "{version}/ping", method = { RequestMethod.GET })
    public String status(@RequestParam String version) throws Exception {
		return "The Service for generating UCD XML is running and ready to accept the requests";
    }
}
