#!/bin/bash

set -e

ADMIN_APP_DIR_NAME="aviasales-admin-app"
PROCESSOR_APP_DIR_NAME="advertisement-processor-service"
COMMON_MODULE_DIR_NAME="aviasales-common"
ADMIN_LOG_FILE="admin-app-out.log"
PROCESSOR_LOG_FILE="processor-app-out.log"

cd ..
PROJECT_ROOT_DIR=$(pwd)
git pull origin main

docker compose down
docker compose up -d
sleep 10

cd "${PROJECT_ROOT_DIR}/${COMMON_MODULE_DIR_NAME}"
mvn clean install -DskipTests=true

cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
mvn clean install -DskipTests=true

ADMIN_APP_JAR_NAME=$(ls target/${ADMIN_APP_DIR_NAME}*.jar 2>/dev/null | head -n 1 | xargs basename)
if [ -z "$ADMIN_APP_JAR_NAME" ]; then
  echo "Admin app JAR file not found in ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/target/"
  exit 1
fi
ADMIN_APP_JAR_PATH="${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/target/${ADMIN_APP_JAR_NAME}"

cd "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}"
mvn clean install -DskipTests=true

PROCESSOR_APP_JAR_NAME=$(ls target/${PROCESSOR_APP_DIR_NAME}*.jar 2>/dev/null | head -n 1 | xargs basename)
if [ -z "$PROCESSOR_APP_JAR_NAME" ]; then
  echo "Processor service JAR file not found in ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/target/"
  exit 1
fi
PROCESSOR_APP_JAR_PATH="${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/target/${PROCESSOR_APP_JAR_NAME}"

kill_process() {
  local jar_name_fragment=$1
  local app_name=$2
  PID=$(pgrep -f "${jar_name_fragment}")
  if [ -n "$PID" ]; then
    echo "${app_name} already running with PID(s): $PID. Terminating process(es)."
    kill $PID
    sleep 2
    if pgrep -f "${jar_name_fragment}" > /dev/null; then
      echo "Process(es) $PID did not terminate gracefully. Forcing termination."
      kill -9 $PID
      sleep 1
    else
       echo "Process(es) $PID terminated successfully."
    fi
  else
    echo "No running ${app_name} process found."
  fi
}

kill_process "${ADMIN_APP_JAR_NAME}" "Admin App"
kill_process "${PROCESSOR_APP_JAR_NAME}" "Processor Service"

cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
nohup java -jar "${ADMIN_APP_JAR_PATH}" > "${ADMIN_LOG_FILE}" 2>&1 &
ADMIN_PID=$!
echo "Admin App started with PID: ${ADMIN_PID}. Log file: ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/${ADMIN_LOG_FILE}"

cd "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}"
nohup java -jar "${PROCESSOR_APP_JAR_PATH}" > "${PROCESSOR_LOG_FILE}" 2>&1 &
PROCESSOR_PID=$!
echo "Processor Service started with PID: ${PROCESSOR_PID}. Log file: ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/${PROCESSOR_LOG_FILE}"

docker ps
cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
