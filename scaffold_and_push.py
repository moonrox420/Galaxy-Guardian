import os
import subprocess
import sys

GITHUB_REPO_URL = "https://github.com/moonrox420/Galaxy-Guardian.git"
PROJECT_NAME = "Galaxy-Guardian"

# --- File definitions (truncated for brevity here, see next message for all file contents) ---
FILES = {
    "app.py": """from bottle import route, run, request, template, static_file
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
""",
    "config.py": """import os

MODEL_PATH = "path/to/your/model.gguf"  # Update this path for a real LLM
SANDBOX_DIR = "sandbox/"
STATIC_FILES_PATH = "./static"
VIEWS_PATH = "./views"
HOST = 'localhost'
PORT = 8080
DEBUG = True

if not os.path.exists(SANDBOX_DIR):
    os.makedirs(SANDBOX_DIR)
""",
    "llm.py": """def load_language_model(model_path):
    # TODO: Integrate your real LLM loading here.
    print(f"Loading language model from {model_path}")
    return "DummyLLMModel"

def generate_code_with_llm(prompt, llm_model):
    # TODO: Replace with real LLM inference call.
    if not llm_model:
        return "# Error: Language model not loaded."
    print(f"Generating code for prompt: '{prompt}'")
    generated_code = f'''
# Python code generated for: {prompt}
def example_function():
    print("Hello from the AI!")
example_function()
'''
    return generated_code
""",
    "code_tools.py": """def format_code(code_string):
    try:
        import black
        formatted_code = black.format_str(code_string, mode=black.FileMode())
        return formatted_code
    except ImportError:
        return "# Black not installed. Code not formatted.\\n" + code_string
    except Exception as e:
        return f"# Black formatting error: {e}\\n" + code_string

def lint_code(code_string):
    try:
        from pylint.lint import Run
        from pylint.reporters.text import TextReporter
        import io
        pylint_output = io.StringIO()
        reporter = TextReporter(pylint_output)
        # Pylint works on files; for demonstration, we'll just return a stub.
        return "Pylint analysis: (Detailed output requires proper integration)\\n" + code_string
    except ImportError:
        return "# Pylint not installed. Code not linted.\\n" + code_string
    except Exception as e:
        return f"# Pylint error: {e}\\n" + code_string

def type_check_code(code_string):
    try:
        # Mypy works on files; here, we return a stub.
        return "Mypy analysis: (Detailed output requires proper integration)\\n" + code_string
    except ImportError:
        return "# Mypy not installed. Code not type-checked.\\n" + code_string
    except Exception as e:
        return f"# Mypy error: {e}\\n" + code_string

def security_analyze_code(code_string):
    try:
        # Bandit works on files; here, we return a stub.
        return "Bandit analysis: (Detailed output requires proper integration)\\n" + code_string
    except ImportError:
        return "# Bandit not installed. Code not security analyzed.\\n" + code_string
    except Exception as e:
        return f"# Bandit error: {e}\\n" + code_string
""",
    "sandbox.py": """import os

from config import SANDBOX_DIR

def execute_code_in_sandbox(code_string):
    import subprocess
    temp_file_path = os.path.join(SANDBOX_DIR, "temp_ai_script.py")
    try:
        with open(temp_file_path, "w") as f:
            f.write(code_string)
        result = subprocess.run(
            ["python", temp_file_path],
            capture_output=True,
            text=True,
            timeout=10
        )
        output = result.stdout
        error = result.stderr
        return f"--- Execution Output ---\\n{output}\\n--- Errors ---\\n{error}"
    except subprocess.TimeoutExpired:
        return "Code execution timed out."
    except Exception as e:
        return f"Error during sandboxed execution: {e}"
    finally:
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)
""",
    "requirements.txt": "bottle\nblack\nbandit\npylint\nmypy\n",
    "static/style.css": """body { font-family: sans-serif; margin: 20px; }
textarea { width: 100%; height: 150px; margin-bottom: 10px; }
pre { background-color: #eee; padding: 10px; border-radius: 5px; overflow-x: auto; }
button { padding: 10px 15px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; }
button:hover { background-color: #0056b3; }
.results-section { margin-top: 20px; border-top: 1px solid #ccc; padding-top: 20px; }
""",
    "views/index.html": """<!DOCTYPE html>
<html>
<head>
    <title>Galaxy Guardian</title>
    <link rel="stylesheet" type="text/css" href="/static/style.css">
</head>
<body>
    <h1>Galaxy Guardian</h1>
    <form id="codeForm">
        <label for="prompt">Enter your idea for the bot:</label><br>
        <textarea id="prompt" name="prompt" placeholder="e.g., a bot that fetches weather data" required></textarea><br>
        <button type="submit">Generate Code</button>
    </form>

    <div class="results-section">
        <h2>Generated Code:</h2>
        <pre id="generatedCode"></pre>

        <h2>Formatted Code:</h2>
        <pre id="formattedCode"></pre>

        <h2>Linting Results (Pylint):</h2>
        <pre id="lintResults"></pre>

        <h2>Type Checking Results (Mypy):</h2>
        <pre id="typeCheckResults"></pre>

        <h2>Security Analysis Results (Bandit):</h2>
        <pre id="securityResults"></pre>

        <h2>Code Execution:</h2>
        <button onclick="executeGeneratedCode()">Execute Code</button>
        <pre id="executionOutput"></pre>
    </div>

    <script>
        document.getElementById('codeForm').addEventListener('submit', async function(event) {
            event.preventDefault();
            const prompt = document.getElementById('prompt').value;

            const response = await fetch('/generate_code', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `prompt=${encodeURIComponent(prompt)}`
            });
            const data = await response.json();

            document.getElementById('generatedCode').textContent = data.generated_code || data.error;
            document.getElementById('formattedCode').textContent = data.formatted_code || '';
            document.getElementById('lintResults').textContent = data.lint_results || '';
            document.getElementById('typeCheckResults').textContent = data.type_check_results || '';
            document.getElementById('securityResults').textContent = data.security_results || '';
        });

        async function executeGeneratedCode() {
            const codeToExecute = document.getElementById('generatedCode').textContent;
            if (!codeToExecute) {
                alert('No code to execute!');
                return;
            }

            const response = await fetch('/execute_code', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `code=${encodeURIComponent(codeToExecute)}`
            });
            const data = await response.json();
            document.getElementById('executionOutput').textContent = data.execution_output || data.error;
        }
    </script>
</body>
</html>
""",
    # You can add docs/README.md, LICENSE, etc. here if desired
}

