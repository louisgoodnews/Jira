import com.atlassian.jira.user.util.UserManager;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;

private LinkedHashMap<String, Serializable> findApplicationUserFromDirectory(String queryString, String directoryName){
    
    UserManager userManager = ComponentAccessor.getUserManager();
    UserSearchService userSearchService = ComponentAccessor.getUserSearchService();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
    LinkedList<ApplicationUser> applicationUserList = userSearchService.findUsers(serviceContext, queryString) as LinkedList<ApplicationUser>;
    List<Long> directoyIdList = applicationUserList*.getDirectoryId().unique();
    Long relevantDirectoryId = applicationUserList*.getDirectoryId().unique().find{ Long direcotyId ->

        userManager.getDirectory(direcotyId).getName().equalsIgnoreCase(directoryName);
    };
    LinkedList<ApplicationUser> result = applicationUserList.findAll{    ApplicationUser applicationUser ->

        applicationUser.getDirectoryId() == relevantDirectoryId
    } as LinkedList<ApplicationUser>;
    return ["no of results" : result.size(), "results" : result];
}
