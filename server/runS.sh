#!/bin/bash
# -*- ENCODING: UTF-8 -*-
echo " Per executar aquest fitxer"
echo "./runServer.sh"
echo "<° Servidor"
#javac *.java
javac -cp javax.ws.rs.jar:gson-2.8.2.jar:. *.java 
#javac -cp javax.ws.rs.jar:jackson.jar:. *.java 
java -cp javax.ws.rs.jar:gson-2.8.2.jar:. -Djava.security.policy=java.policy -Djava.rmi.server.codebase=http://www.csc.calpoly.edu/~mliu/stubs/ CallbackServer
exit
