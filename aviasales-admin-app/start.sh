#!/bin/bash

set -e # Останавливать скрипт при ошибке

DOCKER_COMMAND="docker compose" # По умолчанию
if [ "$1" == "legacy" ] || [ "$1" == "--legacy-compose" ]; then
  DOCKER_COMMAND="docker-compose"
  echo "INFO: Использование legacy команды 'docker-compose' по аргументу '$1'."
elif [ -n "$1" ]; then
  echo "WARNING: Неизвестный аргумент '$1'. Используется команда по умолчанию '$DOCKER_COMMAND'."
  echo "Подсказка: используйте 'legacy' или '--legacy-compose' для 'docker-compose'."
fi

ADMIN_APP_DIR_NAME="aviasales-admin-app"
PROCESSOR_APP_DIR_NAME="advertisement-processor-service"
COMMON_MODULE_DIR_NAME="aviasales-common"
RA_MODULE_DIR_NAME="random-number-ra"
JCA_SERVICE_DIR_NAME="jca-random-service"
WILDFLY_DOCKER_CONTEXT_DIR="wildfly-docker"

ADMIN_LOG_FILE="admin-app-out.log"
PROCESSOR_LOG_FILE="processor-app-out.log"

CURRENT_SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "${CURRENT_SCRIPT_DIR}/.."
PROJECT_ROOT_DIR=$(pwd)
echo "Project Root: ${PROJECT_ROOT_DIR}"

# --- Camunda Variables ---
CAMUNDA_DIR_NAME="camunda-bpm-run-7.23.0"
CAMUNDA_BASE_DIR="${PROJECT_ROOT_DIR}/${CAMUNDA_DIR_NAME}"
CAMUNDA_INTERNAL_SCRIPT_DIR="${CAMUNDA_BASE_DIR}/internal"
CAMUNDA_RUN_SCRIPT="${CAMUNDA_INTERNAL_SCRIPT_DIR}/run.sh"
CAMUNDA_LOG_FILE="${PROJECT_ROOT_DIR}/camunda-run.log"
CAMUNDA_PORT=8090
CAMUNDA_HEALTH_CHECK_URL="http://localhost:${CAMUNDA_PORT}/camunda-welcome/index.html"

# --- Функция остановки Camunda ---
stop_camunda() {
  echo "Attempting to stop Camunda BPM Run..."
  if [ -f "${CAMUNDA_RUN_SCRIPT}" ]; then
    CAMUNDA_RUNNING=false
    if command -v ss > /dev/null; then
        if ss -tulnp | grep -q ":${CAMUNDA_PORT}" ; then CAMUNDA_RUNNING=true; fi
    elif command -v netstat > /dev/null; then
        if netstat -tulnp | grep -q ":${CAMUNDA_PORT}" ; then CAMUNDA_RUNNING=true; fi
    else
        echo "WARNING: Neither 'ss' nor 'netstat' found. Cannot reliably check if Camunda is running by port."
        if [ -f "${CAMUNDA_INTERNAL_SCRIPT_DIR}/pid" ]; then
            echo "PID file exists, attempting stop."
            CAMUNDA_RUNNING=true
        fi
    fi

    if [ "$CAMUNDA_RUNNING" = true ] ; then
      echo "Camunda BPM Run seems to be running or was running. Executing stop script..."
      # Добавляем || true, чтобы скрипт не падал, если stop возвращает ошибку (например, уже остановлен)
      (cd "${CAMUNDA_BASE_DIR}" && sh ./internal/run.sh stop) || true
      echo "Waiting for Camunda to stop..."
      for i in {1..15}; do
        RECHECK_RUNNING=false
        if command -v ss > /dev/null; then
            if ss -tulnp | grep -q ":${CAMUNDA_PORT}" ; then RECHECK_RUNNING=true; fi
        elif command -v netstat > /dev/null; then
            if netstat -tulnp | grep -q ":${CAMUNDA_PORT}" ; then RECHECK_RUNNING=true; fi
        fi
        if [ "$RECHECK_RUNNING" = false ]; then
          echo "Camunda BPM Run stopped."
          rm -f "${CAMUNDA_INTERNAL_SCRIPT_DIR}/pid"
          return 0
        fi
        echo -n "."
        sleep 1
      done
      echo
      echo "WARNING: Camunda BPM Run might not have stopped completely after 15 seconds (port ${CAMUNDA_PORT} might still be in use)."
    else
      echo "Camunda BPM Run does not seem to be running (port ${CAMUNDA_PORT} appears free or status unknown)."
      rm -f "${CAMUNDA_INTERNAL_SCRIPT_DIR}/pid"
    fi
  else
    echo "WARNING: Camunda run script not found at ${CAMUNDA_RUN_SCRIPT}. Cannot stop Camunda."
  fi
}

