import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.Email;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
 
private void sendEmail(String address, String cc, String body, String subject) {
 
    Logger logger = Logger.getLogger("de.louis.scriptrunner.methods.sendEmail");
    logger.setLevel(Level.DEBUG);
 
    MailServerManager mailServerManager = ComponentAccessor.getMailServerManager();
    SMTPMailServer smtpMailServer = mailServerManager.getDefaultSMTPMailServer();
    try{
 
        assert smtpMailServer;
    } catch (AssertionError ae) {
 
        logger.error("Assertion of SMTPMailServer smtpMailServer failed with AssertionError ${ae}.");
    }
    Email email = new Email(address).setCc(cc).setSubject(subject).setBody(body);
    smtpMailServer.send(email);
    return;
}