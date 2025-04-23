# Используем официальный образ WildFly
FROM quay.io/wildfly/wildfly:27.0.1.Final-jdk17

# Установим unzip (если нужно для отладки)
# USER root
# RUN microdnf install -y unzip tar gzip && microdnf clean all
# USER jboss

WORKDIR /opt/jboss/wildfly/standalone/deployments

# Копируем артефакты
COPY ./artifacts/random-number-ra-*.rar ./random-number-ra.rar
COPY ./artifacts/jca-random-service*.war ./jca-random-service.war

# --- Копируем кастомную конфигурацию ---
COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml
# -------------------------------------

# WildFly запускается по умолчанию при старте контейнера на порту 8080
# EXPOSE 8080