# --- Функция запуска Camunda ---
start_camunda() {
  echo "Starting Camunda BPM Run..."
  if [ ! -f "${CAMUNDA_RUN_SCRIPT}" ]; then
    echo "ERROR: Camunda run script not found at ${CAMUNDA_RUN_SCRIPT}. Cannot start Camunda."
    exit 1
  fi

  stop_camunda # Убедимся, что предыдущий экземпляр точно остановлен

  echo "Executing Camunda start script in detached mode..."
  (cd "${CAMUNDA_BASE_DIR}" && nohup sh ./internal/run.sh start --detached > "${CAMUNDA_LOG_FILE}" 2>&1 &)

  echo "Camunda BPM Run start initiated. Log file: ${CAMUNDA_LOG_FILE}"
  echo "Waiting for Camunda BPM Run to be available at ${CAMUNDA_HEALTH_CHECK_URL}..."

  MAX_CAMUNDA_RETRIES=36
  RETRY_CAMUNDA_INTERVAL=5
  CAMUNDA_RETRIES=0
  while [ $CAMUNDA_RETRIES -lt $MAX_CAMUNDA_RETRIES ]; do
    HTTP_CODE_CAMUNDA=$(curl --output /dev/null --silent --head --fail -w "%{http_code}" "${CAMUNDA_HEALTH_CHECK_URL}" || echo "000")
    if [ "$HTTP_CODE_CAMUNDA" -eq 200 ] || [ "$HTTP_CODE_CAMUNDA" -eq 302 ]; then
        echo "Camunda BPM Run is up! (HTTP ${HTTP_CODE_CAMUNDA} at ${CAMUNDA_HEALTH_CHECK_URL})"
        return 0
    fi
    echo "Camunda BPM Run not ready yet (HTTP Code: $HTTP_CODE_CAMUNDA). Retrying in ${RETRY_CAMUNDA_INTERVAL}s... ($((CAMUNDA_RETRIES+1))/${MAX_CAMUNDA_RETRIES})"
    sleep $RETRY_CAMUNDA_INTERVAL
    CAMUNDA_RETRIES=$((CAMUNDA_RETRIES + 1))
  done

  echo "ERROR: Camunda BPM Run did not become available after ${MAX_CAMUNDA_RETRIES} retries."
  echo "Check Camunda logs: tail -100 ${CAMUNDA_LOG_FILE}"
  exit 1
}

# --- Начало основного скрипта ---

stop_camunda

echo "Cleaning Maven target directories..."
rm -rf "${PROJECT_ROOT_DIR}/${RA_MODULE_DIR_NAME}/target"
rm -rf "${PROJECT_ROOT_DIR}/${JCA_SERVICE_DIR_NAME}/target"
rm -rf "${PROJECT_ROOT_DIR}/${COMMON_MODULE_DIR_NAME}/target"
rm -rf "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/target"
rm -rf "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/target"

echo "Cleaning WildFly Docker artifacts..."
rm -rf "${PROJECT_ROOT_DIR}/${WILDFLY_DOCKER_CONTEXT_DIR}/artifacts/"*

echo "Stopping and removing Docker containers (initial cleanup)..."
$DOCKER_COMMAND down --volumes --remove-orphans

echo "Pruning Docker build cache..."
docker builder prune -a -f

echo "Pulling latest changes from git origin main..."
git pull origin main

echo "Stopping existing Docker containers again (after git pull)..."
$DOCKER_COMMAND down --remove-orphans

echo "Building common module..."
cd "${PROJECT_ROOT_DIR}/${COMMON_MODULE_DIR_NAME}"
mvn clean install -DskipTests=true

echo "Building and installing Resource Adapter module..."
cd "${PROJECT_ROOT_DIR}/${RA_MODULE_DIR_NAME}"
mvn clean install -DskipTests=true
# ВАЖНО: RA_ARTIFACT_FOR_WILDFLY ищет .jar. Ваш предыдущий скрипт называл переменную RA_RAR_FILE, но искал .jar.
# Если для WildFly Resource Adapter действительно нужен .rar файл, вам нужно:
# 1. Изменить pom.xml для модуля random-number-ra, чтобы он собирал .rar (например, с помощью maven-rar-plugin).
# 2. Изменить строку ниже для поиска *.rar файла.
RA_ARTIFACT_FOR_WILDFLY=$(ls target/${RA_MODULE_DIR_NAME}-*.jar 2>/dev/null | head -n 1)
if [ -z "$RA_ARTIFACT_FOR_WILDFLY" ]; then
  echo "ERROR: Resource Adapter artifact (expected .jar based on current search pattern) not found in ${PROJECT_ROOT_DIR}/${RA_MODULE_DIR_NAME}/target/"
  exit 1
