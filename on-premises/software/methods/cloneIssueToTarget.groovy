import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.Issue;

import com.atlassian.jira.user.ApplicationUser;
    
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

private MutableIssue cloneIssueToTarget(String issueKey){
    
    Logger logger = Logger.getLogger("de.louis.scriptrunner.console.methods.cloneIssueToTarget");
    logger.setLevel(Level.DEBUG);
    
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    
    MutableIssue issueToClone = issueManager.getIssueObject(issueKey);
    MutableIssue cloneOfIssue = ComponentAccessor.getIssueFactory().cloneIssueWithAllFields(issueToClone);    
    try{
        
        issueManager.createIssueObject(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), cloneOfIssue);
    }catch(Exception e){
        
        logger.error("Calling method 'cloneIssueToTarget' caught exception: ${e}");
    }    
    return cloneOfIssue;
}