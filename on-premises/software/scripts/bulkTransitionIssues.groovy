/*
  This script can be implemented as listener or scheduled job for your Jira instance.
  With this code you can fetch a list of issues via JQL search and then transition each issue.
*/

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.workflow.TransitionOptions;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.workflow.TransitionOptions.Builder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
 
class CONSTANTS{
 
    static final String JQL_QUERY_STRING = ''; //-> add jql query string from issue navigator
    static final Integer ID_TRANSITION = 0; //-> add transition id from workflow editor
}
 
executeJqlSearch(CONSTANTS.JQL_QUERY_STRING, false).each{ MutableIssue issue ->
 
    transitionIssue (issue, CONSTANTS.ID_TRANSITION, false)
}
 
return;
 
//METHOD:
private LinkedList<MutableIssue> executeJqlSearch(String searchString, Boolean doLog){
    Logger logger;
    if (doLog) {
 
        logger = Logger.getLogger("de.louis.scriptrunner.method.executeJqlSearch");
        logger.setLevel(Level.TRACE);
    }
    LinkedList<MutableIssue> result = new LinkedList<MutableIssue>();
    JqlQueryParser jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser.class);
    SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
    IssueManager issueManager = ComponentAccessor.getIssueManager();
  
    try {
  
        Query query = jqlQueryParser.parseQuery(searchString);
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter() as PagerFilter;
        SearchResults<Issue> searchResults = searchService.search(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), query, pagerFilter);
        searchResults.getResults().each { def item ->
            result.add(issueManager.getIssueObject(item.id));
        }
        if (doLog) {
            logger.trace("Found ${searchResults.getResults().size()} results for query with string: ${searchString}");
        }
    } catch (Exception e) {
  
        e.printStackTrace();
    } finally {
  
        return result;
    }
}
 
private void transitionIssue (MutableIssue issue, Integer transitionId, Boolean doLog) {
    Logger logger;
    if (doLog) {
 
        logger = Logger.getLogger("de.louis.scriptrunner.method.transitionIssue");
        logger.setLevel(Level.TRACE);
    }
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    IssueService issueService = ComponentAccessor.getIssueService();
    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    TransitionOptions transitionOptions = new TransitionOptions.Builder().skipConditions().skipPermissions().skipValidators().build();
    IssueInputParametersImpl issueInputParams = new IssueInputParametersImpl();
    try {
         
        IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(applicationUser, issue.getId(), transitionId, issueInputParams, transitionOptions);
        IssueService.IssueResult issueResult = issueService.transition(applicationUser, transitionValidationResult);
        if (issueResult.isValid()) {
 
            if (doLog) {
 
                logger.trace("Issue ${issue.getKey()} successfully transitioned.");
            }
        } else {
 
            if (doLog) {
                 
                logger.error("Issue ${issue.getKey()} was not sucessfully transitioned.");
                transitionValidationResult.getErrorCollection().each { Error error ->
                    logger.error(error.getMessage());
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }
 
}