
Integer totalTimeSpent = new Integer(0);
LinkedList<String> outwardIssueKeys = get("rest/api/latest/issue/${issue.key}").header("Content-Type", "application/json").asObject(Map).body.fields.issuelinks*.outwardIssue*.key;

outwardIssueKeys.each{ String outwardIssueKey ->

    get("rest/api/latest/issue/${outwardIssueKey}").header("Content-Type", "application/json").asObject(Map).body.fields.worklog.worklogs*.timeSpentSeconds.each{ Integer timeSpend ->

        totalTimeSpent += timeSpend;
    };
}

return "${totalTimeSpent / 60 / 60}h";
