
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType
import com.atlassian.applinks.api.ApplicationLinkRequestFactory
import com.atlassian.jira.security.JiraAuthenticationContext
import org.apache.http.client.methods.CloseableHttpResponse
import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.ApplicationLinkRequest
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.sal.api.component.ComponentLocator
import org.apache.http.impl.client.CloseableHttpClient
import com.atlassian.jira.component.ComponentAccessor
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.HttpClientBuilder
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.applinks.api.ApplicationLink
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import com.atlassian.jira.user.ApplicationUser
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import com.atlassian.jira.issue.IssueManager
import org.apache.http.entity.StringEntity
import java.nio.charset.StandardCharsets
import com.atlassian.sal.api.net.Request
import org.apache.http.util.EntityUtils
import com.atlassian.jira.issue.Issue
import org.apache.http.StatusLine
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import org.apache.log4j.Logger
import org.apache.log4j.Level

class CONSTANTS{

    static final String KEY_TARGET_SPACE = "TARGET_SPACE" //-> target space for page creation
    static final String ISSUEKEY = "<enter issue key here>"
    static final Long CF_ID_PROJECT_NUMBER = 13801L
    static final Long CF_ID_PROJECT_GOAL = 13804L
    static final Long CF_ID_PROJECT_FAIL = 15803L
    static final Long CF_ID_PRODUCT_CATEGORY = 17800L
    static final Long CF_ID_PRODUCT_CATEGORY_GROUP = 13813L
    static final Long CF_ID_PROJECT_OWNER = 15802L
    static final Long CF_ID_PROJECT_LIVE_VERSION = 15805L
    static final String REMOTE_BASE_URL = "https://confluence.lodego.de"
    static final LinkedHashMap<String, String> BLUEPRINTHASH = [
        "000000000" : "projectId",
        "000000000" : "projectTasks",
        "000000000" : "projectToDos",
        "000000000" : "productManagement",
        "000000000" : "projectManagement",
        "000000000" : "procurement",
        "000000000" : "afterSales",
        "000000000" : "ressourceDesign",
        "000000000" : "testValidation",
        "000000000" : "prep",
        "000000000" : "prototypeReports",
        "000000000" : "protocol",
        "000000000" : "statusReport"
    ]
}

Logger logger = Logger.getLogger("com.louis.console.debugging.createProjectDocumentation")
logger.setLevel(Level.DEBUG)

Issue issue;
StringBuilder webLink = new StringBuilder()
StringBuilder goodsGroupPageId = new StringBuilder()
StringBuilder projectTyprepageId = new StringBuilder()
StringBuilder projectTitlePageId = new StringBuilder()
LinkedList<String> pageTitleList = new LinkedList<String>()
LinkedList<String> pageContentList = new LinkedList<String>()
LinkedList<Object> labelParamsList = new LinkedList<Object>()
LinkedHashMap<String, String> webLinkHash = new LinkedHashMap<String, String>()
LinkedHashMap<String, Object> userDataParams = new LinkedHashMap<String, Object>()
LinkedHashMap<String, Object> pageContentVars = new LinkedHashMap<String, Object>()
LinkedHashMap<String, String> userPickerCustomFieldHash = new LinkedHashMap<String, String>()

IssueManager issueManager = ComponentAccessor.getIssueManager()
CommentManager commentManager = ComponentAccessor.getCommentManager()
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
if (!issue) {
	
	issue = issueManager.getIssueObject(CONSTANTS.ISSUEKEY) //-> Comment out this line, if this script is running in productive environment.
}

ApplicationUser currentUser = ComponentAccessor.getComponent(JiraAuthenticationContext).getLoggedInUser()
LinkedHashMap<String, String> requestHeadersMap = [
    "Content-Type": "application/json",
    "Accept": "application/json"
]

for (CustomField customFieldObject : customFieldManager.getCustomFieldObjects(issue)) {
	
    if (customFieldObject.getCustomFieldType().getName().toLowerCase().contains("user picker (single user)")) {
		
        userPickerCustomFieldHash.put(customFieldObject.getId(), customFieldObject.getName())
    }
}

