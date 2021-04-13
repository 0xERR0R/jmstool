FROM tomcat:9-jre8

RUN wget https://repo1.maven.org/maven2/org/apache/activemq/artemis-jms-client-all/2.16.0/artemis-jms-client-all-2.16.0.jar -O lib/artemis-all.jar && \
    wget https://repo1.maven.org/maven2/javax/jms/javax.jms-api/2.0/javax.jms-api-2.0.jar -O lib/jms-api.jar && \
    rm -rf webapps/*

COPY backend/target/jmstool.war webapps/ROOT.war
