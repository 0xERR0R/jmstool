FROM tomcat:8-jre8

RUN wget http://central.maven.org/maven2/org/apache/activemq/activemq-all/5.14.3/activemq-all-5.14.3.jar -O /usr/local/tomcat/lib/activemq-all-5.14.3.jar && \
	rm -rf /usr/local/tomcat/webapps/*
COPY docker-tomcat/context.xml /usr/local/tomcat/conf
COPY target/jmstool.war /usr/local/tomcat/webapps/
