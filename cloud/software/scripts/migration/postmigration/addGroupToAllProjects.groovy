// Import required packages
import org.apache.log4j.Logger
import org.apache.log4j.Level
import io.github.openunirest.http.HttpResponse

#TODO update code

// Initialize logger for logging messages
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.addGroupToAllProjects")
logger.setLevel(Level.INFO)

String relevantProjectRole = ""
String relevantGroupName = ""
String jiraBaseUrl = ""

// Send a GET request to retrieve information about the current user
HttpResponse<Map> allGroupsResponse = get("/rest/api/latest/groups/picker")
                                        .header("Accept", "application/json")
                                        .header("Content-Type", "application/json")
                                        .asObject(Map)

// Check if the response status is not 200
if (allGroupsResponse.status != 200){

    // Log error and return error messages
    logger.error("GET 'allGroupsResponse' failed with 'status' ${allGroupsResponse.status} 'statusText' ${allGroupsResponse.statusText}!")
    return allGroupsResponse.body.errorMessages
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
                                                                    "atlassian-group-role-actor-id":[
                                                                        allGroupsResponse.body.groups.find{ Map group ->

                                                                            group.name.equalsIgnoreCase(relevantGroupName).groupId
                                                                        }
                                                                    ]
                                                                ]
                                                            ])
                                                            .asObject(Map)
    }

}catch(Exception e){
    logger.error("Caught exception ${e.getMessage()} with cause ${e.getCause()}")
}
