import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;

private LinkedHashMap<String, Serializable> findApplicationUser(String queryString){
    
    UserSearchService userSearchService = ComponentAccessor.getUserSearchService();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
    LinkedList<ApplicationUser> result = userSearchService.findUsers(serviceContext, queryString) as LinkedList<ApplicationUser>;
    return ["no of results" : result.size(), "results" : result];
}