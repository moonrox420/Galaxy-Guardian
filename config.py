import os

MODEL_PATH = "path/to/your/model.gguf"  # Update this path for a real LLM
SANDBOX_DIR = "sandbox/"
STATIC_FILES_PATH = "./static"
VIEWS_PATH = "./views"
HOST = 'localhost'
PORT = 8080
DEBUG = True

if not os.path.exists(SANDBOX_DIR):
    os.makedirs(SANDBOX_DIR)