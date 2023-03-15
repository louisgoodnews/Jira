import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.permission.PermissionScheme;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
  
/*
    PLEASE BE ADVISED THAT THIS SCRIPT MIGHT WRECK YOUR INSTANCE!
  
    DESCRIPTION: (Please read carefully!)
  
    Depending on your choice with the booleans in class CONSTANTS this script will update permission scheme and project role actor association for projects.
    In the following, I will explain the different boolean variables in the CONSTANS class. Please read carefully BEFORE editing any boolean value(s)!
  
    MODIFY_ALL_PROJECTSMODIFY_ALL_PROJECTS -> if set to true, ALL projects of your Jira instance will be updated in accordance to further information provided in CONSTANTS
    ADD_SINGLE_USERS_TO_PROJECT_ROLES -> if set to true, for the given scope of projects through MODIFY_ALL_PROJECTSMODIFY_ALL_PROJECTS, the script will associate any given user in the PROJECT_ROLE_ACTORS map to the associated project role.
      
    For example:
  
    PROJECT_ROLE_ACTORS = [
        "Administrators": ["", "", ""],
        "Developers": ["", "", ""],
        "Members": ["", "", ""]
    ]
  
    The users that are contained in the project roles' value will be looked up and if found to be existing associated to the key project role (if existing of course) for projects.
      
    ADD_SINGLE_GROUPS_TO_PROJECT_ROLES -> if set to true, works the same way ADD_SINGLE_USERS_TO_PROJECT_ROLES does.
    UPDATE_PROJECT_PERMISSION_SCHEME -> if set to true, the script will associate the permission scheme with id from ID_PERMISSION_SCHEME with projects.
    REMOVE_PREVIOUS_ACTORS -> if set to true, the script will attempt to remove ALL current project role actors from a project BEFORE adding new ones. In this case the executing user will be added as administrator to EVERY project
    ADD_ADMIN_USERS -> if set to true, script will attempt to find ALL admin users and add them as administrators to a given project
     
    Regarding the other parameters in CONSTANTS, please read the follwoing:
  
    ID_PERMISSION_SCHEME -> id of the permission scheme to be associated with a project.
    PROJECT_ROLE_ACTORS -> contains the relevant information for script operation regarding ADD_SINGLE_USERS_TO_PROJECT_ROLES. Please read information provided regarding ADD_SINGLE_USERS_TO_PROJECT_ROLES.
    PROJECT_KEY -> provide a single string project key, if you want script operation to be carried out for a specific project
    PROJECT_KEY_LIST -> provide string project keys, if you want script operation to be carried out for a specific set of projects
  
    WHAT COULD GO WRONG?
  
    I'm glad you asked.
      
    What could go wrong might be that all users are being locked out of the projects due to no longer being associated to any relevant project role.
    Another classic occurrence is that due to the previous permission scheme not being removed from tha project, a project is associated to multiple permission schemes, brekaing the project's permission association.
  
*/
  
class CONSTANTS{
  
    //These boolean values will determine the operations the script will execute
    static final Boolean MODIFY_ALL_PROJECTS = false;
    static final Boolean ADD_SINGLE_USERS_TO_PROJECT_ROLES = false;
    static final Boolean ADD_SINGLE_GROUPS_TO_PROJECT_ROLES = false;
    static final Boolean UPDATE_PROJECT_PERMISSION_SCHEME = false;
    static final Boolean REMOVE_PREVIOUS_ACTORS = false;
    static final Boolean ADD_ADMIN_USERS = false;
  
    //Provide additionally needed information here
    static final Long ID_PERMISSION_SCHEME = null;
    static final HashMap<String, List<String>> PROJECT_ROLE_ACTORS = [
        //-> List should be {projectRoleName}: [{Username1 or GroupName1}, {Username2 or GroupName2}, {Username3 or GroupName3}, etc.]
        null: [null]
    ]
    static final String PROJECT_KEY = null;
    static final List<String> PROJECT_KEY_LIST = [null];
}
 
Boolean doLog = false;
List<String> userNameList = [];
List<String> groupNameList = [];
List<Group> roleActorGroupList = [];
ApplicationUser roleActorUser = null;
List<ApplicationUser> usersFoundList = [];
Collection<String> userNameCollection = [];
List<ApplicationUser> roleActorUserList = [];
PermissionScheme permissionSchemeToSet = null;
Collection<ProjectRole> projectRoleObjectCollection = [];
LinkedList<Project> projectObjectList = new LinkedList();
LinkedHashMap<ProjectRole, List<Group>> groupRoleActorMap = new LinkedHashMap();
LinkedHashMap<ProjectRole, List<ApplicationUser>> userRoleActorMap = new LinkedHashMap();
  
