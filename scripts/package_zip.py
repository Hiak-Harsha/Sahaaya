#!/usr/bin/env python3
"""
Sahaaya Packaging Script
Creates clean, optimized ZIP archives for submission and distribution:
1. Sahaaya-Submission.zip (Submission folder + docs + APK)
2. Sahaaya-Complete-Project.zip (Full codebase + assets + docs + APK)
"""

import os
import sys
import zipfile
from pathlib import Path

WORKSPACE_ROOT = Path("C:/Users/madha/Desktop/Sahaaya-main")
PROJECT_ROOT = WORKSPACE_ROOT / "Sahaaya-main"

EXCLUDE_DIRS = {
    '.gradle', '.kotlin', 'intermediates', 'tmp', '__pycache__', '.idea', '.git'
}

def create_submission_zip():
    zip_path = WORKSPACE_ROOT / "Sahaaya-Submission.zip"
    print(f"Creating {zip_path}...")
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        # Add submission files
        sub_dir = PROJECT_ROOT / "submission"
        for root, dirs, files in os.walk(sub_dir):
            for file in files:
                file_path = Path(root) / file
                arcname = f"submission/{file_path.relative_to(sub_dir)}"
                zipf.write(file_path, arcname)

        # Add docs
        docs_dir = PROJECT_ROOT / "docs"
        for root, dirs, files in os.walk(docs_dir):
            for file in files:
                file_path = Path(root) / file
                arcname = f"docs/{file_path.relative_to(docs_dir)}"
                zipf.write(file_path, arcname)

        # Add README
        zipf.write(PROJECT_ROOT / "README.md", "README.md")
        zipf.write(PROJECT_ROOT / "Problem-Statement-Plan.md", "Problem-Statement-Plan.md")

    print(f"Created {zip_path} ({zip_path.stat().st_size / (1024*1024):.2f} MB)")

def create_complete_project_zip():
    zip_path = WORKSPACE_ROOT / "Sahaaya-Complete-Project.zip"
    print(f"Creating {zip_path}...")
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(PROJECT_ROOT):
            # Skip excluded dirs
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
            
            for file in files:
                if file.endswith('.pyc') or file.endswith('.lock') or file.endswith('.hprof'):
                    continue
                file_path = Path(root) / file
                arcname = file_path.relative_to(PROJECT_ROOT)
                zipf.write(file_path, arcname)

    print(f"Created {zip_path} ({zip_path.stat().st_size / (1024*1024):.2f} MB)")

if __name__ == "__main__":
    create_submission_zip()
    create_complete_project_zip()
    print("All ZIP files created successfully in workspace!")
