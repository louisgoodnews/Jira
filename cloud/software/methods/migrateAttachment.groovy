
import org.apache.log4j.Logger
import org.apache.log4j.Level

public static String migrateAttachments(String sourceIssueKey, String targetIssueKey, String authorization) {
        Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.methods.migrateAttachment")
        logger.setLevel(Level.INFO);

        try {
            // Step 1: Get the source issue
            HttpResponse<Map> sourceIssueResponse = get("rest/api/latest/issue/${sourceIssueKey}")
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic ${authorization}")
                    .asObject(Map.class);

            // Check if the response status is not 200
            if (sourceIssueResponse.status != 200) {
                // Log error and return error messages
                logger.error("GET 'sourceIssue' failed with 'status' ${sourceIssueResponse.status} 'statusText' ${sourceIssueResponse.statusText}!");
                return String.format("Failed to retrieve source issue '%s'.", sourceIssueKey);
            }

            // Step 2: Extract attachments from the source issue
            List<Map<String, Object>> sourceIssueAttachments = (List<Map<String, Object>>) sourceIssueResponse.body.get("attachments");

            // Step 3: Iterate through each attachment and migrate to the target issue
            for (Map<String, Object> attachment : sourceIssueAttachments) {
                String attachmentId = (String) attachment.get("id");
                String attachmentFileName = (String) attachment.get("filename");
                String attachmentContentUrlString = (String) attachment.get("content");

                // Step 3a: Fetch the attachment content from the URL
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

                // Close the input stream and output stream
                inputStream.close();
                byteArrayOutputStream.close();

                // Step 3b: Create a temporary file and write the attachment content to it
                File tempFile = File.createTempFile("temp_attachment_", attachmentFileName);
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(attachmentContent);
                fos.close();

                // Step 3c: Create the attachment in the target issue
                HttpResponse<Map> createAttachmentResponse = post("rest/api/latest/issue/${targetIssueKey}/attachments")
                        .header("Accept", "application/json")
                        .header("Authorization", authorization)
                        .header("X-Atlassian-Token", "no-check")
                        .field("file", tempFile, attachmentFileName)
                        .asObject(Map.class);

                // Check if the response status is not 200
                if (createAttachmentResponse.status != 200) {
                    // Log error and return error messages
                    logger.error("POST 'attachment' failed with 'status' ${createAttachmentResponse.status} 'statusText' ${createAttachmentResponse.statusText}!");
                    return String.format("Failed to create attachment '%s' in the target issue '%s'.", attachmentFileName, targetIssueKey);
                }

                // Delete the temporary file
                tempFile.delete();
            }
        }catch(Exception e){
            logger.error("Caught exception ${e.getMessage()} with cause ${e.getCause()}") // Log the error message along with the cause of the exception
        }
}