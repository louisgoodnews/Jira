import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.Issue;

private MutableIssue getEpicIssue(Issue issue){

	IssueManager issueManager = ComponentAccessor.getIssueManager();
	CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
	
	CustomField epicLinkObject = customFieldManager.getCustomFieldObjects().find{	CustomField customField -> customField.getName().equalsIgnoreCase("Epic Link")};
	
	return issueManager.getIssueObject((issue.getCustomFieldValue(epicLinkObject) as Issue).getKey());
}