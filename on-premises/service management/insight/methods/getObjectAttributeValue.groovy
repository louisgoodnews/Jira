
/*
  This method can be implemented in your groovy Jira scripts.
  When calling this method simply provide the key of the relevant Insight object and the relevant attribute name.
*/

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.ofbiz.core.util.ObjectType;
import com.atlassian.jira.component.ComponentAccessor;
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;
import com.riadalabs.jira.plugins.insight.services.model.ObjectAttributeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeAttributeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeBean
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade;
 
private ArrayList<String> getObjectAttributeValue(String objectKey, String attributeName){
 
    @WithPlugin('com.riadalabs.jira.plugins.insight')
    @PluginModule ObjectFacade objectFacade ;
    @PluginModule ObjectTypeFacade objectTypeFacade;
    @PluginModule ObjectTypeAttributeFacade objectTypeAttributeFacade;
 
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.getObjectAttributeValue");
    logger.setLevel(Level.DEBUG);
 
    Class objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade");
    Class objectTypeFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade");
    Class objectTypeAttributeFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade");
     
    objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass);
    objectTypeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectTypeFacadeClass);
    objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectTypeAttributeFacadeClass);
 
    ObjectBean objectBean = objectFacade.loadObjectBean(objectKey);
    ObjectTypeBean objectTypeBean = objectTypeFacade.loadObjectType(objectBean.objectTypeId);
    ArrayList<ObjectTypeAttributeBean> objectTypeAttributeBeanList = objectTypeAttributeFacade.findObjectTypeAttributeBeans(objectTypeBean.getId()) as ArrayList<ObjectTypeAttributeBean>;
    ObjectTypeAttributeBean objectTypeAttributeBean = objectTypeAttributeBeanList.find{ ObjectTypeAttributeBean objectTypeAttributeBean -> objectTypeAttributeBean.getName().toString().equalsIgnoreCase(attributeName) };
     
    if (!objectTypeAttributeBean) {
         
        logger.error("No ObjectTypeAttributeBean objectTypeAttributeBean found for String attributeName ${attributeName}! Please check, if the attribute exists in Insight!")
        return;
    } else {
         
        return objectBean.getObjectAttributeBeans().find { ObjectAttributeBean objectAttributeBean -> objectAttributeBean.getObjectTypeAttributeId() == objectTypeAttributeBean.getId()}.getObjectAttributeValueBeans()*.getValue() as ArrayList<String>;
    }
}