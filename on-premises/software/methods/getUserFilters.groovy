import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;

private LinkedList<HashMap> getUserFilters(){

    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.findUserFilters");
    logger.setLevel(Level.DEBUG);

    ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService);
    UserSearchService userSearchService = ComponentAccessor.getUserSearchService();
    JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(currentUser);

    LinkedList<HashMap> result = new LinkedList();
    LinkedList<ApplicationUser> applicationUserList = userSearchService.findUsersAllowEmptyQuery(jiraServiceContext, "") as LinkedList<ApplicationUser>;
    assert applicationUserList;
    applicationUserList.each{ ApplicationUser applicationUser -> 

        List<HashMap> searchRequestDataList = [];
        for(SearchRequest searchRequest : searchRequestService.getOwnedFilters(applicationUser)){

            searchRequestDataList.add("name": searchRequest.getName(), "id": searchRequest.getId(), "self": "/issues/?filter="+searchRequest.getId(), "queryString": searchRequest.getQuery().getQueryString());
        }
        result.add("displayname": applicationUser.getDisplayName(), "key": applicationUser.getKey(), "filters": searchRequestDataList);
    }
    return result;
}