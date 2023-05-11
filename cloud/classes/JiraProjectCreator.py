import requests
import json

class JiraProjectCreator:
    def __init__(self, base_url, username, api_token):
        self.base_url = base_url
        self.username = username
        self.api_token = api_token
        self.headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Basic {self._get_auth_token()}'
        }

    def _get_auth_token(self):
        return f'{self.username}:{self.api_token}'.encode('utf-8').b64encode().decode('utf-8')

    def _send_request(self, method, endpoint, data=None):
        url = f'{self.base_url}{endpoint}'
        response = requests.request(method, url, headers=self.headers, json=data)
        response.raise_for_status()
        return response.json()

    def create_project(self, project_key, project_name, project_lead, template_key='com.pyxis.greenhopper.jira:gh-scrum-template'):
        # Create project payload
        payload = {
            'key': project_key,
            'name': project_name,
            'projectTypeKey': 'software',
            'lead': project_lead,
            'templateKey': template_key,
            'projectTemplateWebItemKey': 'com.pyxis.greenhopper.jira:gh-scrum-template-web-item',
            'description': 'Scrum project created using Jira API',
        }

        # Send request to create project
        endpoint = '/rest/api/latest/project'
        response = self._send_request('POST', endpoint, data=payload)
        project_id = response['id']

        return project_id

    def create_board(self, project_id, board_name):
        # Create board payload
        payload = {
            'name': board_name,
            'type': 'scrum',
            'projectIds': [project_id]
        }

        # Send request to create board
        endpoint = '/rest/agile/latest/board'
        response = self._send_request('POST', endpoint, data=payload)
        board_id = response['id']

        return board_id

    def configure_workflow_scheme(self, project_id, workflow_scheme_id):
        # Get the project's workflow configuration
        endpoint = f'/rest/api/latest/project/{project_id}/workflowScheme'
        response = self._send_request('GET', endpoint)
        current_workflow_scheme_id = response['id']

        # Update the project's workflow scheme
        endpoint = f'/rest/api/latest/project/{project_id}/workflowScheme'
        payload = {'id': current_workflow_scheme_id, 'workflowScheme': {'id': workflow_scheme_id}}
        self._send_request('PUT', endpoint, data=payload)

    def configure_field_configuration_scheme(self, project_id, field_configuration_scheme_id):
        # Get the project's field configuration
        endpoint = f'/rest/api/latest/project/{project_id}/configuration'
        response = self._send_request('GET', endpoint)
        current_field_configuration_scheme_id = response['id']

        # Update the project's field configuration scheme
        endpoint = f'/rest/api/latest/project/{project_id}/configuration'
        payload = {'id': current_field_configuration_scheme_id, 'issueTypeScreenScheme': {'id': field_configuration_scheme_id}}
        self._send_request('PUT', endpoint, data=payload)

    def assign_permission_scheme(self, project_id, permission_scheme_id):
        # Get the project's permission scheme
        endpoint = f'/rest/api/latest/project/{project_id}/permissionscheme'        
        response = self._send_request('GET', endpoint)
        current_permission_scheme_id = response['id']

        # Update the project's permission scheme
        endpoint = f'/rest/api/latest/project/{project_id}/permissionscheme'
        payload = {'id': current_permission_scheme_id}
        self._send_request('PUT', endpoint, data=payload)