Logger logger = Logger.getLogger("de.louis.scriptrunner.console.updateProjectRoleActors");
logger.setLevel(Level.DEBUG);
 
UserManager userManager = ComponentAccessor.getUserManager();
GroupManager groupManager = ComponentAccessor.getGroupManager();
SimpleErrorCollection errorCollection = new SimpleErrorCollection();
ProjectManager projectManager = ComponentAccessor.getProjectManager();
List<ApplicationUser> adminUserList = findAllApplicationUsers("admin");
ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getComponentOfType(PermissionSchemeManager.class);
  
//preliminary check. If NO mode i.e., boolean from CONSTANTS is true, the script will abort execution.
try {
  
    if (!CONSTANTS.MODIFY_ALL_PROJECTS && !CONSTANTS.ADD_SINGLE_USERS_TO_PROJECT_ROLES && !CONSTANTS.ADD_SINGLE_GROUPS_TO_PROJECT_ROLES && !CONSTANTS.UPDATE_PROJECT_PERMISSION_SCHEME && !CONSTANTS.REMOVE_PREVIOUS_ACTORS && !CONSTANTS.ADD_ADMIN_USERS) {
          
        logger.error("No valid modes were selected. Script execution aborted!");
        return "ERROR - no valid modes were selected!";
    } else {
  
        if (CONSTANTS.REMOVE_PREVIOUS_ACTORS) {
            if (!CONSTANTS.ADD_SINGLE_USERS_TO_PROJECT_ROLES && !CONSTANTS.ADD_SINGLE_GROUPS_TO_PROJECT_ROLES) {
                 
                logger.warn("Current selection to remove ALL project role actors without enabling adding new ones is DANGEROUS. Script execution aborted!");
                return "WARN - current selection to remove ALL project role actors without enabling adding new ones is DANGEROUS!";
            } else if (CONSTANTS.PROJECT_ROLE_ACTORS.values().size()) {
                projectRoleObjectCollection = projectRoleService.getProjectRoles(errorCollection);
  
                if (CONSTANTS.MODIFY_ALL_PROJECTS) {
  
                    projectObjectList = projectManager.getProjectObjects() as LinkedList<Project>;
                    for (Project projectObject : projectObjectList) {
                         
                        for (ProjectRole projectRoleObject : projectRoleObjectCollection) {
                             
                            if (CONSTANTS.REMOVE_PREVIOUS_ACTORS) {
                                if (projectRoleObject.getName().toLowerCase().contains("admin")) {
 
                                    projectRoleService.addActorsToProjectRole([currentUser.getUsername()] as Collection, projectRoleObject, projectObject, ProjectRoleActor.USER_ROLE_ACTOR_TYPE , new SimpleErrorCollection());                               
                                }
                             
                                Collection<ApplicationUser> applicationUserCollection = projectRoleService.getProjectRoleActors(projectRoleObject, projectObject, new SimpleErrorCollection()).getApplicationUsers() as Collection<ApplicationUser>;
                                applicationUserCollection.each { ApplicationUser applicationUser ->
                                     
                                    if (!applicationUser.getKey().equalsIgnoreCase(currentUser.getKey())) {
                                         
                                        userNameCollection.add(applicationUser.getKey());
                                    }
                                };
                                projectRoleService.removeActorsFromProjectRole(userNameCollection, projectRoleObject, projectObject, "USER_ROLE_ACTOR_TYPE", new SimpleErrorCollection())
                                if (CONSTANTS.ADD_ADMIN_USERS) {
                                     
                                    if (projectRoleObject.getName().toLowerCase().contains("admin")) {
                                         
                                        updateUserProjectRoleActors(projectObject, [projectRoleObject, adminUserList] as LinkedHashMap<ProjectRole, List<ApplicationUser>>, logger);
                                        logger.info("Added ${adminUserList.size()} admin users to project role ${projectRoleObject.getName()} for project ${projectObject.getName()}.");
                                    }
                                }
                                if (doLog) {
                                         
                                    logger.info("Updated actors ${userNameCollection.size()} for project role ${} in project ${projectObject.getName()}");
                                }
                                userNameCollection.clear();
                            }
                        }
                    }
                } else if (!CONSTANTS.MODIFY_ALL_PROJECTS && !CONSTANTS.PROJECT_KEY && CONSTANTS.PROJECT_KEY_LIST) {
  
                    CONSTANTS.PROJECT_KEY_LIST.each { String projectKey -> projectObjectList.add(projectManager.getProjectObjByKey(projectKey)) }
                    for (Project projectObject : projectObjectList) {
                         
                        for (ProjectRole projectRoleObject : projectRoleObjectCollection) {
                             
                            if (CONSTANTS.REMOVE_PREVIOUS_ACTORS) {
                                if (projectRoleObject.getName().toLowerCase().contains("admin")) {
 
                                    projectRoleService.addActorsToProjectRole([currentUser.getUsername()] as Collection, projectRoleObject, projectObject, ProjectRoleActor.USER_ROLE_ACTOR_TYPE , new SimpleErrorCollection());                               
                                }
                             
                                Collection<ApplicationUser> applicationUserCollection = projectRoleService.getProjectRoleActors(projectRoleObject, projectObject, new SimpleErrorCollection()).getApplicationUsers() as Collection<ApplicationUser>;
                                applicationUserCollection.each { ApplicationUser applicationUser ->
                                     
                                    if (!applicationUser.getKey().equalsIgnoreCase(currentUser.getKey())) {
                                         
                                        userNameCollection.add(applicationUser.getKey());
                                    }
                                };
                                projectRoleService.removeActorsFromProjectRole(userNameCollection, projectRoleObject, projectObject, "USER_ROLE_ACTOR_TYPE", new SimpleErrorCollection())
                                if (CONSTANTS.ADD_ADMIN_USERS) {
                                     
                                    if (projectRoleObject.getName().toLowerCase().contains("admin")) {
                                         
                                        updateUserProjectRoleActors(projectObject, [projectRoleObject, adminUserList] as LinkedHashMap<ProjectRole, List<ApplicationUser>>, logger);
                                        logger.info("Added ${adminUserList.size()} admin users to project role ${projectRoleObject.getName()} for project ${projectObject.getName()}.");
                                    }
                                }
                                if (doLog) {
                                         
                                    logger.info("Updated actors ${userNameCollection.size()} for project role ${} in project ${projectObject.getName()}");
                                }
                                userNameCollection.clear();
                            }
                        }
                    }
                } else if (!CONSTANTS.MODIFY_ALL_PROJECTS && !CONSTANTS.PROJECT_KEY_LIST && CONSTANTS.PROJECT_KEY) {
                      
                    Project projectObject = projectManager.getProjectObjByKey(CONSTANTS.PROJECT_KEY);
                    for (ProjectRole projectRoleObject : projectRoleObjectCollection) {
                         
                        if (CONSTANTS.REMOVE_PREVIOUS_ACTORS) {
                            if (projectRoleObject.getName().toLowerCase().contains("admin")) {
 
                                projectRoleService.addActorsToProjectRole([currentUser.getUsername()] as Collection, projectRoleObject, projectObject, ProjectRoleActor.USER_ROLE_ACTOR_TYPE , new SimpleErrorCollection());                               
                            }
                         
                            Collection<ApplicationUser> applicationUserCollection = projectRoleService.getProjectRoleActors(projectRoleObject, projectObject, new SimpleErrorCollection()).getApplicationUsers() as Collection<ApplicationUser>;
                            applicationUserCollection.each { ApplicationUser applicationUser ->
                                 
                                if (!applicationUser.getKey().equalsIgnoreCase(currentUser.getKey())) {
                                     
                                    userNameCollection.add(applicationUser.getKey());
                                }
                            };
                            projectRoleService.removeActorsFromProjectRole(userNameCollection, projectRoleObject, projectObject, "USER_ROLE_ACTOR_TYPE", new SimpleErrorCollection())
                            if (CONSTANTS.ADD_ADMIN_USERS) {
                                 
                                if (projectRoleObject.getName().toLowerCase().contains("admin")) {
                                     
                                    updateUserProjectRoleActors(projectObject, [projectRoleObject, adminUserList] as LinkedHashMap<ProjectRole, List<ApplicationUser>>, logger);
                                    logger.info("Added ${adminUserList.size()} admin users to project role ${projectRoleObject.getName()} for project ${projectObject.getName()}.");
                                }
                            }
                            if (doLog) {
                                     
                                logger.info("Updated actors ${userNameCollection.size()} for project role ${} in project ${projectObject.getName()}");
                            }
                            userNameCollection.clear();
                        }
                    }
                } else {
                     
                    logger.info("No projects found. Script execution aborted!");
                    return "ERROR - no projects found!";
                }
            } else {
                 
                logger.error("No valid project role actors were provided. Script execution aborted!");
                return "ERROR - no valid project role actors were provided!";
            }
        } else if(doLog) {
             
            logger.info("Mode 'remove previous actors' was NOT selected. New users, if provided will be added to given project(s). Script execution will continue.");
        }
  
        //-> validate if mode for adding groups to project roles is set
        if (CONSTANTS.ADD_SINGLE_GROUPS_TO_PROJECT_ROLES) {
  
            if (CONSTANTS.PROJECT_ROLE_ACTORS.keySet().size()) {
 
                projectRoleObjectCollection = projectRoleService.getProjectRoles(errorCollection)
                for(ProjectRole projectRoleObject : projectRoleObjectCollection){
  
                    if (CONSTANTS.PROJECT_ROLE_ACTORS.keySet().contains(projectRoleObject.getName())) {
                        groupRoleActorMap.put(projectRoleObject, [])
                    }
                }
            } else {
  
                logger.error("Mode 'add single groups to project role' was selected, but no users were provided in role actors map. Script execution aborted!");
                return "ERROR - no sufficient group list provided for mode 'add groups users to project role'!";
            }
  
            if (CONSTANTS.PROJECT_ROLE_ACTORS.values().size()) {
  
                for (List<String> outerValuesList : CONSTANTS.PROJECT_ROLE_ACTORS.values()) {
  
                    for (String groupName : outerValuesList) {
  
                        Group groupObject = groupManager.getGroup(groupName);
                        if (!groupObject) {
  
                            logger.warn("Found no groups for group name '${groupName}'. Script execution aborted!");
                            return "WARN - no groups found for update of project role actors!";
                        } else {
  
                            roleActorGroupList.add(groupObject);
                        }
                    }
                    roleActorGroupList.each { Group listItem -> groupNameList.add(listItem.getName()); }
                    String projectRoleName = getKeyForValue(groupRoleActorMap, userNameList);
                    for (ProjectRole projectRole : groupRoleActorMap.keySet()) {
  
                        if (projectRole.getName().equalsIgnoreCase(projectRoleName)) {
  
                            groupRoleActorMap.put(projectRole, roleActorGroupList);
                        } else {
  
                            logger.warn("${projectRoleName} not found in roleActorMap!");
                        }
                    }
                }
            }
  
            if (CONSTANTS.MODIFY_ALL_PROJECTS) {
 
                projectObjectList = projectManager.getProjectObjects() as LinkedList<Project>;
                for (Project projectObject : projectObjectList) {
  
                    updateGroupProjectRoleActors(projectObject, groupRoleActorMap, logger)
                }
                projectObjectList.clear();
            } else if (!CONSTANTS.MODIFY_ALL_PROJECTS && !CONSTANTS.PROJECT_KEY && CONSTANTS.PROJECT_KEY_LIST) {
 
                CONSTANTS.PROJECT_KEY_LIST.each { String projectKey -> projectObjectList.add(projectManager.getProjectObjByKey(projectKey)) }
                if (projectObjectList.size()) {
 
                    for (Project projectObject : projectObjectList) {
 
                        updateGroupProjectRoleActors(projectObject, groupRoleActorMap, logger)
                    }
                }
                projectObjectList.clear();
            } else if (!CONSTANTS.MODIFY_ALL_PROJECTS && !CONSTANTS.PROJECT_KEY_LIST && CONSTANTS.PROJECT_KEY) {
 
                Project projectObject = projectManager.getProjectObjByKey(CONSTANTS.PROJECT_KEY);
            }
             
        //-> validate if mode for adding single users to project roles is set
        } else  if (CONSTANTS.ADD_SINGLE_USERS_TO_PROJECT_ROLES) {
  
            if (CONSTANTS.PROJECT_ROLE_ACTORS.keySet().size()) {
  
                projectRoleObjectCollection = projectRoleService.getProjectRoles(errorCollection)
                for(ProjectRole projectRoleObject : projectRoleObjectCollection){
  
                    if (CONSTANTS.PROJECT_ROLE_ACTORS.keySet().contains(projectRoleObject.getName())) {
                         
                        userRoleActorMap.put(projectRoleObject, [])
                    }
                }
            } else {
  
                logger.error("Mode 'add single users to project role' was selected, but no users were provided in role actors map. Script execution aborted!");
                return "ERROR - no sufficient user list provided for mode 'add single users to project role'!";
            }
  
            if (CONSTANTS.PROJECT_ROLE_ACTORS.values().size()) {
  
                for (List<String> outerValuesList : CONSTANTS.PROJECT_ROLE_ACTORS.values()) {
  
                    for (String userSearchCriteria : outerValuesList) {
  
                        usersFoundList = findAllApplicationUsers(userSearchCriteria);
                        if (usersFoundList.size() != 1) {
  
                            logger.warn("Found ${usersFoundList.size()} for user search criteria '${userSearchCriteria}': ${usersFoundList}. Script execution aborted!");
                            return "WARN - too many or no users found for update of project role actors!";
                        } else {
  
                            roleActorUserList.add(usersFoundList[0]);
                        }
                    }
                    roleActorUserList.each { ApplicationUser listItem -> userNameList.add(listItem.getUsername()); }
                    String projectRoleName = getKeyForValue(userRoleActorMap, userNameList);
                    for (ProjectRole roleActor : userRoleActorMap.keySet()) {
  
                        if (roleActor.getName().equalsIgnoreCase(projectRoleName)) {
  
                            userRoleActorMap.put(roleActor, roleActorUserList);
                        } else {
  
                            logger.warn("${projectRoleName} not found in userRoleActorMap!");
                        }
                    }
                }
            }
  
            if (CONSTANTS.MODIFY_ALL_PROJECTS) {
  
                projectObjectList = projectManager.getProjectObjects() as LinkedList<Project>;
                for (Project projectObject : projectObjectList) {
  
                    updateUserProjectRoleActors(projectObject, userRoleActorMap, logger)
                }
                projectObjectList.clear();
            } else if (!CONSTANTS.MODIFY_ALL_PROJECTS && !CONSTANTS.PROJECT_KEY && CONSTANTS.PROJECT_KEY_LIST) {
  
                CONSTANTS.PROJECT_KEY_LIST.each { String projectKey -> projectObjectList.add(projectManager.getProjectObjByKey(projectKey)) }
                if (projectObjectList.size()) {
  
                    for (Project projectObject : projectObjectList) {
  
                        updateUserProjectRoleActors(projectObject, userRoleActorMap, logger)
                    }
                }
                projectObjectList.clear();
            } else if (!CONSTANTS.MODIFY_ALL_PROJECTS && !CONSTANTS.PROJECT_KEY_LIST && CONSTANTS.PROJECT_KEY) {
  
                Project projectObject = projectManager.getProjectObjByKey(CONSTANTS.PROJECT_KEY);
            }
  
        } else {
  
            logger.info("No users or groups were added to project roles as neither mode 'add single users to project roles' nor 'add single group to project roles' were not selected. Script execution continues!");
        }
  
        //-> validate if modes and relevant information for update of association of permission schemes with project are set
        if (CONSTANTS.ID_PERMISSION_SCHEME && CONSTANTS.UPDATE_PROJECT_PERMISSION_SCHEME) {
  
            try{
  
                //-> in this case a given project or all projects should be updated to use this particular permission scheme.
                permissionSchemeToSet = permissionSchemeManager.getSchemeObject(CONSTANTS.ID_PERMISSION_SCHEME) as PermissionScheme;
            } catch (Exception e) {
  
                logger.error("Retrieving permission scheme with provided id ${CONSTANTS.ID_PERMISSION_SCHEME} failed with exception: ${e}.")
                return "ERROR - could retrieve permission scheme with the provided id!";
            }
        } else if (!CONSTANTS.UPDATE_PROJECT_PERMISSION_SCHEME) {
  
            logger.warn("Permission scheme id was provided, but mode 'update permission scheme' was not selected. Script execution aborted!");
            return "WARN - permission scheme id was provided, but mode 'update permission scheme' was not selected!";
        } else if (!CONSTANTS.ID_PERMISSION_SCHEME) {
  
            logger.warn("Permission scheme id was not provided, but mode 'update permission scheme' not selected. Script execution aborted!");
            return "WARN - permission scheme id was not provided, but mode 'update permission scheme' was selected!";
        } else {
  
            logger.warn("Permission scheme id was not provided and mode 'update permission scheme' was not selected. Script execution aborted!");
            return "WARN - permission scheme id was not provided and mode 'update permission scheme' were not selected!";
        }
  
        //-> updates permission scheme association to a single, a list of or all projects
        if (permissionSchemeToSet && CONSTANTS.UPDATE_PROJECT_PERMISSION_SCHEME) {
  
            if (CONSTANTS.PROJECT_KEY) {
  
                Project projectObject = projectManager.getProjectObjByKey(CONSTANTS.PROJECT_KEY);
                permissionSchemeManager.removeSchemesFromProject(projectObject)
                permissionSchemeManager.addSchemeToProject(projectObject, permissionSchemeToSet as Scheme);
                logger.info("The permission scheme association for project with key ${CONSTANTS.PROJECT_KEY} was set to ${permissionSchemeToSet.getName()}.");
            } else if (CONSTANTS.PROJECT_KEY_LIST.size()){
  
                for (String projectKey : CONSTANTS.PROJECT_KEY_LIST) {
  
                    Project projectObject = projectManager.getProjectObjByKey(projectKey);
                    permissionSchemeManager.removeSchemesFromProject(projectObject)
                    permissionSchemeManager.addSchemeToProject(projectManager.getProjectObjByKey(projectKey), permissionSchemeToSet as Scheme);
                    logger.info("The permission scheme association for projects with keys ${CONSTANTS.PROJECT_KEY_LIST} were set to ${permissionSchemeToSet.getName()}.");
                }
            }
        } else if (permissionSchemeToSet && CONSTANTS.MODIFY_ALL_PROJECTS && CONSTANTS.UPDATE_PROJECT_PERMISSION_SCHEME) {
  
            projectObjectList = projectManager.getProjectObjects() as LinkedList<Project>;
            for (Project projectObject : projectObjectList) {
  
                permissionSchemeManager.addSchemeToProject(projectObject, permissionSchemeToSet as Scheme);
            }
            projectObjectList.clear();
            logger.info("The permission scheme associations for ALL projects were set to ${permissionSchemeToSet.getName()}.");
        } else {
  
            if (!permissionSchemeToSet) {
  
                logger.error("No permission scheme found when attempting to update permission scheme assocation. Script exectuion aborted!");
                return "ERROR - no permission scheme found when attempting to update permission scheme assocation!";
            } else {
  
                logger.info("No new permission schemes were added to existing projects.");
            }
        }
    }
} catch (Exception e){
  
    logger.warn("The task failed due to an exception!");
    logger.warn(e.printStackTrace());
    return e;
}
  
