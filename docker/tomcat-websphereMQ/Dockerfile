FROM tomcat:9-jre8

RUN wget https://repo1.maven.org/maven2/com/ibm/mq/com.ibm.mq.allclient/9.1.1.0/com.ibm.mq.allclient-9.1.1.0.jar -O lib/com.ibm.mq.allclient.jar && \
    wget https://repo1.maven.org/maven2/javax/jms/javax.jms-api/2.0/javax.jms-api-2.0.jar -O lib/jms-api.jar && \
    rm -rf webapps/*

COPY backend/target/jmstool.war webapps/ROOT.war
