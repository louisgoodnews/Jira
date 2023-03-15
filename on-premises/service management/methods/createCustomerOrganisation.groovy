
/*
  This method can be implemented in your groovy Jira scripts.
  When calling this method simply provide a organisation name and a project key.
  This method will return a newly created customer organisation for your Jira Service Management project.
*/

import com.atlassian.servicedesk.api.organization.OrganizationServiceDeskUpdateParameters;
import com.atlassian.servicedesk.api.organization.CreateOrganizationParameters;
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
 
private CustomerOrganization createCustomerOrganisation (String organisationName, String projectKey) {
 
    @WithPlugin("com.atlassian.servicedesk")
    @PluginModule ServiceDeskManager serviceDeskManager;
    @PluginModule OrganizationService organizationService;
 
    LinkedList<ApplicationUser> result;
 
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.createCustomerOrganisation");
    logger.setLevel(Level.DEBUG);
 
    ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    ServiceDesk serviceDesk = serviceDeskManager.getServiceDeskForProject(ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));
    CreateOrganizationParameters createOrganizationParameters = organizationService.newCreateBuilder().name(organisationName).build();
    CustomerOrganization customerOrganization = organizationService.createOrganization(applicationUser, createOrganizationParameters)
    assert customerOrganization;
    OrganizationServiceDeskUpdateParameters organizationServiceDeskUpdateParameters = organizationService.newOrganizationServiceDeskUpdateParametersBuilder().organization(customerOrganization).serviceDeskId(serviceDesk.getId()).build();
 
    if(organizationServiceDeskUpdateParameters){
 
        organizationService.addOrganizationToServiceDesk(applicationUser, organizationServiceDeskUpdateParameters);
    }
    return customerOrganization;
}