fi
echo "Found Resource Adapter artifact: ${RA_ARTIFACT_FOR_WILDFLY}"


echo "Building JCA Service WAR module..."
cd "${PROJECT_ROOT_DIR}/${JCA_SERVICE_DIR_NAME}"
mvn clean package -U -DskipTests=true
JCA_WAR_FILE=$(ls target/${JCA_SERVICE_DIR_NAME}*.war 2>/dev/null | head -n 1)
if [ -z "$JCA_WAR_FILE" ]; then
  echo "ERROR: JCA Service WAR file not found in ${PROJECT_ROOT_DIR}/${JCA_SERVICE_DIR_NAME}/target/"
  exit 1
fi
echo "Found JCA WAR: ${JCA_WAR_FILE}"

echo "Building Admin App module..."
cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
mvn clean package -DskipTests=true
ADMIN_APP_JAR_NAME_BASENAME=$(ls target/${ADMIN_APP_DIR_NAME}*.jar 2>/dev/null | head -n 1 | xargs basename)
if [ -z "$ADMIN_APP_JAR_NAME_BASENAME" ]; then
  echo "ERROR: Admin app JAR file not found in ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/target/"
  exit 1
fi
ADMIN_APP_JAR_PATH="${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/target/${ADMIN_APP_JAR_NAME_BASENAME}"
echo "Found Admin App JAR: ${ADMIN_APP_JAR_PATH}"


echo "Building Processor Service module..."
cd "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}"
mvn clean package -DskipTests=true
PROCESSOR_APP_JAR_NAME_BASENAME=$(ls target/${PROCESSOR_APP_DIR_NAME}*.jar 2>/dev/null | head -n 1 | xargs basename)
if [ -z "$PROCESSOR_APP_JAR_NAME_BASENAME" ]; then
  echo "ERROR: Processor service JAR file not found in ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/target/"
  exit 1
fi
PROCESSOR_APP_JAR_PATH="${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/target/${PROCESSOR_APP_JAR_NAME_BASENAME}"
echo "Found Processor Service JAR: ${PROCESSOR_APP_JAR_PATH}"

echo "Preparing artifacts for WildFly Docker image..."
WILDFLY_ARTIFACTS_DIR="${PROJECT_ROOT_DIR}/${WILDFLY_DOCKER_CONTEXT_DIR}/artifacts"
mkdir -p "${WILDFLY_ARTIFACTS_DIR}"

cp "${PROJECT_ROOT_DIR}/${RA_MODULE_DIR_NAME}/target/$(basename ${RA_ARTIFACT_FOR_WILDFLY})" "${WILDFLY_ARTIFACTS_DIR}/" # Используем basename на случай, если RA_ARTIFACT_FOR_WILDFLY содержал target/
cp "${PROJECT_ROOT_DIR}/${JCA_SERVICE_DIR_NAME}/target/$(basename ${JCA_WAR_FILE})" "${WILDFLY_ARTIFACTS_DIR}/" # Используем basename
echo "Artifacts copied to ${WILDFLY_ARTIFACTS_DIR}"

echo "Starting Docker containers (Zookeeper, Kafka, WildFly) using '$DOCKER_COMMAND'..."
cd "${PROJECT_ROOT_DIR}"
$DOCKER_COMMAND up --build -d
echo "Docker containers started. Waiting for WildFly health check..."

JCA_SERVICE_CHECK_URL="http://localhost:8080/jca-random-service/api/random/invoice-id"
echo "Checking JCA service availability at ${JCA_SERVICE_CHECK_URL}..."
MAX_WILDFLY_RETRIES=24
RETRY_WILDFLY_INTERVAL=5
WILDFLY_RETRIES=0
while [ $WILDFLY_RETRIES -lt $MAX_WILDFLY_RETRIES ]; do
    HTTP_CODE_WILDFLY=$(curl --output /dev/null --silent --head --fail -w "%{http_code}" "$JCA_SERVICE_CHECK_URL" || echo "000")
    if [ "$HTTP_CODE_WILDFLY" -eq 200 ]; then
        echo "JCA service (WildFly) is up! (HTTP 200 OK)"
        break
    fi
    echo "JCA service not ready yet (HTTP Code: $HTTP_CODE_WILDFLY). Retrying in ${RETRY_WILDFLY_INTERVAL}s... ($((WILDFLY_RETRIES+1))/${MAX_WILDFLY_RETRIES})"
    sleep $RETRY_WILDFLY_INTERVAL
    WILDFLY_RETRIES=$((WILDFLY_RETRIES + 1))
