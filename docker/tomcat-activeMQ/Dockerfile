FROM tomcat:9-jre8

RUN wget https://repo1.maven.org/maven2/org/apache/activemq/activemq-all/5.14.3/activemq-all-5.14.3.jar -O lib/activemq-all.jar && \
    wget http://central.maven.org/maven2/javax/jms/javax.jms-api/2.0/javax.jms-api-2.0.jar -O lib/jms-api.jar && \
    rm -rf webapps/*

COPY backend/target/jmstool.war webapps/ROOT.war
