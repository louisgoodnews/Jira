import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.Issue;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.Project;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;

import com.atlassian.jira.user.ApplicationUser;

import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.issuetype.IssueType;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
;

import com.atlassian.jira.config.ConstantsManager;

import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

private MutableIssue createIssueObject(String projectKey, String summary, String description,String issueTypeName, ApplicationUser applicationUser){

    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.createIssueObject");
    logger.setLevel(Level.DEBUG);

    IssueService issueService = ComponentAccessor.getIssueService();
    ProjectManager projectManager = ComponentAccessor.getProjectManager();
    ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
    IssueTypeManager issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class);
    PrioritySchemeManager prioritySchemeManager = ComponentAccessor.getComponent(PrioritySchemeManager.class);

    Project project = projectManager.getProjectObjByKey(projectKey.toUpperCase());
    applicationUser = applicationUser? applicationUser : ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    IssueType issueType = issueTypeManager.getIssueTypes().find{ IssueType issueType -> issueType.getName().equalsIgnoreCase(issueTypeName)};
    IssueContext issueContext = new IssueContextImpl(project, issueType) as IssueContext;
    try{

        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters().with {
            setProjectId(project.getId())
            setIssueTypeId(issueType.getId())
            setReporterId(applicationUser.getName())
            setSummary(summary)
            setDescription(description)
            setPriorityId(prioritySchemeManager.getDefaultOption(issueContext))
        } as IssueInputParameters;
        IssueService.CreateValidationResult validationResult = issueService.validateCreate(applicationUser, issueInputParameters)
        assert validationResult.valid : validationResult.errorCollection;
        IssueService.IssueResult issue = issueService.create(applicationUser, validationResult);
        assert issue.valid : issue.errorCollection;
        logger.info("Successfully created ${issue.getIssue().getKey()}!");
        return issue.getIssue();
    }catch(Exception e){

        logger.error("Calling method 'createIssueObject' failed with exception: ${e}");
    }
}