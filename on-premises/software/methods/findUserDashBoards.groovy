import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.portal.PortalPage;

private LinkedList<HashMap> findUserDashBoards(String queryString){

    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.findUserDashBoards");
    logger.setLevel(Level.DEBUG);

    ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService);
    UserSearchService userSearchService = ComponentAccessor.getUserSearchService();
    PortalPageService portalPageService = ComponentAccessor.getComponent(PortalPageService);
    JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(currentUser);

    LinkedList<HashMap> result = new LinkedList();
    LinkedList<ApplicationUser> applicationUserList = userSearchService.findUsersAllowEmptyQuery(jiraServiceContext, queryString) as LinkedList<ApplicationUser>;
    assert applicationUserList;
    applicationUserList.each{ ApplicationUser applicationUser -> 

        List<HashMap> dashboardDataList = [];
        for(PortalPage portalPage : portalPageService.getOwnedPortalPages(applicationUser)){

            dashboardDataList.add("name": portalPage.getName(), "id": portalPage.getId(), "self": "/secure/Dashboard.jspa?selectPageId="+portalPage.getId());
        }
        result.add("displayname": applicationUser.getDisplayName(), "key": applicationUser.getKey(), "dashboards": dashboardDataList);
    }
    return ["no of results" : result.size(), "results" : result];
}