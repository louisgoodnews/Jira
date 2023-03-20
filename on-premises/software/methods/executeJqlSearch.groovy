import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.Issue;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
 
private LinkedList<MutableIssue> executeJqlSearch(String searchString, Boolean doLog){
    Logger logger;
    if (doLog) {
 
        logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.executeJqlSearch");
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
    } catch (Exception e) {
  
        logger.error("The task failed due to ${e}.");
    } finally {
  
        return result;
    }
}