import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.Issue;

import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.bc.issue.IssueService;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.Project;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;

import com.atlassian.jira.user.ApplicationUser;

import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

private MutableIssue cloneIssueObject(String sourceKey, String targetProjectKey, String issueTypeName, String summary, String description, ApplicationUser applicationUser){


    Logger logger = Logger.getLogger("de.louis.scriptrunner.console.methods.cloneIssueObject");
    logger.setLevel(Level.DEBUG);

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    IssueService issueService = ComponentAccessor.getIssueService();
    ProjectManager projectManager = ComponentAccessor.getProjectManager();
    CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
    IssueTypeManager issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class);
    PrioritySchemeManager prioritySchemeManager = ComponentAccessor.getComponent(PrioritySchemeManager.class);

    MutableIssue sourceObject = issueManager.getIssueObject(sourceKey);
    assert sourceObject ? "Found issue with provided key ${sourceKey} proceeding." : "No issue with provided key ${sourceKey} found! aborting exectution of method 'cloneIssueObject'!";
    Project project = projectManager.getProjectObjByKey(targetProjectKey);
    assert project ? "Found project with provided key ${targetProjectKey} proceeding." : "No project with provided key ${targetProjectKey} found! aborting exectution of method 'cloneIssueObject'!";
    IssueType issueType = issueTypeManager.getIssueTypes().find{ IssueType issueType -> issueType.getName().equalsIgnoreCase(issueTypeName) };
    assert issueType ? "Found issuetype with provided name ${issueTypeName} proceeding." : "No issuetype with provided name ${issueTypeName} found! aborting exectution of method 'cloneIssueObject'!";
    IssueContext issueContext = new IssueContextImpl(project, issueType) as IssueContext;
    applicationUser = applicationUser? applicationUser : ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    summary = summary? summary : sourceObject.getSummary();
    description = description ? description : sourceObject.getDescription();
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters().with{
        setProjectId(project.getId())
        setIssueTypeId(issueType.getId())
        setReporterId(applicationUser.getName())
        setSummary(summary)
        setDescription(description)
        setPriorityId(prioritySchemeManager.getDefaultOption(issueContext))
    } as IssueInputParameters;
    customFieldManager.getCustomFieldObjects(sourceObject).each{ CustomField customField ->
        
        sourceObject.getCustomFieldValue(customField) != null ? issueInputParameters.addCustomFieldValue(customField.getId(), sourceObject.getCustomFieldValue(customField).toString()) : null ;
    }
    try{
        
        IssueService.CreateValidationResult validationResult = issueService.validateCreate(applicationUser, issueInputParameters)
        assert validationResult.valid : validationResult.errorCollection;
        IssueService.IssueResult issue = issueService.create(applicationUser, validationResult);
        assert issue.valid : issue.errorCollection;
        return issue.getIssue();
    }catch(Exception e){
        
        logger.error("Calling method 'cloneIssueObject' failed with exception: ${e}");
    }
}