import glob
import os
import subprocess
import sys

def build():
    print("[Build]: Scanning for Java sources...")
    sources = glob.glob("backend/src/**/*.java", recursive=True)
    if not sources:
        print("[Build Error]: No Java sources found in backend/src/")
        sys.exit(1)

    print(f"[Build]: Found {len(sources)} Java source files.")
    os.makedirs("backend/bin", exist_ok=True)
    
    with open("backend/sources.txt", "w", encoding="utf-8") as f:
        for s in sources:
            f.write(f'"{s.replace(os.sep, "/")}"\n')

    cmd = [
        "javac",
        "-encoding", "UTF-8",
        "-cp", "backend/lib/sqlite-jdbc.jar;.",
        "-d", "backend/bin",
        "@backend/sources.txt"
    ]
    print(f"[Build]: Running javac...")
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print("[Build Error]: Compilation failed!")
        print(res.stderr)
        sys.exit(res.returncode)
    else:
        print("[Build Success]: All Java classes compiled successfully into backend/bin/!")

if __name__ == "__main__":
    build()
