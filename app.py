from bottle import route, run, request, template, static_file
from config import (
    MODEL_PATH, SANDBOX_DIR, STATIC_FILES_PATH, VIEWS_PATH, HOST, PORT, DEBUG
)
from llm import load_language_model, generate_code_with_llm
from code_tools import format_code, lint_code, type_check_code, security_analyze_code
from sandbox import execute_code_in_sandbox
import os

llm_model_instance = load_language_model(MODEL_PATH)

@route('/')
def index():
    return template('index', template_lookup=[VIEWS_PATH])

@route('/static/<filename:path>')
def serve_static(filename):
    return static_file(filename, root=STATIC_FILES_PATH)

@route('/generate_code', method='POST')
def generate_code_route():
    user_prompt = request.forms.get('prompt')
    if not user_prompt:
        return {"error": "No prompt provided."}

    generated_code = generate_code_with_llm(user_prompt, llm_model_instance)
    formatted_code = format_code(generated_code)
    lint_results = lint_code(formatted_code)
    type_check_results = type_check_code(formatted_code)
    security_results = security_analyze_code(formatted_code)

    return {
        "generated_code": generated_code,
        "formatted_code": formatted_code,
        "lint_results": lint_results,
        "type_check_results": type_check_results,
        "security_results": security_results
    }

@route('/execute_code', method='POST')
def execute_code_route():
    code_to_execute = request.forms.get('code')
    if not code_to_execute:
        return {"error": "No code provided for execution."}
    execution_output = execute_code_in_sandbox(code_to_execute)
    return {"execution_output": execution_output}

if __name__ == '__main__':
    if not os.path.exists(SANDBOX_DIR):
        os.makedirs(SANDBOX_DIR)
    if not os.path.exists(VIEWS_PATH):
        os.makedirs(VIEWS_PATH)
    if not os.path.exists(os.path.join(VIEWS_PATH, 'index.html')):
        with open(os.path.join(VIEWS_PATH, 'index.html'), 'w') as f:
            f.write("Galaxy Guardian UI Not Found. Please add index.html to views/")
    run(host=HOST, port=PORT, debug=DEBUG)