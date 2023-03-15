import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;

private void removeGroupsFromProjectRoleActors(Project projectObject, LinkedHashMap<ProjectRole, List<Group>> roleActorMap){
    ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
    SimpleErrorCollection errorCollection = new SimpleErrorCollection();
    Collection<String> groupNameCollection;
    for (ProjectRole projectRole : roleActorMap.keySet()) {
         
        roleActorMap.get(projectRole).each { Group groupObject -> groupNameCollection.add(groupObject.getName()) }
        projectRoleService.removeActorsFromProjectRole(groupNameCollection, projectRole, projectObject, ProjectRoleActor.GROUP_ROLE_ACTOR_TYPE , errorCollection);
    }
}