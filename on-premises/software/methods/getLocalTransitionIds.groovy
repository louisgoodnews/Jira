/*
  The following should be noted when using the methods:

      * private HashMap<String, Object> getLocalTransitionIds (Issue issueObject) → this method needs only one Jira issue and will get ALL statuses and their transitions out for the issue's workflow in the form of a HashMap<String, Object>.
      * private HashMap<String, Object> getLocalTransitionIds (Issue issueObject, String targetStatusName) → this method expects a Jira issue and a target status name. The method then returns ALL transitions to this target status
      * private HashMap<String, Object> getLocalTransitionIds (Issue issueObject, Integer targetStatusId) → does the same as method #2
      * private HashMap<String, Object> getLocalTransitionIds (Issue issueObject, String targetStatusName) → this method works like method #2, but only the Ids of the transitions are issued here
      * private Boolean isGlobalTransition(Issue issueObject, ActionDescriptor actionDescriptor) → this method expects a Jira issue and an ActionDescriptor and returns a Boolean that tells if the passed ActionDescriptor is compatible with all statuses

  It is explicitly warned not to use ALL methods in the same script! With the exception of method #4, all methods must additionally be used with the method isGlobalTransition(Issue issueObject, ActionDescriptor actionDescriptor)!
*/

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.Issue;
 
private HashMap<String, Object> getLocalTransitionIds (Issue issueObject) {
 
    HashMap<String, Object> result = new HashMap();
    JiraWorkflow issueWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueObject);
    issueWorkflow.getLinkedStatusObjects().each { Status status ->
 
        StepDescriptor stepDescriptor = issueWorkflow.getLinkedStep(status);
        List<ActionDescriptor> actionDescriptors = stepDescriptor.getActions();
        actionDescriptors.each { ActionDescriptor actionDescriptor ->
 
            result.put(status.getName(), [actionDescriptor.getName(), ["id": actionDescriptor.getId(), "isGlobal": isGlobalTransition(issueObject, actionDescriptor)]]);
        }
    };
    if (!result) {
  
        return null;
    } else {
  
        return result;
    }
}
 
private HashMap<String, Object> getLocalTransitionIds (Issue issueObject, String targetStatusName) {
 
    HashMap<String, Object> result = new HashMap();
    JiraWorkflow issueWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueObject);
    Status statusObject = ComponentAccessor.getComponent(StatusManager).getStatuses().find { Status status -> status.getName().equalsIgnoreCase(targetStatusName) };
    StepDescriptor stepDescriptor = issueWorkflow.getLinkedStep(statusObject);
    List<ActionDescriptor> actionDescriptors = stepDescriptor.getActions();
    actionDescriptors.each { ActionDescriptor actionDescriptor ->
 
        result.put(actionDescriptor.getName(), ["id": actionDescriptor.getId(), "isGlobal": isGlobalTransition(issueObject, actionDescriptor)]);
    }
    if (!result) {
  
        return null;
    } else {
  
        return result;
    }
}
 
private HashMap<String, Object> getLocalTransitionIds (Issue issueObject, Integer targetStatusId) {
 
    HashMap<String, Object> result = new HashMap();
    JiraWorkflow issueWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueObject);
    Status statusObject = ComponentAccessor.getComponent(StatusManager).getStatuses().find { Status status -> status.getId() == targetStatusId };
    StepDescriptor stepDescriptor = issueWorkflow.getLinkedStep(statusObject);
    List<ActionDescriptor> actionDescriptors = stepDescriptor.getActions();
    actionDescriptors.each { ActionDescriptor actionDescriptor ->
 
        result.put(actionDescriptor.getName(), ["id": actionDescriptor.getId(), "isGlobal": isGlobalTransition(issueObject, actionDescriptor)]);
    }
    if (!result) {
  
        return null;
    } else {
  
        return result;
    }
}
 
private Boolean isGlobalTransition(Issue issueObject, ActionDescriptor actionDescriptor){
    Boolean result = false;
    Integer checker = 0;
    JiraWorkflow issueWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueObject);
    issueWorkflow.getLinkedStatusObjects().each{ Status status ->
 
        issueWorkflow.getLinkedStep(status).getActions().find{ Object workflowActionDescriptor ->
 
            if (workflowActionDescriptor.name.toString().equalsIgnoreCase(actionDescriptor.getName())) {
 
                checker = checker += 1;
            }
        }
    }
    if (checker == issueWorkflow.getLinkedStatusObjects().size()){
 
        return true;
    } else {
 
        return false;
    }
}