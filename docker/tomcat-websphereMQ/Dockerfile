FROM tomcat:9-jre8

ADD backend/target/jmstool.war webapps/jmstool.war

RUN wget http://central.maven.org/maven2/com/ibm/mq/com.ibm.mq.allclient/9.0.5.0/com.ibm.mq.allclient-9.0.5.0.jar -O lib/com.ibm.mq.allclient.jar && \
    wget http://central.maven.org/maven2/javax/jms/javax.jms-api/2.0/javax.jms-api-2.0.jar -O lib/jms-api.jar  && \
    wget https://github.com/0xERR0R/jmstool/releases/download/1.5/jmstool.war -O webapps/jmstool.war 


