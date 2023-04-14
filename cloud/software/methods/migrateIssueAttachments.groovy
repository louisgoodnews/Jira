// Import required packages
import org.apache.log4j.Logger
import org.apache.log4j.Level

private Map migrateIssueAttachments(String sourceIssueKey, String targetIssueKey){

    try{
            // Initialize logger for logging messages
            Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.migrateIssueAttachment")
            logger.setLevel(Level.INFO)

            HttpResponse<Map> sourceIssueResponse = get("rest/api/latest/issue${sourceIssueKey}")
                                                        .header("Accept", "application/json")
                                                        .header("Content-Type", "application/json")
                                                        .asObject(Map)

            // Check if the response status is not 200
            if (sourceIssueResponse.status != 200){

                // Log error and return error messages
                logger.error("GET '${sourceIssueKey}' failed with 'status' ${sourceIssueResponse.status} 'statusText' ${sourceIssueResponse.statusText}!")
                return sourceIssueResponse.body.errorMessages
            }
            
            HttpResponse<Map> targetIssueResponse = get("rest/api/latest/issue${targetIssueKey}")
                                                        .header("Accept", "application/json")
                                                        .header("Content-Type", "application/json")
                                                        .asObject(Map)

            // Check if the response status is not 200
            if (targetIssueResponse.status != 200){

                // Log error and return error messages
                logger.error("GET '${targetIssueKey}' failed with 'status' ${targetIssueResponse.status} 'statusText' ${targetIssueResponse.statusText}!")
                return targetIssueResponse.body.errorMessages
            }

            HttpResponse<Map> updateTargetIssueResponse = put("rest/api/latest/issue${targetIssueKey}")
                                                        .header("Accept", "application/json")
                                                        .header("Content-Type", "application/json")
                                                        .body([
                                                            "fields": [
                                                                "attachment": targetIssueResponse.body.fields.attachment.toString()
                                                            ]
                                                        ])
                                                        .asObject(Map)
    }catch(Exception e){

        logger.error("Caught exception ${e.getMessage()} with cause ${e.getCause()}")
    }
}