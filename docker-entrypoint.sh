#!/bin/sh
set -eu
# Render supplies postgresql://user:pass@host:port/database.
# Spring JDBC uses jdbc:postgresql://host:port/database
# and receives the username/password separately.
if [ -n "${DATABASE_URL:-}" ]; then
  rest="${DATABASE_URL#*://}"
  credentials="${rest%%@*}"
  host_and_db="${rest#*@}"
  export SPRING_DATASOURCE_USERNAME="${credentials%%:*}"
  export SPRING_DATASOURCE_PASSWORD="${credentials#*:}"
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${host_and_db}"
fi
exec java -jar /app/app.jar
