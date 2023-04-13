
/*
  This snippet allows for fetching the changelog for a given issue.
*/

private ArrayList<HashMap<String, Object>> getChangeLog(String issueKey) {
    
    return get("""/rest/api/latest/issue/${issueKey}/changelog""")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .asObject(Map)
            .body
            .values;
 }
