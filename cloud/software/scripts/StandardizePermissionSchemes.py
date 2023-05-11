import requests
import base64
import json
import logging

# Set up logging
logger = logging.getLogger(
    "louisgoodnews.jira.cloud.software.scripts.standardizePermissionSchemes")
logger.setLevel(logging.DEBUG)

jira_domain = "<YOUR JIRA DOMAIN>"  # YOUR JIRA DOMAIN

# Encode email and password to base64
auth = base64.b64encode("<YOUR EMAIL ADDRESS : YOUR API TOKEN>".encode(
    "utf-8")).decode("utf-8")  # YOUR EMAIL ADDRESS : YOUR API TOKEN

# Send GET request to retrieve project roles
project_role_response = requests.get(f"https://{jira_domain}.atlassian.net/rest/api/latest/role",
                                     headers={"Accept": "application/json",
                                              "Authorization": f"Basic {auth}"}).json()

# Check if the response status is not 200
if project_role_response["status-code"] != 200:
    # Log error and return error messages
    logger.error(
        f"GET 'project_role_response' failed with 'status-code' {project_role_response['status-code']} 'message' {project_role_response['message']}!")
    raise Exception(project_role_response["message"])

# Define the permission scheme reference
permission_scheme_reference = {
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
}

create_permissionscheme_request_body = {
    "name": "",
    "description": "",
    "permissions": [

    ]
}

for permission_key in permission_scheme_reference.keys():
    filtered_roles = [project_role for project_role in project_role_response["body"]["values"]
                      if permission_scheme_reference[permission_key].contains(project_role["name"])]
    for filtered_role in filtered_roles:
        create_permissionscheme_request_body["permissions"].append({
            "holder": {
                "parameter": filtered_role["name"],
                "type": "projectRole",
                "value": filtered_role["id"]
            },
            "permission": permission_key
        })

# Send POST request to create the permission scheme
permission_scheme_response = requests.get(f"https://{jira_domain}.atlassian.net/rest/api/latest/role",
                                          headers={"Accept": "application/json",
                                                   "Content-Type": "application/json",
                                                   "Authorization": f"Basic {auth}"},
                                          payload=permission_scheme_reference).json()

# Check if the response status is not 200
if permission_scheme_response["status-code"] != 200:
    # Log error and return error messages
    logger.error(
        f"POST 'permission_scheme_response' failed with 'status-code' {permission_scheme_response['status-code']} 'message' {permission_scheme_response['message']}!")
    raise Exception(permission_scheme_response["message"])
