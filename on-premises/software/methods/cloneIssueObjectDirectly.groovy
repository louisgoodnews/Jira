import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.Issue;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;

import com.atlassian.jira.user.ApplicationUser;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CloneValidationResult;
import com.atlassian.jira.bc.issue.IssueService.AsynchronousTaskResult;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

private void cloneIssueObjectDirectly(String issueKey, String projectKey, String summary, ApplicationUser applicationUser, Boolean cloneAttachments, Boolean cloneSubTasks, Boolean cloneLinks){

    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.cloneIssueObjectDirectly");
    logger.setLevel(Level.DEBUG);
    
    LinkedHashMap<CustomField, Optional<Boolean>> cloneOptionSelections = new LinkedHashMap();

    IssueService issueService = ComponentAccessor.getIssueService();
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

    MutableIssue sourceIssue = issueManager.getIssueObject(issueKey);
    customFieldManager.getCustomFieldObjects(sourceIssue).each{ CustomField customField ->
        
        cloneOptionSelections.put(customField, Optional.of(true));
    };
    applicationUser = applicationUser? applicationUser : ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    try{

        IssueService.CloneValidationResult validationResult = issueService.validateClone(applicationUser, sourceIssue, summary, cloneAttachments, cloneSubTasks, cloneLinks, cloneOptionSelections);
        assert validationResult.valid : validationResult.errorCollection;
        IssueService.AsynchronousTaskResult cloneTask = issueService.clone(applicationUser, validationResult);
        assert cloneTask.valid : cloneTask.errorCollection;
    }catch(Exception e){

        logger.error("Calling method 'cloneIssueObjectDirectly' failed with exception: ${e}");
    }finally{
        
        return;
    }
}