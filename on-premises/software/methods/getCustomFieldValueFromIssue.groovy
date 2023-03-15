import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;

private def getCustomFieldValueFromIssue (String issuekey, String customFieldName){
    return ComponentAccessor.getIssueManager().getIssueObject(issuekey).getCustomFieldValue(ComponentAccessor.getCustomFieldManager().getCustomFieldObjects().find{ CustomField it -> it.getName().equalsIgnoreCase(customFieldName)})
}

private def getCustomFieldValueFromIssue (String issuekey, String customFieldId){
    return ComponentAccessor.getIssueManager().getIssueObject(issuekey).getCustomFieldValue(ComponentAccessor.getCustomFieldManager().getCustomFieldObjects().find{ CustomField it -> it.getid().equalsIgnoreCase(customFieldId)})
}

private def getCustomFieldValueFromIssue (String issuekey, Long customFieldId){
    return ComponentAccessor.getIssueManager().getIssueObject(issuekey).getCustomFieldValue(ComponentAccessor.getCustomFieldManager().getCustomFieldObjects().find{ CustomField it -> it.getId() == customFieldId})
}