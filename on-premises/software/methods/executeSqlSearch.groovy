import org.apache.log4j.Logger;
import groovy.sql.GroovyResultSet;
import org.apache.log4j.Level;
import java.sql.Connection;
import groovy.sql.Sql;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.DelegatorInterface;
import com.atlassian.jira.component.ComponentAccessor;
import java.lang.StringBuffer;

private LinkedList<GroovyResultSet> executeSqlSearch(String queryString, Boolean doLog){

    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.executeSqlSearch");
    logger.setLevel(Level.INFO);

    LinkedList<GroovyResultSet> result = new LinkedList();
    StringBuffer stringBuffer = new StringBuffer();
    DelegatorInterface delegatorInterface = ComponentAccessor.getComponent(DelegatorInterface);
    String groupHelperName = delegatorInterface.getGroupHelperName("default");
    Connection connection = ConnectionFactory.getConnection(groupHelperName);
    Sql sql = new Sql(connection);
    try {
        sql.eachRow(queryString, { GroovyResultSet groovyResultSet ->
            result.add(groovyResultSet)
            if(doLog){
                logger.info(groovyResultSet)
            }
        })
        sql.close();
    } catch (Exception e){

        logger.error("Execution of method 'executeSqlSearch' caught exception; ${e}")
    }
    return result;
}