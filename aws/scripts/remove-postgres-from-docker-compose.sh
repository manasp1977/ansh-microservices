#!/bin/bash

# Remove PostgreSQL service from docker-compose.uat.yml
# Since we're using RDS, we don't need the containerized PostgreSQL

set -e

DOCKER_COMPOSE_FILE="docker-compose.uat.yml"

echo "========================================="
echo "Remove PostgreSQL from Docker Compose"
echo "========================================="
echo ""

if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
    echo "ERROR: $DOCKER_COMPOSE_FILE not found"
    exit 1
fi

# Backup current file
echo "Creating backup of current docker-compose.uat.yml..."
cp $DOCKER_COMPOSE_FILE ${DOCKER_COMPOSE_FILE}.backup
echo "✓ Backup created: ${DOCKER_COMPOSE_FILE}.backup"
echo ""

echo "Removing postgres service from $DOCKER_COMPOSE_FILE..."

# Create a temporary Python script to remove postgres service and update depends_on
cat > /tmp/remove_postgres.py << 'PYTHON_SCRIPT'
import sys
import re

with open(sys.argv[1], 'r') as f:
    content = f.read()

# Remove postgres service block (including everything until the next service or volumes section)
# This regex matches from "  postgres:" to just before the next service
postgres_pattern = r'  postgres:.*?(?=  [a-z-]+:|volumes:|networks:|^$)'
content = re.sub(postgres_pattern, '', content, flags=re.DOTALL)

# Remove postgres from depends_on sections and clean up
lines = content.split('\n')
new_lines = []
skip_next = False
in_depends_on = False

for i, line in enumerate(lines):
    # Track if we're in a depends_on block
    if 'depends_on:' in line:
        in_depends_on = True
        new_lines.append(line)
        continue

    # Check if we're leaving depends_on block
    if in_depends_on and line and not line.startswith(' ' * 6):
        in_depends_on = False

    # Skip postgres entries in depends_on
    if in_depends_on and 'postgres:' in line:
        # Skip this line and the next line (condition: service_healthy)
        skip_next = True
        continue

    if skip_next:
        skip_next = False
        continue

    new_lines.append(line)

# Remove postgres_data volume
new_content = '\n'.join(new_lines)
new_content = re.sub(r'  postgres_data:\s*\n', '', new_content)

# Remove extra blank lines
new_content = re.sub(r'\n\n\n+', '\n\n', new_content)

with open(sys.argv[1], 'w') as f:
    f.write(new_content)

print("✓ PostgreSQL service removed")
PYTHON_SCRIPT

# Run the Python script
python3 /tmp/remove_postgres.py $DOCKER_COMPOSE_FILE 2>/dev/null || {
    # Fallback to manual sed-based approach if Python fails
    echo "Python approach failed, using sed fallback..."

    # Remove postgres service section (lines from '  postgres:' to the next service)
    sed -i '/^  postgres:/,/^  [a-z-]/{ /^  postgres:/d; /^  [a-z-]/!d; }' $DOCKER_COMPOSE_FILE

    # Remove postgres from depends_on
    sed -i '/postgres:/,/condition: service_healthy/d' $DOCKER_COMPOSE_FILE

    # Remove postgres_data volume
    sed -i '/^  postgres_data:/d' $DOCKER_COMPOSE_FILE

    echo "✓ PostgreSQL service removed (sed method)"
}

# Clean up temp file
rm -f /tmp/remove_postgres.py

echo ""
echo "========================================="
echo "✓ PostgreSQL Removal Complete!"
echo "========================================="
echo ""
echo "Changes made to $DOCKER_COMPOSE_FILE:"
echo "  - Removed postgres service"
echo "  - Removed postgres from depends_on clauses"
echo "  - Removed postgres_data volume"
echo ""
echo "Services will now connect to RDS instead of containerized PostgreSQL"
echo ""
