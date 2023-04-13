
//MADE BY LOUIS GOODNEWS

/*
DESCRIPTION:
This script will fetch data from the current issue and then post the data to MS Graph to schedule a MS Outlook appointment.
*/

//IMPORT:
import org.apache.log4j.Logger
import org.apache.log4j.Level

//LOGGER:
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.createOutlookEvent")
logger.setLevel(Level.DEBUG)

//DECLARATION:
//HashMap<Object, String> issue = [key: "enter issue key here"]
HttpResponse issueObject = get("rest/api/latest/issue/${issue.key}").header("Accept", "application/json").asObject(Map)

//TASK(S):
try{
    //-> schedule appointment via REST call:
    HttpResponse scheduleAppointment = post("https://graph.microsoft.com/v1.0/me/events")
    .header("Authorization", "")
    .header("Content-Type", "application/json")
    .header("Prefer", 'outlook.timezone=\"Pacific Standard Time\"')
    .body(
        [
            "subject": """Appointment for ${issueObject.body.key} - ${issueObject.body.fields.summary}""",
            "body":[
                "contentType": "HTML",
                "content": """${issueObject.body.fields.comment.comments.last().body}""" //-> fetch last comment on issues's body
            ],
            "start": [
                "dateTime": """${issueObject.body.fields.customfield_10169.replace('+0000', 'Z')}""", //-> custom field "Startzeit"
                "timeZone": "UTC"
            ],
            "end": [
                "dateTime": """${issueObject.body.fields.customfield_10170.replace('+0000', 'Z')}""", //-> custom field "Endzeit"
                "timeZone": "UTC"
            ],
            "allowNewTimeProposals": true
        ]
    )
    .asJson()
    if(!scheduleAppointment.status.toString().equals("201")){
        logger.error("""scheduleAppointment resulted: ${scheduleAppointment.status} | ${scheduleAppointment.body}""")
    }
}catch(Exception e){
    logger.error("""The task failed due to: ${e}""")
}

//RETURN:
return "done!"
