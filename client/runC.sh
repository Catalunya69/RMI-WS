#!/bin/bash
# -*- ENCODING: UTF-8 -*-
echo " Per executar aquest fitxer"
echo "./runClient.sh"
echo "<° Client"
javac -cp "javax.ws.rs.jar" *.java
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=http://www.csc.calpoly.edu/~mliu/stubs/ CallbackClient
# java -Djava.security.policy=java.security.AllPermission -Djava.rmi.server.codebase=http://www.csc.calpoly.edu/~mliu/stubs/ CallbackClient
exit
