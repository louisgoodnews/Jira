import org.apache.log4j.Logger
import org.apache.log4j.Level

Logger logger = Logger.getLogger("louisgoodnews.jira.cloud.software.scripts.standardizePermissionSchemes")
logger.setLevel(Level.DEBUG)

String authorization = "" //-> {YOUR EMAIL ADDRESS: YOU PASSWORD} encoded to base64

HttpResponse projectRoleResponse = get("/rest/api/latest/role")
                                    .header("Accept", "application/json")
                                    .header("Authorization", "Basic ${authorization}")
                                    .asJson()

// Check if the response status is not 200
if (projectRoleResponse.status != 200){

    // Log error and return error messages
    logger.error("GET 'projectRoleResponse' failed with 'status' ${projectRoleResponse.status} 'statusText' ${projectRoleResponse.statusText}!")
    return projectRoleResponse.body.errorMessages
}

LinkedHashMap<String, Object> permissionSchemeReference = [
    "ADMINISTER_PROJECTS": [
        "Project Administrators",
        "Project Managers"
    ],
    "BROWSE_PROJECTS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "MANAGE_SPRINTS_PERMISSION": [
        "Project Managers"
    ],
    "SERVICEDESK_AGENT": [
        "Project Managers",
        "Project Members (internal)"
    ],
    "VIEW_DEV_TOOLS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "VIEW_READONLY_WORKFLOW": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "ASSIGNABLE_USER": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "ASSIGN_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "CLOSE_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "CREATE_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "DELETE_ISSUES": [
        "Project Administrators",
        "Project Managers"
    ],
    "EDIT_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "LINK_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "MODIFY_REPORTER": [
        "Project Managers"
    ],
    "MOVE_ISSUES": [
        "Project Administrators",
        "Project Managers"
    ],
    "RESOLVE_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "SCHEDULE_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "SET_ISSUE_SECURITY": [
        "Project Managers",
        "Project Members (internal)"
    ],
    "TRANSITION_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "MANAGE_WATCHERS": [
        "Project Managers"
    ],
    "VIEW_VOTERS_AND_WATCHERS": [
        "Project Managers"
    ],
    "ADD_COMMENTS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "DELETE_ALL_COMMENTS": [
        "Project Administrators",
        "Project Managers"
    ],
    "DELETE_OWN_COMMENTS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "EDIT_ALL_COMMENTS": [
        "Project Administrators",
        "Project Managers"
    ],
    "EDIT_OWN_COMMENTS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "CREATE_ATTACHMENT": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "DELETE_ALL_ATTACHMENTS": [
        "Project Administrators",
        "Project Managers"
    ],
    "DELETE_OWN_ATTACHMENTS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "DELETE_ALL_WORKLOGS": [
        "Project Administrators",
        "Project Managers"
    ],
    "DELETE_OWN_WORKLOGS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "EDIT_ALL_WORKLOGS": [
        "Project Administrators",
        "Project Managers"
    ],
    "EDIT_OWN_WORKLOGS": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ],
    "WORK_ON_ISSUES": [
        "Project Managers",
        "Project Members (internal)",
        "Project Members (external)"
    ]
]

LinkedHashMap<String, Object> createPermissionSchemeRequestBody = [
    "name": "",
    "description": "",
    "permissions": new LinkedList<Object>()
]

for (permissionKey in permissionSchemeReference.keySet()) {

    List<Object> projectRoleObjectList = projectRoleResponse.body.findAll{ Object projectRole ->

        permissionSchemeReference.get(permissionKey).contains(projectRole.name)
    }
    for(projectRoleObject in projectRoleObjectList){

        createPermissionSchemeRequestBody.get("permissions").append([
            "holder": [
                "parameter": projectRoleObject.name,
                "type": "projectRole",
                "value": projectRoleObject.id
            ],
            "permission": permissionKey
        ])
    }
}

HttpResponse createPermissionSchemeResponse = post("/rest/api/latest/permissionscheme")
                                                .header("Accept", "application/json")
                                                .header("Content-Type", "application/json")
                                                .header("Authorization", "Basic ${authorization}")
                                                .body(createPermissionSchemeRequestBody)
                                                .asJson()

// Check if the response status is not 200
if (createPermissionSchemeResponse.status != 200){

    // Log error and return error messages
    logger.error("GET 'createPermissionSchemeResponse' failed with 'status' ${createPermissionSchemeResponse.status} 'statusText' ${createPermissionSchemeResponse.statusText}!")
    return createPermissionSchemeResponse.body.errorMessages
}
