import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.user.util.UserManager;

import com.atlassian.jira.user.ApplicationUser;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserSearchService;

import com.atlassian.jira.bc.user.ApplicationUserBuilderImpl;

private void updateUserDomains(String sourceDomain, String targetDomain){
    
	UserManager userManager = ComponentAccessor.getUserManager();
    UserSearchService userSearchService = ComponentAccessor.getUserSearchService();
	
    JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
    LinkedList<ApplicationUser> applicationUserList = userSearchService.findUsers(jiraServiceContext, "") as LinkedList<ApplicationUser>;
	
	applicationUserList.each{ ApplicationUser applicationUser ->
	   
	   if(!applicationUser.getEmailAddress().contains(sourceDomain)){
		   
		   String currentDomain = applicationUser.getEmailAddress().find("@+[A-Za-z]+.[A-Za-z]+/gm");
		   userManager.updateUser(new ApplicationUserBuilderImpl(applicationUser).emailAddress(applicationUser.getEmailAddress().replace(currentDomain, targetDomain)).build())
	   }
	};
}