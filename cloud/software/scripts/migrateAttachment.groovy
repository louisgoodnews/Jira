
/**
 * This script migrates attachments from a source Jira issue to a target Jira issue.
 * Ensure that you provide the appropriate values for the sourceIssueKey, targetIssueKey,
 * and authorization variables before executing the script.
 */

//Import
import org.apache.log4j.Logger
import org.apache.log4j.Level
import java.io.File
import java.io.FileOutputStream
import java.net.URL

//Set up the logger
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.migrateAttachment")
logger.setLevel(Level.DEBUG)

String sourceIssueKey = "" //Key of the source issue
String targetIssueKey = "" //Key of the target issue
String authorization = "" //Replace with your email adress : your Jira API token

//Get the source issue
HttpResponse<Map> sourceIssueResponse = get("rest/api/latest/issue/${sourceIssueKey}")
    .header("Accept", "application/json")
    .header("Content-Type", "application/json")
    .header("Authorization", "Basic ${authorization}")
    .asObject(Map)

//Check if the response status is not 200
if (sourceIssueResponse.status != 200) {
    //Log error and return error messages
    logger.error("GET 'sourceIssue' failed with 'status' ${sourceIssueResponse.status} 'statusText' ${sourceIssueResponse.statusText}!")
    return sourceIssueResponse.body.errorMessages
}

//Extract attachments from the source issue
List<Map<String, Object>> sourceIssueAttachments = sourceIssueResponse.body.fields.attachment

//Iterate through each attachment and migrate to the target issue
for (Map<String, Object> attachment : sourceIssueAttachments) {
    String attachmentId = attachment.id //Assuming 'id' is the key for attachment ID in the response
    String attachmentFileName = attachment.filename
    String attachmentContentUrlString = attachment.content //Assuming 'content' is the key for attachment content URL in the response

    //Fetch the attachment content from the URL
    URL attachmentContentUrl = new URL(attachmentContentUrlString);
    URLConnection connection = attachmentContentUrl.openConnection();
    connection.setRequestProperty("Authorization", authorization);
    connection.connect();
    
    InputStream inputStream = connection.getInputStream();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    
    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
        byteArrayOutputStream.write(buffer, 0, bytesRead);
    }
    
    byte[] attachmentContent = byteArrayOutputStream.toByteArray();
    
    //Close the input stream and output stream
    inputStream.close();
    byteArrayOutputStream.close();

    //Create a temporary file and write the attachment content to it
    File tempFile = File.createTempFile("temp_attachment_", attachmentFileName)
    FileOutputStream fos = new FileOutputStream(tempFile)
    fos.write(attachmentContent)
    fos.close()

    //Create the attachment in the target issue
    HttpResponse<Map> createAttachmentResponse = post("rest/api/latest/issue/${targetIssueKey}/attachments")
        .header("Accept", "application/json")
        .header("Authorization", authorization)
        .header("X-Atlassian-Token", "no-check")
        .field("file", tempFile, attachmentFileName)
        .asObject(Map)

    //Check if the response status is not 200
    if (createAttachmentResponse.status != 200) {
        //Log error and return error messages
        logger.error("POST 'attachment' failed with 'status' ${createAttachmentResponse.status} 'statusText' ${createAttachmentResponse.statusText}!")
        return createAttachmentResponse.body.errorMessages
    }

    //Delete the temporary file
    tempFile.delete()

    //Log success message for each attachment migrated
    logger.info("Attachment with ID ${attachmentId} migrated from issue ${sourceIssueKey} to ${targetIssueKey}")
}

//Return a success message
return "Attachments migrated successfully from issue ${sourceIssueKey} to ${targetIssueKey}"