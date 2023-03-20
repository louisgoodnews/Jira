import com.atlassian.jira.project.version.Version
import com.atlassian.jira.task.context.Context
import com.atlassian.jira.project.Project
import com.atlassian.jira.project.version.VersionManager
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.bc.project.component.ProjectComponentManager
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Level
import org.apache.log4j.Logger

private void cleanupProject(String projectKey, Boolean deleteProjectIssues, Boolean deleteProjectVersions, Boolean resetProjectIssueCount, Boolean deleteProjectComponents, Boolean deleteProjectObject){

    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.cleanupProject");
    logger.setLevel(Level.DEBUG);

    ProjectManager projectManager = ComponentAccessor.getProjectManager();
    VersionManager versionManager = ComponentAccessor.getVersionManager();
    ProjectComponentManager projectComponentManager = ComponentAccessor.getProjectComponentManager();

    Project project = projectManager.getProjectObjByKey(projectKey);
    Context context = new Context.Builder().log(logger, "").build();

    try{
	
		if (deleteProjectIssues) {

			projectManager.removeProjectIssues(project, context);
		}

		if (deleteProjectVersions) {

			versionManager.getVersions(project.getId()).each { Version version ->

				versionManager.deleteAndRemoveFromIssues(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(),version);
			}
		}

		if (resetProjectIssueCount) {

			projectManager.setCurrentCounterForProject(project, 0);
		}

		if (deleteProjectComponents) {

			projectComponentManager.deleteAllComponents(project.getId());
		}

		if (deleteProjectObject) {

			projectManager.removeProject(project);
		}
	} catch(Exception e){
	
		logger.error("Executing method 'cleanupProject' for project ${projectKey} with params 'deleteProjectIssues' ${deleteProjectIssues}, 'deleteProjectVersions' ${deleteProjectVersions}, 'resetProjectIssueCount' ${resetProjectIssueCount}, 'deleteProjectComponents' ${deleteProjectComponents}, 'deleteProjectObject' ${deleteProjectObject} caught exception ${e}.");
	}
}