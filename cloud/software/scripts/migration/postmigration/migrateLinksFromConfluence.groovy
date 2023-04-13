
/*
    Description:
    This script is designed to migrate links from an on-premise Confluence instance to a Confluence Cloud instance.
    It first retrieves a list of spaces from the Cloud instance and then, for each space, retrieves a list of pages.
    For each page that contains a link to the on-premise instance, the link is replaced with the corresponding link on the Cloud instance. 

    To use this script, you'll need to specify the URLs for both the on-premise and Cloud instances, as well as the name of the logger to use for logging output.
    You can then execute the script as a Groovy script in a suitable environment, such as the Groovy Console or a Jenkins build step.
*/

// Import required packages
import java.util.ArrayList
import org.apache.log4j.Logger
import org.apache.log4j.Level
import io.github.openunirest.http.HttpResponse

// Initialize logger for logging messages
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.migrateLinksFromConfluence")
logger.setLevel(Level.INFO)

// Initialize on-premise and cloud base URLs
String onpremiseBaseUrl = ""
String cloudBaseUrl = ""

// Retrieve list of spaces from the cloud API
HttpResponse<Map> arrayOfSpacesResponse = get("/wiki/api/v2/spaces")
                                        .header("Accept", "application/json")
                                        .header("Content-Type", "application/json")
                                        .asObject(Map)

// Check if the response status is not 200
if (arrayOfSpacesResponse.status != 200){
    // Log error and return error messages
    logger.error("Getting 'arrayOfSpaces' failed with 'status' ${arrayOfSpacesResponse.status} 'statusText' ${arrayOfSpacesResponse.statusText}!")
    return arrayOfSpacesResponse.body.errorMessages
}

// Iterate over each space in the list of spaces
arrayOfSpacesResponse.body.results*.id.each { Integer spaceId ->
    // Retrieve list of pages for the current space from the cloud API
    HttpResponse<Map> arrayOfSpacePagesResponse = get("/wiki/api/v2/spaces/${spaceId}/pages")
                                                .header("Accept", "application/json")
                                                .header("Content-Type", "application/json")
                                                .asObject(Map)

    // Check if the response status is not 200
    if (arrayOfSpacePagesResponse.status != 200){
        // Log error and return error messages
        logger.error("Getting 'arrayOfSpacePages' failed with 'status' ${arrayOfSpacePagesResponse.status} 'statusText' ${arrayOfSpacePagesResponse.statusText}!")
        return arrayOfSpacePagesResponse.body.errorMessages
    }

    // Iterate over each page in the list of pages for the current space
    arrayOfSpacePagesResponse.body.results.each { Map<String, Object> spacePage ->
        // Check if the page body is not empty
        if (spacePage.body != null){
        // Check if the page body value contains the on-premise base URL
            if (spacePage.body.value.contains(onpremiseBaseUrl)){
                // Replace the on-premise base URL with the cloud base URL in the page body and update the page
                HttpResponse<Map> updateSpacePageBodyResponse = put("/wiki/api/v2/pages/${spacePage.id}")
                                                                .header("Accept", "application/json")
                                                                .header("Content-Type", "application/json")
                                                                .body([
                                                                    "id": spacePage.id,
                                                                    "status": spacePage.status,
                                                                    "title": spacePage.title,
                                                                    "spaceId": spacePage.spaceId,
                                                                    "body":[
                                                                        "representation": "storage",
                                                                        "value": spacePage.body.replaceAll(onpremiseBaseUrl, cloudBaseUrl)
                                                                    ]
                                                                ])
                                                                .asJson()
                // Check if the response status is not 200
                if (updateSpacePageBodyResponse.status != 200){
                    // Log error and return error messages
                    logger.error("Updating page with ID ${spacePage.id} failed with 'status' ${updateSpacePageBodyResponse.status} 'statusText' ${updateSpacePageBodyResponse.statusText}!")
                    return updateSpacePageBodyResponse.body.errorMessages
                }
            }
        }
    }
}
