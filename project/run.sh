#!/usr/bin/env bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "================================================================================"
echo "  RAMRAO ADIK INSTITUTE OF TECHNOLOGY, NERUL - DEPT OF COMPUTER ENGG."
echo "  SMART BANKING TRANSACTION & ACCOUNT PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)"
echo "  Candidate: Anish Vyapari | Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1"
echo "================================================================================"

# Check Java
if ! command -v java &> /dev/null; then
    echo "[CRITICAL ERROR] 'java' command not found. Please install OpenJDK 17 or 21."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -n 1)
echo "[INFO] Java Runtime: $JAVA_VER"

mkdir -p backend/bin backend/lib database docs screenshots

# Download SQLite drivers if missing
if [ ! -f "backend/lib/sqlite-jdbc.jar" ]; then
    echo "[INFO] Downloading SQLite JDBC Driver..."
    curl -L -o "backend/lib/sqlite-jdbc.jar" "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar"
fi
if [ ! -f "backend/lib/slf4j-api.jar" ]; then
    echo "[INFO] Downloading SLF4J API..."
    curl -L -o "backend/lib/slf4j-api.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar"
fi
if [ ! -f "backend/lib/slf4j-simple.jar" ]; then
    echo "[INFO] Downloading SLF4J Simple..."
    curl -L -o "backend/lib/slf4j-simple.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.12/slf4j-simple-2.0.12.jar"
fi

# Compile if needed
if [ ! -f "backend/bin/com/sbtms/Main.class" ]; then
    echo "[INFO] Compiling SB-TMS backend Java classes..."
    javac -encoding UTF-8 -cp "backend/lib/*:." -d "backend/bin" backend/src/com/sbtms/*.java backend/src/com/sbtms/model/*.java backend/src/com/sbtms/interfaces/*.java backend/src/com/sbtms/exception/*.java backend/src/com/sbtms/db/*.java backend/src/com/sbtms/service/*.java backend/src/com/sbtms/web/*.java backend/src/com/sbtms/cli/*.java
fi

echo "================================================================================"
echo "  SERVER STARTING AT: http://localhost:8080/index.html"
echo "================================================================================"

# Try opening default browser
if command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:8080/index.html" &
elif command -v open &> /dev/null; then
    open "http://localhost:8080/index.html" &
fi

java -cp "backend/bin:backend/lib/*" com.sbtms.Main --port 8080