DIRS = [
    PROJECT_NAME,
    f"{PROJECT_NAME}/static",
    f"{PROJECT_NAME}/views",
    f"{PROJECT_NAME}/sandbox",
]

def run(cmd, cwd=None):
    print(f"Running: {cmd}")
    result = subprocess.run(cmd, shell=True, cwd=cwd, capture_output=True, text=True)
    if result.returncode != 0:
        print(result.stderr)
        sys.exit(result.returncode)
    if result.stdout:
        print(result.stdout)

def main():
    # Step 1: Create directories
    for d in DIRS:
        os.makedirs(d, exist_ok=True)

    # Step 2: Write files
    for rel_path, content in FILES.items():
        abs_path = os.path.join(PROJECT_NAME, rel_path)
        os.makedirs(os.path.dirname(abs_path), exist_ok=True)
        with open(abs_path, "w", encoding="utf-8") as f:
            f.write(content)

    # Step 3: Git init/clone
    os.chdir(PROJECT_NAME)
    if not os.path.isdir(".git"):
        run(f"git init")
        run(f"git remote add origin {GITHUB_REPO_URL}")

    # Step 4: Add, commit, push
    run("git add .")
    run('git commit -m "Initial scaffolded Galaxy Guardian commit"')
    run("git branch -M main")
    run("git pull origin main --rebase || echo 'No remote history, continuing...'")
    run("git push -u origin main")

    print("🚀 Galaxy Guardian scaffolded and pushed to GitHub!")

if __name__ == "__main__":
    main()