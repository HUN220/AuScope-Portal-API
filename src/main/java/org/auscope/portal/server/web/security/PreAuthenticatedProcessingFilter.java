package org.auscope.portal.server.web.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * This AbstractPreAuthenticatedProcessingFilter implementation 
 * obtains the username from request header pre-populated by an 
 * external Shibboleth authentication system.
 * 
 * 
 * @author san218
 * @version $Id$
 */
public class PreAuthenticatedProcessingFilter 
   extends AbstractPreAuthenticatedProcessingFilter {

   protected final Logger logger = Logger.getLogger(getClass());
   
   protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
	
	  java.util.Enumeration eHeaders = request.getHeaderNames();
      while(eHeaders.hasMoreElements()) {
         String name = (String) eHeaders.nextElement();
         if ( ( name.matches(".*Shib.*") || name.matches(".*shib.*") ) && 
              !name.equals("HTTP_SHIB_ATTRIBUTES") && 
              !name.equals("Shib-Attributes") ) 
         {
               Object object = request.getHeader(name);
               String value = object.toString();
               logger.debug("Shib header - " + name + " : " + value);
         }
      }
      
      if (request.getHeader("Shib-Shared-Token") != null) {
    	  
	      logger.info("Shib-Person-mail: " + request.getHeader("Shib-Person-mail"));
	      request.getSession().setAttribute("Shib-Person-mail", request.getHeader("Shib-Person-mail"));
	      request.getSession().setAttribute("Shib-Shared-Token", request.getHeader("Shib-Shared-Token"));
	      request.getSession().setAttribute("Shib-Person-commonName", request.getHeader("Shib-Person-commonName"));
      }
      
      return request.getSession().getAttribute("Shib-Person-mail");
   }
   
   protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
      // no password - user is already authenticated
      return "NONE";
   }
}