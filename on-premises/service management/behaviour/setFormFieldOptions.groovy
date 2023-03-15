import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;

private void setFormFieldOptions (def formField, List<String> optionValuesList) {

	CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
	OptionsManager optionsManager = ComponentAccessor.getOptionsManager();
	
	def customField = customFieldManager.getCustomFieldObject(formField.getId());
	def issueConfig = customFieldManager.getRelevantConfig(getIssueContext());
	def issueOptions = optionsManager.getOptions(issueConfig);
	
	formField.setFieldOptions(issueOptions.find{ it.value in optionValuesList });
	return;
}