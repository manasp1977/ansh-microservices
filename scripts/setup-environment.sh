#!/bin/bash
# Environment Setup Script for AnshShare Microservices (Linux/Mac)
# This script sets environment variables for the current session

echo "========================================"
echo " AnshShare Environment Setup"
echo "========================================"
echo ""

# ====================
# Java Configuration
# ====================
echo "Setting up Java..."

# Try to find Java
if [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
elif [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
elif [ -d "/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home"
elif [ -d "$HOME/.jdks/openjdk-17" ]; then
    export JAVA_HOME="$HOME/.jdks/openjdk-17"
else
    echo "[WARNING] Java 17+ not found in common locations"
    echo "Please set JAVA_HOME manually"
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "[OK] JAVA_HOME = $JAVA_HOME"
fi
echo ""

# ====================
# Maven Configuration
# ====================
echo "Setting up Maven..."

# Try to find Maven
if [ -d "/usr/share/maven" ]; then
    export MAVEN_HOME="/usr/share/maven"
    export M2_HOME="$MAVEN_HOME"
elif [ -d "/opt/maven" ]; then
    export MAVEN_HOME="/opt/maven"
    export M2_HOME="$MAVEN_HOME"
elif [ -d "/usr/local/Cellar/maven" ]; then
    # Mac with Homebrew
    MAVEN_VERSION=$(ls /usr/local/Cellar/maven | tail -n 1)
    export MAVEN_HOME="/usr/local/Cellar/maven/$MAVEN_VERSION/libexec"
    export M2_HOME="$MAVEN_HOME"
else
    echo "[WARNING] Maven not found in common locations"
    echo "Please install Maven or set MAVEN_HOME manually"
fi

if [ -n "$MAVEN_HOME" ]; then
    export PATH="$MAVEN_HOME/bin:$PATH"
    echo "[OK] MAVEN_HOME = $MAVEN_HOME"
fi
echo ""

# ====================
# PostgreSQL Configuration
# ====================
echo "Setting up PostgreSQL..."

# PostgreSQL is usually in PATH by default on Linux/Mac
if command -v psql &> /dev/null; then
    PGSQL_BIN=$(which psql)
    export PGSQL_HOME=$(dirname $(dirname $PGSQL_BIN))
    echo "[OK] PostgreSQL found at: $PGSQL_HOME"
else
    echo "[WARNING] PostgreSQL not found in PATH"
    echo "Please install PostgreSQL"
fi
echo ""

# ====================
# Maven Options
# ====================
echo "Setting Maven options..."
export MAVEN_OPTS="-Xmx2048m -Xms512m"
echo "[OK] MAVEN_OPTS = $MAVEN_OPTS"
echo ""

echo "========================================"
echo " Environment Setup Complete"
echo "========================================"
echo ""
echo "Current session environment:"
echo "  JAVA_HOME  = $JAVA_HOME"
echo "  MAVEN_HOME = $MAVEN_HOME"
echo "  PGSQL_HOME = $PGSQL_HOME"
echo ""
echo "NOTE: These settings are for THIS SESSION ONLY"
echo "To make permanent, add to ~/.bashrc or ~/.zshrc"
echo ""

# Verify installations
echo "========================================"
echo " Verifying Installations"
echo "========================================"
echo ""

echo "Java version:"
java -version
echo ""

echo "Maven version:"
mvn -version
echo ""

echo "PostgreSQL version:"
psql --version
echo ""

echo "========================================"
echo " Verification Complete"
echo "========================================"
echo ""
echo "If you see version information above, setup was successful!"
echo ""
echo "To make these settings permanent, add to ~/.bashrc or ~/.zshrc:"
echo "  export JAVA_HOME=\"$JAVA_HOME\""
echo "  export MAVEN_HOME=\"$MAVEN_HOME\""
echo "  export PATH=\"\$JAVA_HOME/bin:\$MAVEN_HOME/bin:\$PATH\""
echo ""
echo "Next steps:"
echo "1. Run: ./setup-databases.sh"
echo "2. Run: cd .. && mvn clean install"
echo "3. Run: ./start-all-services.sh"
echo ""
