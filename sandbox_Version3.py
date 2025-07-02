import os

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
        return f"--- Execution Output ---\n{output}\n--- Errors ---\n{error}"
    except subprocess.TimeoutExpired:
        return "Code execution timed out."
    except Exception as e:
        return f"Error during sandboxed execution: {e}"
    finally:
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)