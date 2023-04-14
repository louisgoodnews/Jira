import org.apache.log4j.Logger
import io.github.openunirest.http.HttpResponse

Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migration.postmigration.deleteAllProjects")

HttpResponse<Map> getAllProjectsResponse = get("rest/api/latest/project/search")
                                            .header("Accept", "application/json")
                                            .header("Content-Type", "application/json")
                                            .asObject(Map)

// Check if the response status is not 200
if (getAllProjectsResponse.status != 200){

    // Log error and return error messages
    logger.error("GET 'getAllProjectsResponse' failed with 'status' ${getAllProjectsResponse.status} 'statusText' ${getAllProjectsResponse.statusText}!")
    return getAllProjectsResponse.body.errorMessages
}

for(Map project : getAllProjectsResponse.body.values){

    HttpResponse<Map> deleteProjectRepsonse = delete("rest/api/latest/project/${project.id}")
                                                .header("Accept", "application/json")
                                                .header("Content-Type", "application/json")
                                                .asObject(Map)

    // Check if the response status is not 200
    if (deleteProjectRepsonse.status != 200){

        // Log error and return error messages
        logger.error("DELETE 'deleteProjectRepsonse' failed with 'status' ${deleteProjectRepsonse.status} 'statusText' ${deleteProjectRepsonse.statusText}!")
        return deleteProjectRepsonse.body.errorMessages
    } else{

        logger.info("Successfully deleted project ${project.name} - ${project.key}")
    }
}