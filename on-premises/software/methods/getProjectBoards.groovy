import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.board.Board;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.board.BoardManager;

private LinkedList<HashMap> getProjectBoards(){

    BoardManager boardManager = ComponentAccessor.getComponent(BoardManager);
    ProjectManager projectManager = ComponentAccessor.getProjectManager();

    LinkedList<HashMap> result = new LinkedList();
    LinkedList<Project> projectList = projectManager.getProjectObjects() as LinkedList;
    for(Project project : projectList){

        if(boardManager.hasBoardForProject(project.getId())){
            List<Board> projectBoards = boardManager.getBoardsForProject(project.getId());
            if (projectBoards.size() > 0){

                for (Board projectBoard : projectBoards){

                    result.add(["name": projectBoard.toString(), "id": projectBoard.getId(), "jql": projectBoard.getJql(), "self": "/secure/RapidBoard.jspa?rapidView=" + projectBoard.getId(),  "project": project.getName()]);
                }
            }
        }
    }
    return ["no of results" : results.size(), "results": result];
}