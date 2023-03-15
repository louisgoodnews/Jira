import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

private LinkedHashMap<String, Serializable> fieldCustomFieldId (String customFieldName){

    final Logger logger = Logger.getLogger("de.louis.scriptrunner.console.getCustomFieldIds");
    logger.setLevel(Level.DEBUG);

    final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
    LinkedList<CustomField> listOfCustomFields = customFieldManager.getCustomFieldObjects().findAll{ CustomField customField -> customField.getName().toLowerCase().contains(customFieldName.toLowerCase())} as LinkedList<CustomField>;
    LinkedList<String> result = new LinkedList();

    try{

        listOfCustomFields.each{ CustomField customField ->
            
            if(customField.getName().contains(" ") || customField.getName().contains("-")){

                String[] stringList;
                StringBuilder outerStringBuilder = new StringBuilder();
                if(customField.getName().contains(" ")){                

                    stringList = customField.getName().split(" ");
                }else if(customField.getName().contains("-")){

                    stringList = customField.getName().split("-");
                }
                for(Integer i = 0; i < stringList.size(); i++){

                    if(i == 0){

                        outerStringBuilder.append(stringList[i].toLowerCase());
                    }else{

                        outerStringBuilder.append(stringList[i].substring(0, 1).toUpperCase());
                        outerStringBuilder.append(stringList[i].substring(1).toLowerCase());
                    }
                }
                result.add(new String("String ${outerStringBuilder} = \"${customField.getId()}\"; //${customField.getName()}"));
                outerStringBuilder.delete(0, outerStringBuilder.length());
            }else{

                result.add(new String("String ${customField.getName().toLowerCase()} = \"${customField.getId()}\"; //${customField.getName()}"));
            }
        }
    } catch(Exception e){

        logger.error("Executing script failed with exception: ${e}!");
    }
    return ["size": listOfCustomFields.size(), "fields":result];
}