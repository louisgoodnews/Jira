
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.attachment.AttachmentDirectoryAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

private void placeIssueAttachmentLocally(String issueKey, String filename, String targetpath){

    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.placeIssueAttachmentLocally");
    logger.setLevel(Level.DEBUG);

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    AttachmentManager attachmentManager = ComponentAccessor.getAttachmentManager();
    AttachmentDirectoryAccessor attachmentDirectoryAccessor = ComponentAccessor.getComponent(AttachmentDirectoryAccessor);

    MutableIssue issue = issueManager.getIssueObject(issueKey);
    assert issue ? null : logger.error("No issue with key ${issueKey} found!");
    Attachment relevantAttachment = attachmentManager.getAttachments(issue).find{   Attachment attachment

        attachment.getFilename().equalsIgnoreCase(filename);
    }
    try{

        if(targetpath[targetpath.length() - 1].equals("/")){

            new File("${attachmentDirectoryAccessor.getAttachmentDirectory(issue)}/${filename}").renameTo("${targetpath}${filename}");
            logger.info("Successfully placed ${filename} at ${targetpath}");
        }else{

            new File("${attachmentDirectoryAccessor.getAttachmentDirectory(issue)}/${filename}").renameTo("${targetpath}/${filename}");
            logger.info("Successfully placed ${filename} at ${targetpath}");
        }
    }catch(Exception e){

        logger.error("Invoking method 'placeIssueAttachmentLocally' caught exception ${e}.");
    }
    return;
}