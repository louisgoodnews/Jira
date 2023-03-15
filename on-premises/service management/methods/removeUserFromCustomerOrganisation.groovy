
/*
  This method can be implemented in your groovy Jira scripts.
  When calling this method, provide an organisation name, a project key, and a list of application user objects.
  This method will remove the given users from the customer organisation.
*/

import com.atlassian.servicedesk.api.util.paging.PagedResponse;
import com.atlassian.servicedesk.api.organization.OrganizationsQuery;
import com.atlassian.servicedesk.api.organization.UsersOrganizationUpdateParameters;
import com.atlassian.servicedesk.api.organization.CustomerOrganization;
import com.atlassian.servicedesk.api.ServiceDesk;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;
import com.atlassian.jira.project.Project;
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import org.apache.log4j.Logger;
import com.atlassian.jira.project.ProjectManager;
import org.apache.log4j.Level;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.servicedesk.api.ServiceDeskManager;
import com.atlassian.servicedesk.api.organization.OrganizationService;
  
private void removeUsersFromCustomerOrganisation (String organisationName, String projectKey, ApplicationUser applicationUser) {
  
    @WithPlugin("com.atlassian.servicedesk")
    @PluginModule ServiceDeskManager serviceDeskManager;
    @PluginModule OrganizationService organizationService;
  
    LinkedList<ApplicationUser> result;
  
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.removeUsersFromCustomerOrganisation");
    logger.setLevel(Level.DEBUG);
  
    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    ServiceDesk serviceDesk = serviceDeskManager.getServiceDeskForProject(ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));
    OrganizationsQuery organizationsQuery = organizationService.newOrganizationsQueryBuilder().serviceDeskId(serviceDesk.getId()).build();
    PagedResponse<CustomerOrganization> customerOrganizations = organizationService.getOrganizations(applicationUser, organizationsQuery);
    UsersOrganizationUpdateParameters usersOrganizationUpdateParameters = organizationService.newUsersOrganizationUpdateParametersBuilder().organization(customerOrganizations.getResults().find{ CustomerOrganization it -> it.name == organisationName}).users([applicationUser] as Set<ApplicationUser>).build();
    if (usersOrganizationUpdateParameters){
  
        try{
  
            organizationService.removeUsersFromOrganization(applicationUser, usersOrganizationUpdateParameters);
        } catch (Exception e){
  
            logger.error("Could not remove users ${usersToRemove*.getKey()} from organisation ${organisationName} in project ${projectKey} due to exception ${e}.");
            return;
        }
    } else {
  
        logger.error("Could not create UsersOrganizationUpdateParameters usersOrganizationUpdateParameters. Execution of method 'removeUsersFromOrganisation' aborted!");
        return;
    }
}