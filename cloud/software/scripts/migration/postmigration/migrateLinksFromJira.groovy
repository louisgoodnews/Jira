
/*
    Description:
    This script is designed to migrate links from an on-premise Jira instance to a Jira Cloud instance.
    It first retrieves a list of projhects from the Cloud instance and then, for each project, retrieves a list of issues containing the URL from the on-premise instance.
    For each issue description and comment that contains a link to the on-premise instance, the link is replaced with the corresponding link on the Cloud instance. 

    To use this script, you'll need to specify the URLs for both the on-premise and Cloud instances, as well as the name of the logger to use for logging output.
    You can then execute the script as a Groovy script in a suitable environment, such as the Groovy Console or a Jenkins build step.
*/

// Import required packages
import java.util.ArrayList
import org.apache.log4j.Logger
import org.apache.log4j.Level
import io.github.openunirest.http.HttpResponse

// Initialize logger for logging messages
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.migrateLinksFromJira")
logger.setLevel(Level.INFO)

// Initialize on-premise and cloud base URLs
String onpremiseBaseUrl = ""
String cloudBaseUrl = ""

// Send a GET request to retrieve a list of projects from the Jira API
HttpResponse<Map> listOfProjectsResponse = get("rest/api/3/project/search")
                                        .header("Accept", "application/json")
                                        .header("Content-Type", "application/json")
                                        .asObject(Map)

// Check if the response status is not 200
if (listOfProjectsResponse.status != 200){
    // Log error and return error messages
    logger.error("GET 'listOfProjectsResponse' failed with 'status' ${listOfProjectsResponse.status} 'statusText' ${listOfProjectsResponse.statusText}!")
    return listOfProjectsResponse.body.errorMessages
}

// Loop through the projects and send a GET request to retrieve a list of issues for each project
listOfProjectsResponse.body.each.values.each{   Map project ->

    HttpResponse<Map> listOfIssuesResponse = get("/rest/api/3/search")
                                            .header("Accept", "application/json")
                                            .header("Content-Type", "application/json")
                                            .queryString("jql", "project = ${project.key} AND text ~ ${onpremiseBaseUrl}")
                                            .asObject(Map)

    // Check if the response status is not 200
    if (listOfIssuesResponse.status != 200){
        // Log error and return error messages
        logger.error("GET 'listOfIssuesResponse' failed with 'status' ${listOfIssuesResponse.status} 'statusText' ${listOfIssuesResponse.statusText}!")
        return listOfIssuesResponse.body.errorMessages
    }

    // Loop through the issues and update the comments if necessary
    listOfIssuesResponse.body.issues.each{   issue ->

        if(issue.description.contains(onpremiseBaseUrl)){

            // Send a PUT request to update the issue with the new comment text
            HttpResponse<Map> listOfIssuesResonse = put("/rest/api/3/issue/${issue.key}")
                                                .header("Accept", "application/json")
                                                .header("Content-Type", "application/json")
                                                .body(
                                                    [
                                                        "fiels":[

                                                        ]
                                                    ]
                                                )
                                                .asJson()

            // Check if the response status is not 200
            if (listOfIssuesResonse.status != 200){
                // Log error and return error messages
                logger.error("PUT 'listOfIssuesResonse' failed with 'status' ${listOfIssuesResonse.status} 'statusText' ${listOfIssuesResonse.statusText}!")
                return listOfIssuesResonse.body.errorMessages
            }
        } else {

            continue
        }

        // Send a GET request to retrieve the comments for the current issue
        HttpResponse<Map> listOfIssuesCommentsResonse = get("/rest/api/3/issue/${issue.key}/comment")
                                            .header("Accept", "application/json")
                                            .header("Content-Type", "application/json")
                                            .asObject(Map)
        
        // Check if the response status is not 200
        if (listOfIssuesCommentsResonse.status != 200){
            // Log error and return error messages
            logger.error("GET 'listOfIssuesCommentsResonse' failed with 'status' ${listOfIssuesCommentsResonse.status} 'statusText' ${listOfIssuesCommentsResonse.statusText}!")
            return listOfIssuesCommentsResonse.body.errorMessages
        }

        // Loop through the issues and update the comments if necessary
        listOfIssuesCommentsResonse.body.comments.each{ Map comment ->

            if(comment.body.content.content.text.contains(onpremiseBaseUrl)){
                
                HttpResponse<Map> updateCommentRequest = put("/rest/api/3/issue/${issue.key}/comment/${comment.id}")
                                            .header("Accept", "application/json")
                                            .header("Content-Type", "application/json")
                                            .body(
                                                [
                                                    "body":[
                                                        "content":[
                                                            [
                                                                "content":[
                                                                    [
                                                                        "text": comment.body.content.content.text.replaceAll(onpremiseBaseUrl, cloudBaseUrl)
                                                                        "type": "text"
                                                                    ]
                                                                ],
                                                                "type": "paragraph"
                                                            ]
                                                        ],
                                                        "type": "doc",
                                                        "version": comment.body.version
                                                    ]
                                                ]
                                            )
                                            .asObject(Map)
                        
                // Check if the response status is not 200
                if (updateCommentRequest.status != 200){
                    // Log error and return error messages
                    logger.error("PUT 'updateCommentRequest' failed with 'status' ${updateCommentRequest.status} 'statusText' ${updateCommentRequest.statusText}!")
                    return updateCommentRequest.body.errorMessages
                }
            } else {

                continue
            }
        }
    }
}

