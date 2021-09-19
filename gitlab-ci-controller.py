#!/usr/bin/python3 -u
'''
This script tries to guarantee that just one pipeline at a time will be working,
and cancel redundant pipelines.

Logic:
  If there are more running pipelines: await.
  While waiting, if this pipeline is not the last one created,
  cancel itself.

How to use:
- Copy this code to your repository or used docker image.
- Run it in your .gitlab-ci.yml file, possibly in `before_script`.

Requirements:
- A key for the Gitlab API, with at least maintainer role, must be available and
  configured as ENV var 'GITLAB_API_TOKEN'.
- Python 3.

Limitations:
- If too many pipelines are created at the same time, it may take some time to
  clear the intermediary ones (it takes about 10s for a pipeline to cancel itself).
- If there is a fast continuous creation of pipelines (faster than they can
  cancel themselves) no pipeline will be able to execute.
- This code wasn't tested with multiple branches. Currently it should consider
  pipelines in different branches as redundant and cancel them, what doesn't
  seem right for most cases.
'''

import os
import time
import json
from urllib.request import Request, urlopen

# Env vars
envs = {
    'project_id': 'CI_PROJECT_ID',
    'this_pipeline_id': 'CI_PIPELINE_ID',
    'api_token': 'GITLAB_API_TOKEN'
}

def load_env_vars():
    '''Get env vars and check their existence.'''
    fail = False
    for key, env_var_name in envs.items():
        env_var_value = os.environ.get(env_var_name)
        if not env_var_value:
            print('Error: %s not set.' % env_var_name)
            fail = True
        envs[key] = env_var_value
    if fail:
        exit(1)


def get_json(url: str, post=False):
    '''Get JSON data using API token.'''
    req = Request(url)
    req.add_header('PRIVATE-TOKEN', envs['api_token'])
    content = urlopen(req, data=bytes() if post else None).read()
    return json.loads(content.decode('utf-8'))


def list_all_pipelines() -> list:
    '''List pipelines for this project.'''
    return get_json(base_url + '?per_page=100')


def list_running_pipelines() -> list:
    '''List running pipelines for this project.'''
    return get_json(base_url + '?per_page=100&status=running')


def cancel_pipeline(pipeline_id: str) -> dict:
    '''Cancel a pipeline based on its id.'''
    return get_json(base_url + pipeline_id + '/cancel', post=True)


def i_am_the_last_pipeline() -> bool:
    '''Return True if this is the last created pipeline for this project.'''
    pipelines = list_all_pipelines()
    if str(pipelines[0]['id']) != envs['this_pipeline_id']:
        print('I am not the last =(')
        print('%s is.' % pipelines[0]['id'])
        return False
    else:
        print('I am the last! =D')
        return True


def is_the_last_or_cancel() -> bool:
    '''Keep this pipeline alive if it's the last one, otherwise cancel itself.'''
    if i_am_the_last_pipeline():
        return True
    else:
        # Tries to cancel itself
        cancel_pipeline(envs['this_pipeline_id'])
        print('Waiting my own death...')
        time.sleep(100)
        # If failed to cancel itself, return error and make this pipeline fail instead
        print('No death? Error!')
        exit(1)


def other_pipelines_running() -> bool:
    '''Return True if other pipelines are running.'''
    return len(list_running_pipelines()) > 1


if __name__ == "__main__":
    load_env_vars()

    # Gitlab API base URL
    base_url = 'https://cygit.eu/api/v4/projects/%s/pipelines/' % envs['project_id']

    while is_the_last_or_cancel() and other_pipelines_running():
        print('Waiting my turn...')
        time.sleep(2)
    print('Time to work!')
