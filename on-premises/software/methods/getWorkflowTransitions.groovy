/**
 * This script fetches all transitions of a Jira workflow and returns a List of maps containing the transition name, 
 * transition id, and if it is global or not. It takes a string parameter 'workflowName' which is the name of the 
 * workflow for which transitions are to be fetched.
 * 
 * To use this script, simply copy and paste it into the Script Console in Jira, then call the method passing 
 * the name of the workflow as a string argument. For example, to get the transitions for a workflow named "My Workflow", 
 * call the method as follows:
 * 
 *   List<Map<String, Object>> transitions = getWorkflowTransitions("My Workflow")
 * 
 * The method returns a List of maps, with each map representing a single transition, and containing the following keys:
 *   - name: the name of the transition
 *   - id: the id of the transition
 *   - isGlobal: a boolean value indicating whether the transition is global or not
 * 
 * If an error occurs while fetching the transitions, an empty list will be returned and the error will be logged to the console.
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.JiraWorkflow
import com.atlassian.jira.workflow.WorkflowManager
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl
import com.atlassian.jira.workflow.TransitionDescriptor
import org.apache.log4j.Logger

List<Map<String, Object>> getWorkflowTransitions(String workflowName) {
    WorkflowManager workflowManager = ComponentAccessor.workflowManager
    JiraWorkflow workflow = workflowManager.getWorkflowByName(workflowName)    
    Logger logger = Logger.getLogger("louisgoodnews.jira.on-premises.software.methods.getWorkflowTransitions")

    try {
        List<TransitionDescriptor> transitions = workflow.getAllActions()
        List<Map<String, Object>> transitionsList = transitions.collect { TransitionDescriptor transition ->
            Map<String, Object> transitionMap = [:]
            boolean isGlobal = WorkflowTransitionUtilImpl.isGlobal(transition)
            transitionMap.put("name", transition.getName())
            transitionMap.put("id", transition.getId())
            transitionMap.put("isGlobal", isGlobal)
            return transitionMap
        }
        return transitionsList
    } catch (Exception e) {
        logger.error("Error fetching transitions for workflow: " + workflowName, e)
        return []
    }
}
