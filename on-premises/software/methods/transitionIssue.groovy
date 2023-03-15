import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.workflow.TransitionOptions;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.workflow.TransitionOptions.Builder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
 
private void transitionIssue (MutableIssue issue, Integer transitionId, Boolean doLog) {
    Logger logger;
    if (doLog) {
 
        logger = Logger.getLogger("de.louis.scriptrunner.method.transitionIssue");
        logger.setLevel(Level.TRACE);
    }
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    IssueService issueService = ComponentAccessor.getIssueService();
    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    TransitionOptions transitionOptions = new TransitionOptions.Builder().skipConditions().skipPermissions().skipValidators().build();
    IssueInputParametersImpl issueInputParams = new IssueInputParametersImpl();
    try {
 
        IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(applicationUser, issue.getId(), transitionId, issueInputParams, transitionOptions);
        IssueService.IssueResult issueResult = issueService.transition(applicationUser, transitionValidationResult);
        if (issueResult.isValid()) {
 
            if (doLog) {
 
                logger.trace("Issue ${issue.getKey()} successfully transitioned.");
            }
        } else {
 
            if (doLog) {
                 
                logger.error("Issue ${issue.getKey()} was not sucessfully transitioned.");
                transitionValidationResult.getErrorCollection().each { Error error ->
                    logger.error(error.getMessage());
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }
 
}