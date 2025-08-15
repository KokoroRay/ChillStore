#!/bin/bash
echo "Starting SQL Server setup..."

# Start SQL Server in background
/opt/mssql/bin/sqlservr &

# Wait for SQL Server to start
echo "Waiting for SQL Server to start..."
sleep 30

# Test connection
echo "Testing connection..."
for i in {1..30}; do
    if /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -C -No -Q "SELECT 1" > /dev/null 2>&1; then
        echo "SQL Server is ready!"
        break
    fi
    echo "Waiting for SQL Server... ($i/30)"
    sleep 2
done

# Run initialization script
echo "Running database initialization..."
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -C -No -i /init-db.sql

echo "Database setup completed!"

# Keep SQL Server running
wait