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
