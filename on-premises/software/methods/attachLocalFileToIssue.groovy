import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean.Builder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.IssueManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

private void attachLocalFileToIssue(String issueKey, String filename, String pathOfFile, String contentType, Boolean doLog){

    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.attachLocalFileToIssue");
    logger.setLevel(Level.DEBUG);

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    AttachmentManager attachmentManager = ComponentAccessor.getAttachmentManager();

    MutableIssue issue = issueManager.getIssueObject(issueKey);
    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

    File localFile = new File("${pathOfFile}${if(!pathOfFile.endsWith("/")){"/"}else{""}}${filename}");

    CreateAttachmentParamsBean createAttachmentParamsBean = new CreateAttachmentParamsBean.Builder()
    .file(localFile)
    .filename(filename)
    .contentType(contentType)
    .author(applicationUser)
    .issue(issue)
    .copySourceFile(true)
    .build()

    attachmentManager.createAttachment(createAttachmentParamsBean);
    return;
}