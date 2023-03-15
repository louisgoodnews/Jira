
/*
  This method can be implemented in your groovy Jira scripts.
  When calling this method, provide a comment and an authorised application user object.
  If the comment is marked as a public comment, the method will return a 'false', otherwise a 'true' Boolean.
*/

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.servicedesk.api.comment.ServiceDeskCommentService;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import groovy.json.JsonSlurper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.component.ComponentAccessor;

private Boolean isCommentInternal(Comment comment, ApplicationUser applicationUser){
  
@WithPlugin('com.atlassian.servicedesk')
 
@PluginModule
ServiceDeskCommentService serviceDeskCommentService

    CommentPropertyService commentPropertyService = ComponentAccessor.getComponent(CommentPropertyService);
    EntityProperty commentProperty = commentPropertyService.getProperty(applicationUser, comment.getId(), "sd.public.comment").getEntityProperty().getOrNull();
    if (commentProperty) {
 
        Object propertyValue = new JsonSlurper().parseText(commentProperty.getValue());
        return propertyValue['internal'].asBoolean();
    } else{
         
        return false;
    }
}