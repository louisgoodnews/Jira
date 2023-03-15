import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.ProjectManager;

private HashMap<String, Long> collectAttachmentSizesForProject(String projectKey){

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    ProjectManager projectManager = ComponentAccessor.getProjectManager();
    AttachmentManager attachmentManager = ComponentAccessor.getAttachmentManager();

    HashMap<String, Long> result = new HashMap();
    ArrayList<String> issueDataArray = new ArrayList();
    ArrayList<String> issueTypeDataArray = new ArrayList();
    ArrayList<String> issueTypeNameArray = new ArrayList();

    Project projectObject = projectManager.getProjectObjByKey(projectKey);

    LinkedList<Issue> issueList = issueManager.getIssueObjects(issueManager.getIssueIdsForProject(projectObject.getId())) as LinkedList<Issue>;

    issueList.each { Issue issue ->

        Long issueAttachmentsSize = 0;
        List<Attachment> issueAttachments = attachmentManager.getAttachments(issue);
        issueAttachments.each{ Attachment attachment ->

            issueAttachmentsSize = issueAttachmentsSize += attachment.getFilesize();
        }
        if(!result.keySet().contains(issue.getIssueType().getName())){

            if (result.keySet().contains(issue.getIssueType().getName())){

                Long mappedAttachmentSize = result.get(issue.getIssueType().getName());
                issueAttachmentsSize = mappedAttachmentSize += issueAttachmentsSize;
                result.put(issue.getIssueType().getName(),issueAttachmentsSize);
            }
            result.put(issue.getIssueType().getName(), issueAttachmentsSize);
        }
        if (result.keySet().contains(issue.getIssueType().getName())){

            Long mappedAttachmentSize = result.get(issue.getIssueType().getName());
            issueAttachmentsSize = mappedAttachmentSize += issueAttachmentsSize;
            result.put(issue.getIssueType().getName(),issueAttachmentsSize);
        }
    }
    Long totalAttachmentSize = 0;
    result.values().each{ Long attachmentSize ->


        totalAttachmentSize += attachmentSize
    }
    result.put("total", totalAttachmentSize);
    return result;
}