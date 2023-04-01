
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/*

    To use this script, you'll need the following:
    * a select list (single select) customfield to reflect the projectTypeKey of the project that should be created
    * a user picker customfield for the users that are to be added as project members
*/

class CONSTANTS{

    static String AUTHORIZATION = ""; //provide a 64bit encoded authorization string
}

Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts");
logger.setLevel(Level.DEBUG);

HttpResponse createProject;

HttpResponse groups = get("/rest/api/latest/groups/picker")
    .header("Accept", "application/json")
    .asObject(Map);

if(groups.status != 200){

    logger.error("Retrieving groups failed!");
    return groups.body;
}

HttpResponse projectRoles = get("/rest/api/latest/role")
    .header("Accept", "application/json")
    .asObject(Map);

if(projectRoles.status != 200){

    logger.error("Retrieving project roles failed!");
    return projectRoles.body;
}

switch(issue.customfield_00000) {

    case "Business":

        createProject = post("/rest/api/latest/project")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", CONSTANTS.AUTHORIZATION)
            .body(
                [
                    "key": "${(issue.summary.findAll('[A-Z]+') as ArrayList).join()}",
                    "name": "${issue.summary}",
                    "description": "${issue.description}",
                    "leadAccountId": "${issue.reporter.accountId}",
                    "assigneeType": "PROJECT_LEAD",
                    "issueSecurityScheme": "", //-> to be retriedved via UI
                    "notificationScheme": "", //-> to be retriedved via UI
                    "projectTypeKey": "business", //-> you can use 'business', 'service_desk' or 'software'
                    "workflowScheme": "", //-> to be retriedved via UI
                    "issueTypeScreenScheme": "", //-> to be retriedved via UI
                    "issueTypeScheme": "", //-> to be retriedved via UI
                    "fieldConfigurationScheme": "" //-> to be retriedved via UI
                ]
            )
            .asObject(Map);
    break;

    case "Service Management":

        createProject = post("/rest/api/latest/project")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", CONSTANTS.AUTHORIZATION)
            .body(
                [
                    "key": "${(issue.summary.findAll('[A-Z]+') as ArrayList).join()}",
                    "name": "${issue.summary}",
                    "description": "${issue.description}",
                    "leadAccountId": "${issue.reporter.accountId}",
                    "assigneeType": "PROJECT_LEAD",
                    "issueSecurityScheme": "", //-> to be retriedved via UI
                    "notificationScheme": "", //-> to be retriedved via UI
                    "projectTypeKey": "service_desk", //-> you can use 'business', 'service_desk' or 'software'
                    "workflowScheme": "", //-> to be retriedved via UI
                    "issueTypeScreenScheme": "", //-> to be retriedved via UI
                    "issueTypeScheme": "", //-> to be retriedved via UI
                    "fieldConfigurationScheme": "" //-> to be retriedved via UI
                ]
            )
            .asObject(Map);
    break;

    case "Software":

        createProject = post("/rest/api/latest/project")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", CONSTANTS.AUTHORIZATION)
            .body(
                [
                    "key": "${(issue.summary.findAll('[A-Z]+') as ArrayList).join()}",
                    "name": "${issue.summary}",
                    "description": "${issue.description}",
                    "leadAccountId": "${issue.reporter.accountId}",
                    "assigneeType": "PROJECT_LEAD",
                    "issueSecurityScheme": "", //-> to be retriedved via UI
                    "notificationScheme": "", //-> to be retriedved via UI
                    "projectTypeKey": "software", //-> you can use 'business', 'service_desk' or 'software'
                    "workflowScheme": "", //-> to be retriedved via UI
                    "issueTypeScreenScheme": "", //-> to be retriedved via UI
                    "issueTypeScheme": "", //-> to be retriedved via UI
                    "fieldConfigurationScheme": "" //-> to be retriedved via UI
                ]
            )
            .asObject(Map);
    break;
}

if(createProjectRequest.status != 200){

    logger.error("Creating a project failed!");
    return createProjectRequest.body;
}
