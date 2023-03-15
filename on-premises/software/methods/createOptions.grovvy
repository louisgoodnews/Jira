import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.component.ComponentAccessor;
 
private List<Option> createOptions (Issue issue, CustomField customField, Long parentOptionId, Long optionSequence, List<String> optionValues){
 
    OptionsManager optionsManager = ComponentAccessor.getOptionsManager();
    FieldConfig fieldConfig = customField.getRelevantConfig(issue);
    
    return optionsManager.createOptions(fieldConfig, parentOptionId, optionSequence, optionValues);
}