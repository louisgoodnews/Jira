
//MADE BY LOUIS GOODNEWS

/*
DESCRIPTION:
This script will fetch ALL boards in the instance and store the relevant info in LinkedHashMap format.
*/

//IMPORT:
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

//LOGGER:
Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.getAllBoards");
logger.setLevel(Level.INFO);

//INITIALISATION:
LinkedHashMap<String, Integer> resultMap = new LinkedHashMap();

//DECLARATION:
HttpResponse<JsonNode> getAllBoards = get("/rest/agile/1.0/board").asJson();

//TASK(S):
try{
    //TASK DESCRIPTION: add the returned board info to resultMap
    for(Integer i = 0; i < (Integer) getAllBoards.body.array.getAt(0).values.size(); i++){;
        resultMap.put(getAllBoards.body.array.getAt(0).values.getAt(i).name, getAllBoards.body.array.getAt(0).values.getAt(i).id);
    }
}catch(Exception e){
    logger.error("The task failed due to: ", e);
}

//LOGGER LOGGING STUFF:
logger.info("Boards found: " + resultMap.size());

//RETURN:
return resultMap;