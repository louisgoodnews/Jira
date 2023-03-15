import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;

private void removeUsersFromProjectRoleActors(Project projectObject, LinkedHashMap<ProjectRole, List<ApplicationUser>> roleActorMap){
    ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
    SimpleErrorCollection errorCollection = new SimpleErrorCollection();
    Collection<String> userNameCollection;
    for (ProjectRole projectRole : roleActorMap.keySet()) {
         
        roleActorMap.get(projectRole).each { ApplicationUser applicationUser -> userNameCollection.add(applicationUser.getUsername()) }
        projectRoleService.removeActorsFromProjectRole(userNameCollection, projectRole, projectObject, ProjectRoleActor.USER_ROLE_ACTOR_TYPE , errorCollection);
    }
}