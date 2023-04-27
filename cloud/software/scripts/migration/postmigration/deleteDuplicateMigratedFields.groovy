// Import required packages
import org.apache.log4j.Logger
import org.apache.log4j.Level
import io.github.openunirest.http.HttpResponse

// Initialize logger for logging messages
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.deleteDuplicateMigratedFields")
logger.setLevel(Level.INFO)

String authenticationString = "{YOUR EMAIL:YOUR PASSWORD}" //-> your email:password encoded to base64

HttpResponse<Map> allCustomFields = get("rest/api/latest/fields?type=custom")
                                        .header("Accept", "application/json")
                                        .header("Content-Type", "application/json")
                                        .header("Authorization", "Basic ${authenticationString}")
                                        .asObject(Map)

// Check if the response status is not 200
if (allCustomFields.status != 200){

    // Log error and return error messages
    logger.error("GET 'allCustomFields' failed with 'status' ${allCustomFields.status} 'statusText' ${allCustomFields.statusText}!")
    return allCustomFields.body.errorMessages
}

logger.info("Found ${allCustomFields.body.values.size()} customfields.")
try{

    for(Map customField : allCustomFields.body.values){

        String customFieldName = customField.name

        if(customFieldName.contains("(migrated)") || customFieldName.contains("(migrated ")){

            HttpResponse<Map> deleteCustomField = delete("rest/api/latest/field/${customField.id}")
                                                    .header("Accept", "application/json")
                                                    .header("Content-Type", "application/json")
                                                    .header("Authorization", "Basic ${authenticationString}")
                                                    .asObject(Map)

            // Check if the response status is not 200
            if (deleteCustomField.status != 200){

                // Log error and return error messages
                logger.error("DELETE 'deleteCustomField' failed with 'status' ${deleteCustomField.status} 'statusText' ${deleteCustomField.statusText}!")
                return deleteCustomField.body.errorMessages
            }
        }
    }
}catch (Exception e){
    
    logger.error("Caught exception: ${e.getMessage()} with cause:  ${e.getCause()}")
}