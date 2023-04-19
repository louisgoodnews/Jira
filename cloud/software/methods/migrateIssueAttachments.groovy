// Import required packages
import org.apache.log4j.Logger  // Importing the Logger class from the log4j library
import org.apache.log4j.Level   // Importing the Level class from the log4j library

// Defining a private function named migrateIssueAttachments with two string parameters
private Map migrateIssueAttachments(String sourceIssueKey, String targetIssueKey){

    try{
            // Initialize logger for logging messages
            Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.migrateIssueAttachment") // Creating a Logger instance for logging messages
            logger.setLevel(Level.INFO) // Setting the logging level for the logger instance to INFO

            // Send a GET request to retrieve information about the source issue using the sourceIssueKey
            HttpResponse<Map> sourceIssueResponse = get("rest/api/latest/issue/${sourceIssueKey}")
                                                        .header("Accept", "application/json")
                                                        .header("Content-Type", "application/json")
                                                        .asObject(Map)

            // Check if the response status is not 200
            if (sourceIssueResponse.status != 200){

                // Log error and return error messages
                logger.error("GET '${sourceIssueKey}' failed with 'status' ${sourceIssueResponse.status} 'statusText' ${sourceIssueResponse.statusText}!")
                return sourceIssueResponse.body.errorMessages // Return the error messages received from the sourceIssueResponse
            }
            
            // Send a GET request to retrieve information about the target issue using the targetIssueKey
            HttpResponse<Map> targetIssueResponse = get("rest/api/latest/issue/${targetIssueKey}")
                                                        .header("Accept", "application/json")
                                                        .header("Content-Type", "application/json")
                                                        .asObject(Map)

            // Check if the response status is not 200
            if (targetIssueResponse.status != 200){

                // Log error and return error messages
                logger.error("GET '${targetIssueKey}' failed with 'status' ${targetIssueResponse.status} 'statusText' ${targetIssueResponse.statusText}!")
                return targetIssueResponse.body.errorMessages // Return the error messages received from the targetIssueResponse
            }

            // Send a PUT request to update the target issue with the attachments of the source issue
            HttpResponse<Map> updateTargetIssueResponse = post("rest/api/latest/issue/${targetIssueKey}/attachments")
                                                        .header("Accept", "application/json")
                                                        .header("Content-Type", "application/json")
                                                        .header("X-Atlassian-Token", "no-check")
                                                        .field(targetIssueResponse.body.fields.attachment[0].name, targetIssueResponse.body.fields.attachment[0].content)
                                                        .asJson()
    }catch(Exception e){

        logger.error("Caught exception ${e.getMessage()} with cause ${e.getCause()}") // Log the error message along with the cause of the exception
    }
}
