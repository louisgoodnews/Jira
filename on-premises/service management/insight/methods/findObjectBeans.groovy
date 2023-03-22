
/*
  This script can be implemented in your groovy Jira scripts. With this method you can execute an IQL search and fetch the results.
*/

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import com.atlassian.jira.component.ComponentAccessor;
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade
 
private ArrayList<ObjectBean> findObjectBeans(String queryString){
  
    @WithPlugin('com.riadalabs.jira.plugins.insight')
    @PluginModule ObjectFacade objectFacade ;
    @PluginModule IQLFacade iqlFacade;
  
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.findObjectBeans");
    logger.setLevel(Level.DEBUG);
  
    Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade");
    Class objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade");
      
    objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass);
    iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass);
 
    return iqlFacade.findObjectsByIQL(queryString) as ArrayList<ObjectBean>;
}