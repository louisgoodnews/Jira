
/*
	This method can be implemented in your groovy Jira script.
	Simply provide a name for the relevant organisation and a project key as string, and your'e good to go!
	This method will remove said customer organisation from your Jira Service Management project.
*/

import com.atlassian.servicedesk.api.util.paging.PagedResponse;
import com.atlassian.servicedesk.api.organization.OrganizationServiceDeskUpdateParameters;
import com.atlassian.servicedesk.api.organization.OrganizationsQuery;
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
 
private void removeOrganisationFromProject (String organisationName, String projectKey) {
  
    @WithPlugin("com.atlassian.servicedesk")
    @PluginModule ServiceDeskManager serviceDeskManager;
    @PluginModule OrganizationService organizationService;
  
    LinkedList<ApplicationUser> result;
  
    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.service-management.methods.removeOrganisationFromProject");
    logger.setLevel(Level.DEBUG);
  
    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    ServiceDesk serviceDesk = serviceDeskManager.getServiceDeskForProject(ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));
    OrganizationsQuery organizationsQuery = organizationService.newOrganizationsQueryBuilder().serviceDeskId(serviceDesk.getId()).build();
    PagedResponse<CustomerOrganization> customerOrganizations = organizationService.getOrganizations(applicationUser, organizationsQuery);
    CustomerOrganization customerOrganization = customerOrganizations.getResults().find{ CustomerOrganization it -> it.name == organisationName};
    assert customerOrganization;
    OrganizationServiceDeskUpdateParameters organizationServiceDeskUpdateParameters = organizationService.newOrganizationServiceDeskUpdateParametersBuilder().organization(customerOrganization).serviceDeskId(serviceDesk.getId()).build();
  
    if(organizationServiceDeskUpdateParameters){
        try {
 
            organizationService.removeOrganizationFromServiceDesk(applicationUser, organizationServiceDeskUpdateParameters);
            logger.info("Removed organisation with name ${organisationName} from project with key ${projectKey}");
            return;
        } catch (Exception e) {
 
            logger.error("Could not remove organisation ${organisationName} from project with key ${projectKey} due to exception ${e}.");
            return;
        }
    }
}