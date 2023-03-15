
/*
  This method can be implemented in your groovy Jira scripts.
  When calling this method simply provide the key of the relevant Insight object.
  This method will return the object.
*/

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.atlassian.jira.component.ComponentAccessor;
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade;

private ObjectBean getObjectBean(String objectKey){
 
    @WithPlugin('com.riadalabs.jira.plugins.insight')
    @PluginModule ObjectFacade objectFacade ;
 
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.getObjectBean");
    logger.setLevel(Level.DEBUG);
 
    Class objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade");
    
    objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass);
 
    ObjectBean objectBean = objectFacade.loadObjectBean(objectKey);
    assert objectBean;
    return objectBean;
}