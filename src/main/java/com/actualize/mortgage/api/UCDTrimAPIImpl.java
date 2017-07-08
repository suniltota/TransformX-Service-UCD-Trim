/**
 * 
 */
package com.actualize.mortgage.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.actualize.mortgage.service.impl.UCDXMLServiceImpl;
/**
 * @author sboragala
 *
 */
@RestController
@RequestMapping("actualize/transformx/services/ucd/")
public class UCDTrimAPIImpl {
	
	private static final Logger LOG = LogManager.getLogger(UCDTrimAPIImpl.class);
	
	@RequestMapping(value = "{version}/trim", method = { RequestMethod.POST }, produces = "application/xml")
    public String generateUCDXml(@PathVariable String version, @RequestBody String xmldoc) throws Exception {
		LOG.info("user "+SecurityContextHolder.getContext().getAuthentication().getName()+" used Service: MISMO XML to UCD XML");
		UCDXMLServiceImpl ucdxmlServiceImpl = new UCDXMLServiceImpl();
		InputStream inputXmlStream = new ByteArrayInputStream(xmldoc.getBytes(StandardCharsets.UTF_8));
		return ucdxmlServiceImpl.createClosingDisclosureUCDXML(inputXmlStream);
    }

	@RequestMapping(value = "{version}/ping", method = { RequestMethod.GET })
    public String status(@PathVariable String version) throws Exception {
		LOG.info("user "+SecurityContextHolder.getContext().getAuthentication().getName()+" used Service: ping to Trim Service");
		return "The Service for generating UCD XML is running and ready to accept the requests";
    }
}
