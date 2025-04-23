FROM quay.io/wildfly/wildfly:27.0.1.Final-jdk17

WORKDIR /opt/jboss/wildfly/standalone/deployments

COPY ./artifacts/jca-random-service*.war ./jca-random-service.war

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

# EXPOSE 8080
