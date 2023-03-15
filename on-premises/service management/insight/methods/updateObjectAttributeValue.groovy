
/*
  This method can be implemented in your groovy Jira scripts.
  When calling this method provide the key of the relevant Insight object, the relevant attribute name and a string attribute value to be associated with the insight object's attribute.
  The method will return the object attribute bean containing the object attribute and it's new value.
*/

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.ofbiz.core.util.ObjectType;
import com.atlassian.jira.component.ComponentAccessor;
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import com.onresolve.scriptrunner.runner.customisers.PluginModule;
import com.riadalabs.jira.plugins.insight.services.model.ObjectAttributeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeAttributeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade;
import com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory;
import com.riadalabs.jira.plugins.insight.services.model.MutableObjectAttributeBean;

private ObjectAttributeBean updateObjectAttributeValue(String objectKey, String attributeName, String attributeValue){
 
    @WithPlugin('com.riadalabs.jira.plugins.insight')
    @PluginModule ObjectFacade objectFacade ;
    @PluginModule ObjectTypeFacade objectTypeFacade;
    @PluginModule ObjectTypeAttributeFacade objectTypeAttributeFacade;
    @PluginModule ObjectAttributeBeanFactory objectAttributeBeanFactory;
 
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.updateObjectAttributeValue");
    logger.setLevel(Level.DEBUG);
 
    Class objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade");
    Class objectTypeFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade");
    Class objectTypeAttributeFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade");
    Class objectAttributeBeanFactoryClass = ComponentAccessor.getPluginAccessor().getClassLoader().loadClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory");
     
    objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass);
    objectTypeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectTypeFacadeClass);
    objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectTypeAttributeFacadeClass);
    objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(objectAttributeBeanFactoryClass);
 
    ObjectBean objectBean = objectFacade.loadObjectBean(objectKey);
    ObjectTypeBean objectTypeBean = objectTypeFacade.loadObjectType(objectBean.objectTypeId);
    ArrayList<ObjectTypeAttributeBean> objectTypeAttributeBeanList = objectTypeAttributeFacade.findObjectTypeAttributeBeans(objectTypeBean.getId()) as ArrayList<ObjectTypeAttributeBean>;
    ObjectTypeAttributeBean objectTypeAttributeBean = objectTypeAttributeBeanList.find{ ObjectTypeAttributeBean objectTypeAttributeBean -> objectTypeAttributeBean.getName().toString().equalsIgnoreCase(attributeName) };
    if (!objectTypeAttributeBean) {
         
        logger.error("No ObjectTypeAttributeBean objectTypeAttributeBean found for String attributeName ${attributeName}! Please check, if the attribute exists in Insight!")
        return;
    } else {
         
        MutableObjectAttributeBean newObjectTypeAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(objectBean, objectTypeAttributeBean, attributeValue);
        newObjectTypeAttributeBean.setId(objectTypeAttributeBean.getId());
        return objectFacade.storeObjectAttributeBean(newObjectTypeAttributeBean);
    }
}