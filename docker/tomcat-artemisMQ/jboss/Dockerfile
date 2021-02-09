FROM daggerok/jboss-eap-7.2

ADD configure.cli /tmp/configure.cli
RUN standalone.sh -Djboss.server.default.config=standalone-full.xml & (sleep 10 && add-user.sh -a -u dev -p pass4dev -g guest && add-user.sh -m -u admin -p admin && jboss-cli.sh --connect --file=/tmp/configure.cli)
