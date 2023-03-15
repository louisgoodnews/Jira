import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.component.ComponentAccessor;

private void disableOptions (Issue issue, CustomField customField, Long parentOptionId, Long optionSequence, List<String> optionValues){
     
    OptionsManager optionsManager = ComponentAccessor.getOptionsManager();
    FieldConfig fieldConfig = customField.getRelevantConfig(issue);
    List<Option> optionList = optionsManager.getOptions(fieldConfig);
    optionValues.each { String optionValue ->
 
        optionList.find { Option option ->
 
            if (option.getValue().toString().equalsIgnoreCase(optionValue)){
 
                optionsManager.disableOption(option);
            }
        }
    }
}