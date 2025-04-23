#!/bin/bash


#set -e # Останавливать скрипт при ошибке

ADMIN_APP_DIR_NAME="aviasales-admin-app"
PROCESSOR_APP_DIR_NAME="advertisement-processor-service"
COMMON_MODULE_DIR_NAME="aviasales-common"
RA_MODULE_DIR_NAME="random-number-ra"
JCA_SERVICE_DIR_NAME="jca-random-service"
WILDFLY_DOCKER_CONTEXT_DIR="wildfly-docker" # Директория с Dockerfile для Wildfly

ADMIN_LOG_FILE="admin-app-out.log"
PROCESSOR_LOG_FILE="processor-app-out.log"

cd ..
PROJECT_ROOT_DIR=$(pwd)
echo "Project Root: ${PROJECT_ROOT_DIR}"

# 1. Удаляем директории target во всех модулях, где они есть
echo "Cleaning Maven target directories..."
rm -rf ./random-number-ra/target
rm -rf ./jca-random-service/target
rm -rf ./aviasales-common/target
rm -rf ./aviasales-admin-app/target
rm -rf ./advertisement-processor-service/target

# 2. Удаляем артефакты, скопированные для Docker
echo "Cleaning WildFly Docker artifacts..."
rm -rf ./wildfly-docker/artifacts/*

# 3. Останавливаем и удаляем контейнеры Docker Compose (на всякий случай)
echo "Stopping and removing Docker containers..."
docker compose down --volumes --remove-orphans # --volumes удалит и тома (если есть)

# 4. Очищаем кэш сборки Docker (как в скрипте, но можно и вручную)
echo "Pruning Docker build cache..."
docker builder prune -a -f

git pull origin main

echo "Stopping existing Docker containers..."
# Используем docker compose
docker compose down --remove-orphans # Удаляем и старые контейнеры

# --- Сборка Maven модулей ---
echo "Building common module..."
cd "${PROJECT_ROOT_DIR}/${COMMON_MODULE_DIR_NAME}"
mvn clean install -DskipTests=true

echo "Building and installing Resource Adapter module (RAR and JAR)..."
cd "${PROJECT_ROOT_DIR}/${RA_MODULE_DIR_NAME}"
# --- Используем install, чтобы JAR попал в локальный репозиторий ---
mvn clean install -DskipTests=true
# --- Конец изменений ---
RA_RAR_FILE=$(ls target/${RA_MODULE_DIR_NAME}-*.jar 2>/dev/null | head -n 1)
# Проверяем наличие и RAR
if [ -z "$RA_RAR_FILE" ]; then
  echo "ERROR: Resource Adapter RAR file not found after install in ${PROJECT_ROOT_DIR}/${RA_MODULE_DIR_NAME}/target/"
  exit 1
fi
# Дополнительно проверим наличие JAR в target (необязательно, но полезно для отладки)
RA_JAR_FILE=$(ls target/${RA_MODULE_DIR_NAME}-*-classes.jar 2>/dev/null | head -n 1) # Ищем JAR с классификатором
if [ -z "$RA_JAR_FILE" ]; then
   echo "WARNING: Resource Adapter JAR with 'classes' classifier was not found in target directory. Ensure 'mvn install' puts it in the local repository."
  # Не выходим, так как главное - чтобы он был в .m2
fi
echo "Found RA RAR: ${RA_RAR_FILE}"

echo "Building JCA Service WAR module..."
cd "${PROJECT_ROOT_DIR}/${JCA_SERVICE_DIR_NAME}"
# --- Добавляем флаг -U, чтобы принудительно проверить зависимости ---
mvn clean package -U -DskipTests=true
# --- Конец изменений ---
JCA_WAR_FILE=$(ls target/${JCA_SERVICE_DIR_NAME}*.war 2>/dev/null | head -n 1)
if [ -z "$JCA_WAR_FILE" ]; then
  echo "ERROR: JCA Service WAR file not found in ${PROJECT_ROOT_DIR}/${JCA_SERVICE_DIR_NAME}/target/"
  exit 1
fi
echo "Found JCA WAR: ${JCA_WAR_FILE}"

echo "Building Admin App module..."
cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
mvn clean package -DskipTests=true # Используем package вместо install, если не нужен в локальном репо
ADMIN_APP_JAR_NAME=$(ls target/${ADMIN_APP_DIR_NAME}*.jar 2>/dev/null | head -n 1 | xargs basename)
if [ -z "$ADMIN_APP_JAR_NAME" ]; then
  echo "ERROR: Admin app JAR file not found in ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/target/"
  exit 1
fi
ADMIN_APP_JAR_PATH="${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/target/${ADMIN_APP_JAR_NAME}"
echo "Found Admin App JAR: ${ADMIN_APP_JAR_PATH}"


echo "Building Processor Service module..."
cd "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}"
mvn clean package -DskipTests=true # Используем package вместо install
PROCESSOR_APP_JAR_NAME=$(ls target/${PROCESSOR_APP_DIR_NAME}*.jar 2>/dev/null | head -n 1 | xargs basename)
if [ -z "$PROCESSOR_APP_JAR_NAME" ]; then
  echo "ERROR: Processor service JAR file not found in ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/target/"
  exit 1
fi
PROCESSOR_APP_JAR_PATH="${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/target/${PROCESSOR_APP_JAR_NAME}"
echo "Found Processor Service JAR: ${PROCESSOR_APP_JAR_PATH}"

# --- Подготовка артефактов для Docker ---
echo "Preparing artifacts for WildFly Docker image..."
WILDFLY_ARTIFACTS_DIR="${PROJECT_ROOT_DIR}/${WILDFLY_DOCKER_CONTEXT_DIR}/artifacts"
mkdir -p "${WILDFLY_ARTIFACTS_DIR}"
# Используем путь к найденному файлу RAR
cp "${PROJECT_ROOT_DIR}/${RA_MODULE_DIR_NAME}/${RA_RAR_FILE}" "${WILDFLY_ARTIFACTS_DIR}/"
# Используем путь к найденному файлу WAR
cp "${PROJECT_ROOT_DIR}/${JCA_SERVICE_DIR_NAME}/${JCA_WAR_FILE}" "${WILDFLY_ARTIFACTS_DIR}/"
echo "Artifacts copied to ${WILDFLY_ARTIFACTS_DIR}"

# --- Запуск Docker Compose ---
echo "Starting Docker containers (Zookeeper, Kafka, WildFly)..."
cd "${PROJECT_ROOT_DIR}"
docker compose up --build -d
echo "Docker containers started. Waiting for WildFly health check..."

# --- Улучшенное ожидание healthcheck Wildfly ---
 JCA_SERVICE_CHECK_URL="http://localhost:8080/jca-random-service/api/random/invoice-id"
 echo "Checking JCA service availability at ${JCA_SERVICE_CHECK_URL}..."
 # Увеличим количество попыток и интервал, WildFly может долго стартовать
 MAX_RETRIES=3 # ~2 минуты ожидания
 RETRY_INTERVAL=5
 RETRIES=0
 while [ $RETRIES -lt $MAX_RETRIES ]; do
     # Проверяем, что curl вернул код 200 OK
     HTTP_CODE=$(curl --output /dev/null --silent --head --fail -w "%{http_code}" "$JCA_SERVICE_CHECK_URL" || echo "000")
     if [ "$HTTP_CODE" -eq 200 ]; then
         echo "JCA service is up! (HTTP 200 OK)"
         break
     fi
     echo "JCA service not ready yet (HTTP Code: $HTTP_CODE). Retrying in ${RETRY_INTERVAL}s... ($((RETRIES+1))/${MAX_RETRIES})"
     sleep $RETRY_INTERVAL
     RETRIES=$((RETRIES + 1))
 done

 if [ $RETRIES -eq $MAX_RETRIES ]; then
     echo "ERROR: JCA service did not become available with status 200 after ${MAX_RETRIES} retries."
     echo "Check WildFly logs:"
     # Используем docker compose logs
     docker compose logs jca-service | tail -100
     exit 1
 fi
# --- Конец улучшенного ожидания ---

# --- Запуск Spring Boot приложений (JAR) ---

# Функция для остановки старых процессов
kill_process() {
  local jar_name_fragment=$1
  local app_name=$2
  # Ищем PID по части имени JAR-файла
  PID=$(pgrep -f "${jar_name_fragment}")
  if [ -n "$PID" ]; then
    echo "Found running ${app_name} with PID(s): $PID. Terminating..."
    # Пытаемся завершить штатно
    kill $PID
    sleep 3 # Даем время на завершение
    # Проверяем, жив ли еще процесс
    if pgrep -f "${jar_name_fragment}" > /dev/null; then
      echo "Process(es) $PID did not terminate gracefully. Forcing termination (kill -9)."
      kill -9 $PID
      sleep 1
    else
       echo "Process(es) $PID terminated successfully."
    fi
  else
    echo "No running ${app_name} process found matching '${jar_name_fragment}'."
  fi
}

echo "Stopping potentially running old application JARs..."
# Передаем только имя файла JAR для поиска процесса
kill_process "${ADMIN_APP_JAR_NAME}" "Admin App"
kill_process "${PROCESSOR_APP_JAR_NAME}" "Processor Service"

echo "Starting Admin App JAR..."
cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
nohup java -jar "${ADMIN_APP_JAR_PATH}" > "${ADMIN_LOG_FILE}" 2>&1 &
ADMIN_PID=$!
# Проверка, что процесс действительно запустился (опционально)
sleep 2
if ! ps -p $ADMIN_PID > /dev/null; then
   echo "ERROR: Failed to start Admin App. Check ${ADMIN_LOG_FILE}."
   exit 1
fi
echo "Admin App started with PID: ${ADMIN_PID}. Log file: ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/${ADMIN_LOG_FILE}"


echo "Starting Processor Service JAR..."
cd "${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}"
nohup java -jar "${PROCESSOR_APP_JAR_PATH}" > "${PROCESSOR_LOG_FILE}" 2>&1 &
PROCESSOR_PID=$!
# Проверка, что процесс действительно запустился (опционально)
sleep 2
if ! ps -p $PROCESSOR_PID > /dev/null; then
   echo "ERROR: Failed to start Processor Service. Check ${PROCESSOR_LOG_FILE}."
   # Попытаться остановить админское приложение перед выходом
   kill_process "${ADMIN_APP_JAR_NAME}" "Admin App"
   exit 1
fi
echo "Processor Service started with PID: ${PROCESSOR_PID}. Log file: ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/${PROCESSOR_LOG_FILE}"

echo "Application startup complete."
echo "Running Docker containers:"
# Используем docker compose ps
docker compose ps
echo "Admin App logs: tail -f ${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}/${ADMIN_LOG_FILE}"
echo "Processor Service logs: tail -f ${PROJECT_ROOT_DIR}/${PROCESSOR_APP_DIR_NAME}/${PROCESSOR_LOG_FILE}"
# Используем docker compose logs
echo "WildFly logs: docker compose logs -f jca-service"

# Вернуться в директорию админки для удобства
cd "${PROJECT_ROOT_DIR}/${ADMIN_APP_DIR_NAME}"
