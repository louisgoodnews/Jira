// Import required packages
import org.apache.log4j.Logger
import org.apache.log4j.Level
import io.github.openunirest.http.HttpResponse

// Initialize logger for logging messages
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.addCurrentUserToAllProjects")
logger.setLevel(Level.INFO)

String relevantProjectRole = ""
String jiraBaseUrl = ""

// Send a GET request to retrieve information about the current user
HttpResponse<Map> currentUserRequest = get("/rest/api/latest/myself")
                                        .header("Accept", "application/json")
                                        .header("Content-Type", "application/json")
                                        .asObject(Map)

// Check if the response status is not 200
if (currentUserRequest.status != 200){

    // Log error and return error messages
    logger.error("GET 'currentUserRequest' failed with 'status' ${currentUserRequest.status} 'statusText' ${currentUserRequest.statusText}!")
    return currentUserRequest.body.errorMessages
}

// Send a GET request to retrieve a list of projects from the Jira API
HttpResponse<Map> getProjectsResponse = get("rest/api/3/project/search")
                                        .header("Accept", "application/json")
                                        .header("Content-Type", "application/json")
                                        .asObject(Map)

// Check if the response status is not 200
if (getProjectsResponse.status != 200){

    // Log error and return error messages
    logger.error("GET 'getProjectsResponse' failed with 'status' ${getProjectsResponse.status} 'statusText' ${getProjectsResponse.statusText}!")
    return getProjectsResponse.body.errorMessages
}

try{

    // Loop through each project in the response
    for(Map project : getProjectsResponse.body.values){

        // Send a GET request to retrieve the roles for the current project
        HttpResponse<Map> getProjectRolesFromProjectResponse = get("/rest/api/latest/project/${project.key}/role")
                                                                .header("Accept", "application/json")
                                                                .header("Content-Type", "application/json")
                                                                .asObject(Map)

        // Check if the response status is not 200
        if (getProjectRolesFromProjectResponse.status != 200){

            // Log error and return error messages
            logger.error("GET 'getProjectRolesFromProjectResponse' failed with 'status' ${getProjectRolesFromProjectResponse.status} 'statusText' ${getProjectRolesFromProjectResponse.statusText}!")
            return getProjectRolesFromProjectResponse.body.errorMessages
        }

        // Send a POST request to update the "administrators" role for the current project
        HttpResponse<Map> updateProjectRoleActorRequest = put(getProjectRolesFromProjectResponse.body.get(relevantProjectRole).replace(jiraBaseUrl, ""))
                                                            .header("Accept", "application/json")
                                                            .header("Content-Type", "application/json")
                                                            .body([
                                                                "categorisedActors": [
                                                                    "atlassian-user-role-actor":[
                                                                        currentUserRequest.body.accountId
                                                                    ]
                                                                ]
                                                            ])
                                                            .asObject(Map)
    }

}catch(Exception e){
    logger.error("Caught exception ${e.getMessage()} with cause ${e.getCause()}")
}