if (userPickerCustomFieldHash) {
	
    for (String customFieldId : userPickerCustomFieldHash.keySet()) {
		
        ApplicationUser issueRelatedUser = (issue as Issue).getCustomFieldValue(customFieldManager.getCustomFieldObject(customFieldId) as CustomField) as ApplicationUser
        if (!issueRelatedUser) {
			
            commentManager.create(issue, currentUser, "Script execution was erroneous (Error 001). Please check the respective log entries for debugging.", false)
            return "Error 001 - Users related to issue could not be retrieved! ${userPickerCustomFieldHash.get(customFieldId)}"
        }
        LinkedHashMap<String, Object> getParticularUser = executeLinkedGetRequest("${CONSTANTS.REMOTE_BASE_URL}/rest/api/user?username=${issueRelatedUser.getName()}", logger)
        userDataParams.put(userPickerCustomFieldHash.get(customFieldId), [issueRelatedUser.getName(), getParticularUser.get("userKey")])
    }
} else {
	
    commentManager.create(issue, currentUser, "Script execution was erroneous (Error 002). Please check the respective log entries for debugging.", false)
    return "Error 002 - Users related to issue could not be retrieved in Confluence! ${userDataParams}"
}

for (String key : userDataParams.keySet()) {
	
    pageContentVars.put(new StringBuilder().append("var" + key.replace(" ", "") + "Id").toString(), userDataParams.get(key).getAt(1))    
}

pageContentVars.put("varTypeName", issue.getIssueType().getName())
pageContentVars.put("varPriorityName", issue.getPriority().getName())
pageContentVars.put("varStatusName", issue.getStatus().getName())
pageContentVars.put("varPLVName", issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(CONSTANTS.CF_ID_PROJECT_LIVE_VERSION))?.getDisplayName())
pageContentVars.put("varProductLineName", issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(CONSTANTS.CF_ID_PRODUCT_CATEGORY)))
pageContentVars.put("varProjectOwnerName", issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(CONSTANTS.CF_ID_PROJECT_OWNER))?.getDisplayName())
pageContentVars.put("varProjectNumberName", issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(CONSTANTS.CF_ID_PROJECT_NUMBER)))
pageContentVars.put("varProjectClassName", issue.getIssueType().getName())
pageContentVars.put("varGoodsGroupName", issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(CONSTANTS.CF_ID_PROJECT_NUMBER)).toString().find("[0-9]+"))
pageContentVars.put("varProjectGoal", issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(CONSTANTS.CF_ID_PROJECT_GOAL)))
pageContentVars.put("varProjectNonGoal", issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(CONSTANTS.CF_ID_PROJECT_FAIL)))
pageContentVars.put("varProjectTypeId", issue.getIssueType().getName().find("[0-9]+"))

if (pageContentVars.values().toString().contains("null")) { //check if issue data could be retrieved

    List<String> errorResult = []
    for (String key : pageContentVars.keySet()) {
		
        if (pageContentVars.get(key) == null) {
			
            errorResult.add(key)
        }
    }
    commentManager.create(issue, currentUser, "Script execution was erroneous (Error 003). Please check the respective log entries for debugging.", false)
    return "Error 003 - Issue data could not be retrieved! ${errorResult}"
}

for (String outerKey in CONSTANTS.BLUEPRINTHASH.keySet()) {
	
	if (outerKey.equals("128679937")) {
		
		String vProjectPageBody = executeWebRequest("", "${CONSTANTS.REMOTE_BASE_URL}/rest/experimental/template/${outerKey}?expand=body.storage.value", "GET", requestHeadersMap, null, true).get("body").get("body").get("storage").get("value").toString()
		for (String innerKey : pageContentVars.keySet()) {
			
			if (vProjectPageBody.contains(key)) {
				
				vProjectPageBody = vProjectPageBody.replaceAll(innerKey.toString(), pageContentVars.get(innerKey).toString())
			}
		}
		pageContentList.add(vProjectPageBody)
	} else {
		
		pageContentList.add(executeGetRequest("", "${CONSTANTS.REMOTE_BASE_URL}/rest/experimental/template/${outerKey}?expand=body.storage.value", logger).get("body").get("body").get("storage").get("value").toString())
	}
}