done

if [ $WILDFLY_RETRIES -eq $MAX_WILDFLY_RETRIES ]; then
    echo "ERROR: JCA service (WildFly) did not become available with status 200 after ${MAX_WILDFLY_RETRIES} retries."
    echo "Check WildFly logs:"
    $DOCKER_COMMAND logs jca-service | tail -100
    echo "Stopping Docker containers due to WildFly startup failure..."
    $DOCKER_COMMAND down --remove-orphans || true # Попытка остановить, даже если были ошибки
    exit 1
fi

start_camunda

kill_spring_boot_process() {
  local jar_name_pattern=$1
  local app_name=$2

  PID=$(pgrep -f "java -jar .*${jar_name_pattern}")
  if [ -n "$PID" ]; then
    echo "Found running ${app_name} with PID(s): $PID (matched by '${jar_name_pattern}'). Terminating..."
    # Добавляем || true, чтобы скрипт не падал, если процесс уже убит или kill вернул ошибку
    kill $PID || true
    sleep 3
    # Повторно проверяем с pgrep, так как kill может вернуть 0, даже если процесс не завершился немедленно
    if pgrep -f "java -jar .*${jar_name_pattern}" > /dev/null; then
      echo "Process(es) for ${app_name} (PID $PID) did not terminate gracefully. Forcing termination (kill -9)."
      kill -9 $PID || true
      sleep 1
    else
       echo "Process(es) for ${app_name} (PID $PID) terminated successfully."
    fi
  else
    echo "No running ${app_name} process found matching pattern '${jar_name_pattern}'."
  fi
}

echo "Stopping potentially running old application JARs..."
kill_spring_boot_process "${ADMIN_APP_JAR_NAME_BASENAME}" "Admin App"
kill_spring_boot_process "${PROCESSOR_APP_JAR_NAME_BASENAME}" "Processor Service"

echo "Starting Admin App JAR..."
cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
nohup java -jar "${ADMIN_APP_JAR_PATH}" > "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/${ADMIN_LOG_FILE}" 2>&1 &
ADMIN_PID=$!
sleep 5
if ! ps -p $ADMIN_PID > /dev/null; then
   echo "ERROR: Failed to start Admin App. Check ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/${ADMIN_LOG_FILE}."
   stop_camunda || true
   echo "Stopping Docker containers due to Admin App startup failure..."
   $DOCKER_COMMAND down --remove-orphans || true
   exit 1
fi
echo "Admin App started with PID: ${ADMIN_PID}. Log file: ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/${ADMIN_LOG_FILE}"


echo "Starting Processor Service JAR..."
cd "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}"
nohup java -jar "${PROCESSOR_APP_JAR_PATH}" > "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/${PROCESSOR_LOG_FILE}" 2>&1 &
PROCESSOR_PID=$!
sleep 5
if ! ps -p $PROCESSOR_PID > /dev/null; then
   echo "ERROR: Failed to start Processor Service. Check ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/${PROCESSOR_LOG_FILE}."
   kill_spring_boot_process "${ADMIN_APP_JAR_NAME_BASENAME}" "Admin App" || true
   stop_camunda || true
   echo "Stopping Docker containers due to Processor Service startup failure..."
   $DOCKER_COMMAND down --remove-orphans || true
   exit 1
fi
echo "Processor Service started with PID: ${PROCESSOR_PID}. Log file: ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/${PROCESSOR_LOG_FILE}"

echo ""
echo "-----------------------------------------"
echo "Application startup complete."
echo "-----------------------------------------"
echo "Running Docker containers:"
$DOCKER_COMMAND ps
echo ""
echo "--- Logs ---"
echo "Admin App logs: tail -f ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/${ADMIN_LOG_FILE}"
echo "Processor Service logs: tail -f ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/${PROCESSOR_LOG_FILE}"
echo "WildFly (JCA Service) logs: $DOCKER_COMMAND logs -f jca-service"
echo "Camunda BPM Run logs: tail -f ${CAMUNDA_LOG_FILE}"
echo "-----------------------------------------"

cd "${PROJECT_ROOT_DIR}"
echo "Script finished successfully. Current directory: $(pwd)"
