
/*
  This method can be implemented in your groovy Jira scripts.
  When calling this method, provide the name of the relevant customer organisation and a project key.
  This method will return a list of application users.
*/

import com.atlassian.servicedesk.api.organization.CustomerOrganization;
import com.atlassian.servicedesk.api.organization.OrganizationsQuery.Builder;
import com.atlassian.servicedesk.api.organization.OrganizationsQuery;
import com.atlassian.servicedesk.api.organization.UsersInOrganizationQuery;
import com.atlassian.servicedesk.api.organization.UsersInOrganizationQuery.Builder;
import com.atlassian.servicedesk.api.ServiceDeskService;
import com.atlassian.servicedesk.api.ServiceDesk;
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.atlassian.jira.project.Project;
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import org.apache.log4j.Logger;
import com.atlassian.jira.project.ProjectManager;
import org.apache.log4j.Level;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.servicedesk.api.ServiceDeskManager;
import com.atlassian.servicedesk.api.organization.OrganizationService;
 
private LinkedList<ApplicationUser> getCustomerOrganisationMembers (String organisationName, String projectKey) {
 
    @WithPlugin("com.atlassian.servicedesk")
    @PluginModule ServiceDeskManager serviceDeskManager;
    @PluginModule OrganizationService organizationService;
 
    LinkedList<ApplicationUser> result;
 
    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.service-management.methods.getCustomerOrganisationMembers");
    logger.setLevel(Level.DEBUG);
 
    UsersInOrganizationQuery.Builder usersInOrganizationQueryBuilder = ComponentAccessor.getComponent(UsersInOrganizationQuery.Builder.class);
    OrganizationsQuery.Builder organizationsQueryBuilder = ComponentAccessor.getComponent(OrganizationsQuery.Builder.class);
    ServiceDeskService serviceDeskService = ComponentAccessor.getComponent(ServiceDeskService.class);
    ProjectManager projectManager = ComponentAccessor.getProjectManager();
 
    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    ServiceDesk serviceDesk = serviceDeskManager.getServiceDeskForProject(projectManager.getProjectObjByKey(projectKey));
    assert serviceDesk;
    CustomerOrganization customerOrganisation = organizationService.getOrganizations(applicationUser, organizationService.newOrganizationsQueryBuilder().serviceDeskId(serviceDesk.getId()).build()).find{ CustomerOrganization customerOrganizationObject -> customerOrganizationObject.getName().equalsIgnoreCase(organisationName)} as CustomerOrganization;
    assert customerOrganisation;
    UsersInOrganizationQuery usersInOrganizationQuery = organizationService.newUsersInOrganizationQuery().customerOrganization(customerOrganisation).build();
    result = organizationService.getUsersInOrganization(applicationUser, usersInOrganizationQuery) as LinkedList<ApplicationUser>;
    if(result.size() > 0){
        return result;
    } else{
 
        logger.warn("No organisation members found for organisation with name ${organisationName} in project ${projectKey}!");
        return null;
    }
}