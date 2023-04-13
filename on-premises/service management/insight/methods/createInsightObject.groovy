import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.objects.ObjectType
import com.riadalabs.jira.plugins.insight.channel.external.api.model.ObjectBean

ObjectBean createInsightObject(String objectName, String objectType) {
    ObjectFacade objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ObjectFacade.class)
    ObjectType type = objectFacade.loadObjectTypeByName(objectType)
    
    if (type == null) {
        throw new IllegalArgumentException("Invalid object type: " + objectType)
    }
    
    ObjectBean objectBean = new ObjectBean()
    objectBean.setObjectName(objectName)
    objectBean.setObjectType(type)
    
    objectFacade.createObjectBean(objectBean)
}
