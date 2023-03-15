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

private void removeUserFromProject(ApplicationUser applicationUser, Project project){ 

    Logger logger = Logger.getLogger("jira.on-premises.console.methods.removeUserFromProject");
    logger.setLevel(Level.DEBUG);   

    ProjectRoleService projectRoleService = ComponentAccessor.getComponent(ProjectRoleService);
    LinkedHashMap<ProjectRole, ApplicationUser> roleActorMap = new LinkedHashMap<ProjectRole, ApplicationUser>();
    Collection<ProjectRole> projectRoleCollection = projectRoleService.getProjectRoles(new SimpleErrorCollection());
    try{
        LinkedList<ProjectRole> projectRoleList = projectRoleCollection.findAll{    ProjectRole projectRole ->
                
            projectRoleService.getProjectRoleActors(projectRole, project, new SimpleErrorCollection()).getRoleActors().findAll{    RoleActor roleActor ->

                roleActor.contains(applicationUser);
            };
        } as LinkedList<ProjectRole>;
        projectRoleList.each{   ProjectRole projectRole ->

            roleActorMap.put(projectRole, applicationUser);
        }
        roleActorMap.keySet().each {    ProjectRole projectRole ->

            projectRoleService.removeActorsFromProjectRole([roleActorMap.get(projectRole).getUsername()] as Collection<String>, projectRole, project, ProjectRoleActor.USER_ROLE_ACTOR_TYPE, new SimpleErrorCollection());
        }
    }catch(Exception e){

        logger.error("Executing method 'removeUserFromProject' with params project ${project.getName()} and applicationUser ${applicationUser.getDisplayName()} caught exception ${e}");
    }
}