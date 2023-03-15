import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

private void addUserToProject(String projectRoleName, Project project, ApplicationUser applicationUser){ 

    Logger logger = Logger.getLogger("jira.on-premises.console.methods.addUserToProject");
    logger.setLevel(Level.DEBUG);   

    ProjectRoleService projectRoleService = ComponentAccessor.getComponent(ProjectRoleService);
    LinkedHashMap<ProjectRole, ApplicationUser> roleActorMap = new LinkedHashMap<ProjectRole, ApplicationUser>();
    Collection<ProjectRole> projectRoleCollection = projectRoleService.getProjectRoles(new SimpleErrorCollection());
    try{

        LinkedList<ProjectRole> projectRoleList = projectRoleCollection.findAll{    ProjectRole projectRole ->
                
            projectRoleService.getProjectRoleActors(projectRole, project, new SimpleErrorCollection()).getRoleActors().findAll{    RoleActor roleActor ->

                !roleActor.contains(applicationUser);
            };
        } as LinkedList<ProjectRole>;
        roleActorMap.put(projectRoleList.find{   ProjectRole projectRole ->

            projectRole.getName().equalsIgnoreCase(projectRoleName);
        }, applicationUser);
        roleActorMap.keySet().each {    ProjectRole projectRole ->

            projectRoleService.addActorsToProjectRole([applicationUser.getUsername()] as Collection<String>, projectRole, project, ProjectRoleActor.USER_ROLE_ACTOR_TYPE, new SimpleErrorCollection());
        };
    }catch(Exception e){

        logger.error("Executing method 'addUserToProject' with params project ${project.getName()} and aplicationUser ${applicationUser.getDisplayName()} and project role ${projectRoleName} caught exception ${e}");
    };
}