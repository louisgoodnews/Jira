import com.atlassian.crowd.model.group.Group
import com.atlassian.jira.bc.group.search.GroupPickerSearchService
import com.atlassian.jira.component.ComponentAccessor
 
private LinkedList<Group> findUserGroups (String queryString) {
 
    GroupPickerSearchService groupPickerSearchService = ComponentAccessor.getComponent(GroupPickerSearchService);
    return groupPickerSearchService.findGroups(queryString) as LinkedList<Group>;
}