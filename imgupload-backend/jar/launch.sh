#!/bin/bash

export DB_URL="jdbc:postgresql://localhost:5432/your_db"
export DB_USERNAME="your_db_user"
export DB_PASSWORD="your_password"

export SERVER_PORT="8080"
CORS_ALLOWED_ORIGIN="https://yourhost.com/"

IMAGE_UPLOAD_DIR="/your/image/upload/dir"
IMAGE_BASE_URL="https://www.yourdomain.com/"

JAVA_EXECUTABLE="java"



if [ -z "$1" ]; then
    echo "Usage: $0 <jar_file_name>"
    echo "Example: $0 yourjar.jar"
    exit 1
fi

JAR_FILE="$1"

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file '$JAR_FILE' not found in $(pwd)"
    exit 1
fi

echo "Starting $JAR_FILE on port $SERVER_PORT with profile $SPRING_PROFILES_ACTIVE..."

"$JAVA_EXECUTABLE" $JAVA_OPTS -jar "$JAR_FILE" \
    --server.port="$SERVER_PORT" \
    --app.cors.allowedOrigin="$CORS_ALLOWED_ORIGIN" \
    --app.image.upload-dir="$IMAGE_UPLOAD_DIR" \
    --app.image.base-url="$IMAGE_BASE_URL"

echo "Application stopped."