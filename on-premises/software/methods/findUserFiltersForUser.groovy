import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;

private LinkedList<HashMap> findUserFiltersForUser(String queryString){

    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.findUserFiltersForUser");
    logger.setLevel(Level.DEBUG);

    ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService);
    UserSearchService userSearchService = ComponentAccessor.getUserSearchService();
    JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(currentUser);
    
    if(!queryString){

        logger.error("Execution of method 'findUserFiltersForUser' failed due to invalid query string ${queryString}!");
        return;
    } else{

        LinkedList<HashMap> result = new LinkedList();
        LinkedList<ApplicationUser> applicationUserList = userSearchService.findUsersAllowEmptyQuery(jiraServiceContext, queryString) as LinkedList<ApplicationUser>;
        assert applicationUserList;
        for(ApplicationUser applicationUser : applicationUserList){

            Collection<SearchRequest> searchRequestCollection = searchRequestService.getOwnedFilters(applicationUser);
            List<HashMap> searchRequestDataList = [];
            if (searchRequestCollection.size() > 0){
                for(SearchRequest searchRequest : searchRequestCollection){
                    
                    searchRequestDataList.add("name": searchRequest.getName(), "id": searchRequest.getId(), "self": "/issues/?filter="+searchRequest.getId(), "queryString": searchRequest.getQuery().getQueryString());
                }
                result.add("displayname": applicationUser.getDisplayName(), "key": applicationUser.getKey(), "filters": searchRequestDataList);
            }
        }
        if (result.size() > 0) {

            return result;
        } else {

            return;
        }
    }
}