LinkedHashMap<String, Object> getParentSpace = executeLinkedGetRequest("${CONSTANTS.REMOTE_BASE_URL}/rest/api/space/${CONSTANTS.KEY_TARGET_SPACE}", logger)
LinkedHashMap<String, Object> getParentGoodsGroup = executeLinkedGetRequest("${CONSTANTS.REMOTE_BASE_URL}/rest/api/content?spaceKey=${getParentSpace.get("key")}&title=Warengruppe+${pageContentVars.get("varGoodsGroupName")}", logger)
if (getParentGoodsGroup.get("results").size() > 1) {
	
    commentManager.create(issue, currentUser, "Script execution was erroneous (Error 004). Please check the respective log entries for debugging.", false)
    return "Error 004 - Ambigouus goods group pages found!"
}
LinkedList<Object> goodsGroupDescendants = new LinkedList<Object>()
if (getParentGoodsGroup.get("results").size() == 1) {
	
    goodsGroupDescendants = executeLinkedGetRequest("${CONSTANTS.REMOTE_BASE_URL}/rest/api/content/${getParentGoodsGroup.get("results").getAt(0).get("id")}/child/page", logger).get("results") as LinkedList<Object>
    for (Object descendant : goodsGroupDescendants) {
			
		byte[] titleBytes = descendant.get("title").getBytes(StandardCharsets.US_ASCII)
		String descendantTitle = new String(titleBytes, StandardCharsets.US_ASCII)
		if (descendantTitle.contains(pageContentVars.get("varProjectTypeId"))) {
			
			projectTyprepageId.append(descendant.get("id"))
		}
    }
}

try{
    if (!getParentGoodsGroup.get("results")) {
		
        LinkedHashMap<String, Object> parentGoodsGroupParams = [
                type : "page",
                title : new StringBuilder().append("Warengruppe " + pageContentVars.get("varGoodsGroupName")),
                space : [
                        key: CONSTANTS.KEY_TARGET_SPACE
                ],
                body : [
                        storage: [
                            value         : "Liste mit Warengruppen - aus Meta-Daten erstellt, wie bei den protocoln",
                                representation: "storage"
                        ]
                ],
                ancestors:[
                    [
                        type : "page",
                        id : getParentSpace.get("_expandable").get("homepage").find("[0-9]+")
                    ]
                ]
            ]
        goodsGroupPageId.append(executeLinkedPostRequest("rest/api/content", parentGoodsGroupParams, logger).get("id"))
    } else {
		
         goodsGroupPageId.append(getParentGoodsGroup.get("results").getAt(0).get("id"))
    }
	
    if (!projectTyprepageId) {
		
        LinkedHashMap<String, Object> parentProjectTypreparams = [
                type : "page",
                title : new StringBuilder().append(pageContentVars.get("varGoodsGroupName") + " ⬝ " + pageContentVars.get("varProjectTypeId")),
                space : [
                        key: CONSTANTS.KEY_TARGET_SPACE
                ],
                body : [
                        storage: [
                            value         : "Liste mit Projekten - aus Meta-Daten erstellt, wie bei den protocoln",
                                representation: "storage"
                        ]
                ],
                ancestors:[
                    [
                        type : "page",
                        id : goodsGroupPageId
                    ]
                ]
            ]
        projectTyprepageId.append(executeLinkedPostRequest("rest/api/content", parentProjectTypreparams, logger).get("id"))
    }
	
    for (Integer i = 0; i < CONSTANTS.BLUEPRINTHASH.keySet().size(); i++) {
		
        if (i == 0) {
			
            LinkedHashMap<String, Object> projectAncestorPageParams = [
                type : "page",
                title : new StringBuilder().append(pageContentVars.get("varProjectNumberName") + " ⬝ " + CONSTANTS.BLUEPRINTHASH.get(i)),
                space : [
                    key: CONSTANTS.KEY_TARGET_SPACE
                ],
                body : [
                    storage: [
                        value         : pageContentList.getAt(i),
                        representation: "storage"
                    ]
                ],
                ancestors:[
                        [
                            type : "page",
                            id : projectTyprepageId
                        ]
                ]
            ] as LinkedHashMap<String, Object>
            LinkedHashMap<String, Object> createProjectAncestorPage = executeLinkedPostRequest("rest/api/content", projectAncestorPageParams, logger)
            projectTitlePageId.append(createProjectAncestorPage.get("id"))
            webLinkHash.put(new StringBuilder().append(pageContentVars.get("varProjectNumberName") + " ⬝ " + CONSTANTS.BLUEPRINTHASH.get(i)).toString(), new StringBuilder().append("${CONSTANTS.REMOTE_BASE_URL}/pages/viewpage.action?pageId=${projectTitlePageId}").toString())
            webLink.append("${CONSTANTS.REMOTE_BASE_URL}/pages/viewpage.action?pageId=${projectTitlePageId}")
            LinkedList<Object> projectAncestorLabelParams = [
                    [
                        name : "projectId"
                        
                    ],
                    [
                        name : pageContentVars.get("varProjectNumberName")
                        
                    ],
                    [
                        name : "Projekttyp_${pageContentVars.get("varProjectTypeId")}"
                    ],
                    [
                        name : "Warengruppe_${pageContentVars.get("varGoodsGroupName")}"
                    ]
            ] as LinkedList<Object>  
            executeLinkedPostRequest("rest/api/content/${createProjectAncestorPage.get("id")}/label", projectAncestorLabelParams, logger)
        }
		
        if (i > 0) {
			
            LinkedHashMap<String, Object> projectDescendantPageParams = [
                type : "page",
                title: new StringBuilder().append(pageContentVars.get("varProjectNumberName") + " ⬝ " + CONSTANTS.BLUEPRINTHASH.get(i)),
                space: [
                    key: CONSTANTS.KEY_TARGET_SPACE
                ],
                body : [
                    storage: [
                        value         : pageContentList.getAt(i),
                        representation: "storage"
                    ],
                ],
               ancestors:[
                        [
                            type : "page",
                            id : projectTitlePageId
                        ]
                ]
            ] as LinkedHashMap<String, Object>
            LinkedHashMap<String, Object> createProjectDescendantPage = executeLinkedPostRequest("rest/api/content", projectDescendantPageParams, logger)
            LinkedList<Object> projectDescendantLabelParams = [
                    [
                        name : "v_projekt"
                        
                    ],
                    [
                        name : pageContentVars.get("varProjectNumberName")
                        
                    ],
                    [
                        name : "Projekttyp_${pageContentVars.get("varProjectTypeId")}"
                    ],
                    [
                        name : "Warengruppe_${pageContentVars.get("varGoodsGroupName")}"
                    ]
            ] as LinkedList<Object>
            webLinkHash.put(new StringBuilder().append(pageContentVars.get("varProjectNumberName") + " ⬝ " + CONSTANTS.BLUEPRINTHASH.get(i)).toString(), new StringBuilder().append("${CONSTANTS.REMOTE_BASE_URL}/pages/viewpage.action?pageId=${projectTitlePageId}").toString())
            executeLinkedPostRequest("rest/api/content/${createProjectDescendantPage.get("id")}/label", projectDescendantLabelParams, logger)
        }
    }
}catch(Exception e) {
	
    logger.error("com.louis.console.debugging.createProjectDocumentation failed due to: ${e}")
}