//METHODS:
private List<ApplicationUser> findAllApplicationUsers(String queryString){
    List<ApplicationUser> result;
    UserSearchService userSearchService = ComponentAccessor.getUserSearchService();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
    result = userSearchService.findUsers(serviceContext, queryString);
    return result;
}
  
private def getKeyForValue(HashMap mapToQuery, def valueToFind){
    def result;
    for (def keyItem : mapToQuery) {
         
        if (mapToQuery.get(keyItem) == valueToFind) {
             
            return keyItem;
        }else{
             
            continue;
        }
    }
}
  
private void updateUserProjectRoleActors(Project projectObject, LinkedHashMap<ProjectRole, List<ApplicationUser>> roleActorMap, Logger logger){
    ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
    SimpleErrorCollection errorCollection = new SimpleErrorCollection();
    Collection<String> userNameCollection;
    for (ProjectRole projectRole : roleActorMap.keySet()) {
         
        roleActorMap.get(projectRole).each { ApplicationUser applicationUser -> userNameCollection.add(applicationUser.getUsername()) }
        projectRoleService.addActorsToProjectRole(userNameCollection, projectRole, projectObject, ProjectRoleActor.USER_ROLE_ACTOR_TYPE , errorCollection);
    }
}
  
private void updateGroupProjectRoleActors(Project projectObject, LinkedHashMap<ProjectRole, List<Group>> roleActorMap, Logger logger){
    ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
    SimpleErrorCollection errorCollection = new SimpleErrorCollection();
    Collection<String> groupNameCollection;
    for (ProjectRole projectRole : roleActorMap.keySet()) {
         
        roleActorMap.get(projectRole).each { Group groupObject -> groupNameCollection.add(groupObject.getName()) }
        projectRoleService.addActorsToProjectRole(groupNameCollection, projectRole, projectObject, ProjectRoleActor.GROUP_ROLE_ACTOR_TYPE , errorCollection);
    }
}