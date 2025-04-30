#!/bin/bash

echo "Compiling Library Management System..."
echo "======================================="

JDBC_JAR_PATH="src/lib/mysql-connector-j-9.3.0.jar"
MAIN_CLASS="library.LoginPage"

# Check if JDBC JAR exists
if [ ! -f "$JDBC_JAR_PATH" ]; then
    echo "ERROR: JDBC Driver JAR not found at $JDBC_JAR_PATH"
    echo "Please check the JDBC_JAR_PATH variable in this script."
    read -p "Press Enter to exit..."
    exit 1
fi

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile Java files
javac -d bin -cp "$JDBC_JAR_PATH" src/library/*.java src/library/frontend/*.java src/library/backend/*.java

if [ $? -ne 0 ]; then
    echo "======================================="
    echo "ERROR: Compilation failed. See messages above."
    read -p "Press Enter to exit..."
    exit 1
fi

echo "======================================="
echo "Compilation successful."
echo "Running Library Management System..."
echo "======================================="

java -cp "bin:$JDBC_JAR_PATH" "$MAIN_CLASS"

EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
    echo "======================================="
    echo "INFO: Application exited with code $EXIT_CODE."
else
    echo "======================================="
    echo "Application finished."
fi

read -p "Press Enter to exit..."