commentManager.create(issue, currentUser, ("Hi there ${issue.getAssignee().getDisplayName()}, \n\n I've created the Confluence pages for you. You can find them [here|${webLink}]. \n\n Best regards, \n Scriptrunner.").toString(), false)

return webLinkHash

//____________________________________________________________________________________________________
private LinkedHashMap<String, Object> executeWebRequest (String requestAuthorization, String requestUrl, String requestMethod, LinkedHashMap<String, String> requestHeadersMap, LinkedHashMap<String, Object> requestBodyMap, Boolean doLog) {
  
    Logger logger = Logger.getLogger("de.louis.scriptrunner.console.methods.executeWebRequest");
    logger.setLevel(Level.DEBUG);
  
    CloseableHttpResponse responseObject;
    HttpUriRequest getRequestObject;
    HttpUriRequest postRequestObject;
    HttpUriRequest putRequestObject;
    HttpUriRequest deleteRequestObject;
  
    CloseableHttpClient clientObject = HttpClientBuilder.create().build();
    LinkedHashMap<String, Object> result = new LinkedHashMap();
      
    try {
        if (requestMethod.equalsIgnoreCase("GET")) {
  
            getRequestObject = new HttpGet(requestUrl);
            if (requestAuthorization) {
  
                getRequestObject.setHeader("Authorization", requestAuthorization);
            };
			
            if (requestHeadersMap) {
  
                requestHeadersMap.keySet().each { String requestHeaderKey ->
  
                    getRequestObject.setHeader(requestHeaderKey, requestHeadersMap.get(requestHeaderKey));
                };
            };
        } else if (requestMethod.equalsIgnoreCase("POST")) {
  
            postRequestObject = new HttpPost(requestUrl);
            if (requestAuthorization) {
  
                postRequestObject.setHeader("Authorization", requestAuthorization);
            };
			
            if (requestHeadersMap) {
  
                requestHeadersMap.keySet().each { String requestHeaderKey ->
  
                    postRequestObject.setHeader(requestHeaderKey, requestHeadersMap.get(requestHeaderKey));
                };
            };
			
            if (requestBodyMap) {
  
                postRequestObject.setEntity(new StringEntity(requestBodyMap.toString()));
            };
        } else if (requestMethod.equalsIgnoreCase("PUT")) {
  
            putRequestObject = new HttpPut(requestUrl);
            if (requestAuthorization) {
  
                putRequestObject.setHeader("Authorization", requestAuthorization);
            };
			
            if (requestHeadersMap) {
  
                requestHeadersMap.keySet().each { String requestHeaderKey ->
  
                    putRequestObject.setHeader(requestHeaderKey, requestHeadersMap.get(requestHeaderKey));
                };
            };
			
            if (requestBodyMap) {
  
                putRequestObject.setEntity(new StringEntity(requestBodyMap.toString()));
            };
        } else if (requestMethod.equalsIgnoreCase("DELETE")) {
  
            deleteRequestObject = new HttpDelete();
            if (requestAuthorization) {
  
                deleteRequestObject.setHeader("Authorization", requestAuthorization);
            };
            if (requestHeadersMap) {
  
                requestHeadersMap.keySet().each { String requestHeaderKey ->
  
                    deleteRequestObject.setHeader(requestHeaderKey, requestHeadersMap.get(requestHeaderKey));
                };
            };
        } else {
  
            doLog == true?:logger.error("No valid request method was provided - Method execution of 'executeWebRequest' aborted!");
            return;
        };
		
        if (getRequestObject) {
  
            responseObject = clientObject.execute(getRequestObject);
        } else if (postRequestObject) {
  
            responseObject = clientObject.execute(postRequestObject);
        } else if (putRequestObject)  {
  
            responseObject = clientObject.execute(putRequestObject);
        } else if (deleteRequestObject) {
  
            responseObject = clientObject.execute(deleteRequestObject);
        };
        Integer statusCode = responseObject.getStatusLine().getStatusCode();
        result.put("status", ["statusCode": statusCode, "statusText":  org.apache.commons.httpclient.HttpStatus.getStatusText(statusCode)]);
        if (responseObject.getEntity()) {
  
            result.put("body", new JsonSlurper().parseText(EntityUtils.toString(responseObject.getEntity())));
        };
		
    } catch (Exception e) {
  
        logger.error("Invoking of method 'executeWebRequest' failed with exception: ${e}.");
        return e.printStackTrace();
    } finally {
  
        return result;
    };
};

