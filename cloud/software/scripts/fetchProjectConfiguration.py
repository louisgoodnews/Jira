import requests
import json

# Jira Cloud API endpoint and authentication credentials
jira_base_url = ''  # Your Jira instance URL
project_key = ''  # Key of the project you want to fetch
# Base64-encoded username:api_token
authorization = ''

# Helper function to make GET requests to Jira API


def get_request(url):
    """
    Sends a GET request to the specified URL with the provided authentication headers.
    Raises an exception if the request fails.
    """
    headers = {'Authorization': f"Basic {authorization}"}
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()


try:
    # Create a dictionary to hold project data and configuration items
    project_data = {}

    # Fetch project details
    project_details_url = f'{jira_base_url}/rest/api/latest/project/{project_key}'
    project_details = get_request(project_details_url)
    project_data['project_details'] = project_details

    # Fetch issue types and their schemes
    issue_types_url = f'{jira_base_url}/rest/api/latest/issue/createmeta?projectKeys={project_key}&expand=projects.issuetypes.fields'
    issue_types = get_request(issue_types_url)
    project_data['configuration_issue_types'] = issue_types

    # Fetch workflows and their schemes
    workflows_url = f'{jira_base_url}/rest/api/latest/workflow'
    workflows = get_request(workflows_url)
    project_data['configuration_workflows'] = workflows

    # Fetch screens and their schemes
    screens_url = f'{jira_base_url}/rest/api/latest/screens'
    screens = get_request(screens_url)
    project_data['configuration_screens'] = screens

    # Fetch custom fields
    custom_fields_url = f'{jira_base_url}/rest/api/latest/field'
    custom_fields = get_request(custom_fields_url)
    project_data['configuration_custom_fields'] = custom_fields

    # Fetch project roles
    project_roles_url = f'{jira_base_url}/rest/api/latest/project/{project_key}/role'
    project_roles = get_request(project_roles_url)
    project_data['configuration_project_roles'] = project_roles

    # Fetch project role actors
    project_role_actors = {}
    for role_name, role_url in project_roles.items():
        project_role_actors_url = role_url
        role_actors = get_request(project_role_actors_url)
        project_role_actors[role_name] = role_actors

    project_data['configuration_project_role_actors'] = project_role_actors

    # Fetch permission schemes
    permission_schemes_url = f'{jira_base_url}/rest/api/latest/project/{project_key}/permissionscheme'
    permission_schemes = get_request(permission_schemes_url)
    project_data['configuration_permission_schemes'] = permission_schemes

    # Fetch issue security schemes
    issue_security_schemes_url = f'{jira_base_url}/rest/api/latest/issuesecurityschemes'
    issue_security_schemes = get_request(issue_security_schemes_url)
    project_data['configuration_issue_security_schemes'] = issue_security_schemes

    # Fetch notification schemes
    notification_schemes_url = f'{jira_base_url}/rest/api/latest/notificationscheme'
    notification_schemes = get_request(notification_schemes_url)
    project_data['configuration_notification_schemes'] = notification_schemes

    # Convert the project data dictionary to JSON with encoding support
    json_data = json.dumps(project_data, ensure_ascii=False).encode('utf-8')

    # Store the JSON in a file
    with open(f'{project_key}_project_configuration.json', 'w') as file:
        file.write(json_data)

    print(
        f'Project configuration data has been saved to {project_key}_project_configuration.json.')

except requests.exceptions.RequestException as e:
    print(f'Error: {str(e)}')
