import  com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import  com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import  com.atlassian.applinks.api.ApplicationLinkService;
import  com.atlassian.sal.api.component.ComponentLocator;
import  com.atlassian.applinks.api.ApplicationLink;
import  com.atlassian.sal.api.net.Request;
import  groovy.json.JsonSlurper;
import  org.apache.log4j.Logger;
import  org.apache.log4j.Level;
 
private LinkedHashMap<String, Object> executeLinkedPostRequest(String requestUrl, LinkedHashMap<String, Object> requestBody) {
     
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.executeLinkedPostRequest");
    logger.setLevel(Level.DEBUG);
    final ApplicationLink confluenceLink = ComponentLocator.getComponent(ApplicationLinkService).getPrimaryApplicationLink(ConfluenceApplicationType);
    try {
         
        assert confluenceLink;
    } catch (AssertionError ae) {
         
        logger.error("Assertion of 'confluenceLink' failed with AssertionError ${ae}");
    }
    ApplicationLinkRequestFactory authenticatedRequestFactory = confluenceLink.createAuthenticatedRequestFactory();
    return new JsonSlurper().parseText(authenticatedRequestFactory.createRequest(Request.MethodType.POST, requestUrl).addHeader("Content-Type", "application/json").addHeader("Accept", "application/json").setRequestBody(requestBody.toString()).execute()) as LinkedHashMap<String, Object>;
}