private LinkedHashMap<String, Object> executeLinkedGetRequest(String requestUrl, Logger logger) {
        final ApplicationLink confluenceLink = ComponentLocator.getComponent(ApplicationLinkService).getPrimaryApplicationLink(ConfluenceApplicationType)
        try{
			
			assert confluenceLink
		}catch(AssertionError ae) {
			
			logger.error("Assertion of confluenceLink failed due to: ${ae}")
		}
        ApplicationLinkRequestFactory authenticatedRequestFactory = confluenceLink.createImpersonatingAuthenticatedRequestFactory()
        LinkedHashMap<String, Object> result = new JsonSlurper().parseText(authenticatedRequestFactory.createRequest(Request.MethodType.GET, requestUrl).addHeader("Content-Type", "application/json").addHeader("Accept", "application/json").execute()) as LinkedHashMap<String, Object>
		return result
}

private LinkedHashMap<String, Object> executeLinkedPostRequest(String requestUrl, Object requestParams, Logger logger) {
        final ApplicationLink confluenceLink = ComponentLocator.getComponent(ApplicationLinkService).getPrimaryApplicationLink(ConfluenceApplicationType)
        try{
			
			assert confluenceLink
		}catch(AssertionError ae) {
			
			logger.error("Assertion of confluenceLink failed due to: ${ae}")
		}
        ApplicationLinkRequestFactory authenticatedRequestFactory = confluenceLink.createImpersonatingAuthenticatedRequestFactory()
        LinkedHashMap<String, Object> result = new JsonSlurper().parseText(authenticatedRequestFactory.createRequest(Request.MethodType.POST, requestUrl).addHeader("Content-Type", "application/json").addHeader("Accept", "application/json").setRequestBody(new JsonBuilder(requestParams).toString()).execute()) as LinkedHashMap<String, Object>
		return result
}