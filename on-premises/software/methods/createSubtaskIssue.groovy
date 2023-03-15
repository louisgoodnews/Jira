import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;

private Issue createSubtaskIssue(Issue parentIssue, String issueTypeName, ApplicationUser reporterObject, String summary){
    
    IssueService issueService = ComponentAccessor.getIssueService();
    ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();

    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    assert parentIssue : "No parent issue present!";
    Collection<IssueType> subtaskIssueTypes = constantsManager.getAllIssueTypeObjects().findAll{ IssueType it -> it.isSubTask() };
    IssueType targetIssueType = subtaskIssueTypes.find{ IssueType it -> it.name == issueTypeName};
    assert targetIssueType : "No issue type with name ${issueTypeName} found";
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters().with{
        setProjectId(parentIssue.getProjectId())
        setIssueTypeId(targetIssueType.getId())
        setReporterId(reporterObject.getKey())
        setSummary(summary)
    }
    IssueService.CreateValidationResult validationResult = issueService.validateSubTaskCreate(applicationUser, parentIssue.getId(), issueInputParameters);
    assert validationResult : "Assertion of validation result for sub task creation failed!";
    IssueService.IssueResult issueResult = issueService.create(applicationUser, validationResult);
    assert issueResult.valid : issueResult.errorCollection;
    ComponentAccessor.subTaskManager.createSubTaskIssueLink(parentIssue, issueResult.getIssue(), applicationUser);
    return issueResult.getIssue();
}