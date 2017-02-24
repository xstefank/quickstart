cluster-ha-singleton: A SingletonService started by SingletonStartup
=============================================================================================================
Author: Wolf-Dieter Fink  
Level: Advanced  
Technologies: EJB, HASingleton, JNDI, Clustering, MSC  
Summary: The `cluster-ha-service` quickstart deploys a Service wrapped with the SingletonService decorator, and used as a cluster-wide singleton service.  
Target Product: WildFly  
Source: <https://github.com/wildfly/quickstart/>  

What is it?
-----------

This example demonstrates the deployment of a Service that is wrapped with the SingletonService decorator and used as a cluster-wide singleton service in Wildfly application server. The service activates a cheduled timer, which is started only once in the cluster.

The example is composed of a Maven subproject and a parent project. The projects are as follows:

1. `service`: This subproject contains the Service and the EJB code to instantiate, start and access the service.
2. The root parent `pom.xml` builds the `service` subproject and deploys the archive to the server.


System requirements
-------------------

All you need to build this project is Java 8.0 (Java SDK 1.8) or later and Maven 3.1 or better.

The application this project produces is designed to be run on JBoss WildFly.

 
Configure Maven
---------------

You can copy or link to the Maven configuration information in the README file in the root folder of the quickstarts. For example:

If you have not yet done so, you must [Configure Maven](../README.md#mavenconfiguration) before testing the quickstarts.


Start JBoss WildFly with a HA profile
-------------------------

If you run a non HA profile the singleton service will not start correctly. To run the example one instance must be started, to see the singleton behaviour at minimum two instances
should be started.

    Start server one : standalone.sh --server-config=standalone-ha.xml -Djboss.node.name=nodeOne
    Start server two : standalone.sh --server-config=standalone-ha.xml -Djboss.node.name=nodeTwo -Djboss.socket.binding.port-offset=100


Build and Deploy the Quickstart
-------------------------

_NOTE: The following build command assumes you have configured your Maven user settings. If you have not, you must include Maven setting arguments on the command line. See [Build and Deploy the Quickstarts](../README.md#buildanddeploy) for complete instructions and additional options._

1. Make sure you have started the JBoss Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. Type this command to build and deploy the archive:

        mvn clean install wildfly:deploy

4. This will deploy `service/target/wildfly-cluster-ha-singleton-service.jar` to the running instance of the server.
5. Type this command to deploy the archive to the second server (or more) and replace the port, depending on your settings:

        mvn wildfly:deploy -Dwildfly.port=10090

6. This will deploy `service/target/wildfly-cluster-ha-singleton-service.jar` to the running instance of the additional server.
 
7. To verify the application deployed to each server instance, check the server logs. The first instance have the following message:

        INFO  [org.wildfly.clustering.server] (DistributedSingletonService - 1) WFLYCLSV0003: localhost elected as the singleton provider of the jboss.quickstart.ha.singleton.timer service
        
    The first server instance will alse have messages like the following:
    
        INFO  [class org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.SchedulerBean] (EJB default - 1) HASingletonTimer: Info=HASingleton timer @localhost <timestamp>
        INFO  [class org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.SchedulerBean] (EJB default - 1) HASingletonTimer: Info=HASingleton timer @localhost <timestamp>

    The other servers will have the message:
    
        WFLYSRV0010: Deployed "wildfly-cluster-ha-singleton-service.jar" (runtime-name : "wildfly-cluster-ha-singleton-service.jar")

    _NOTE: You will se the following warnings in both server logs when you deploy the application. You can ignore them._
    
        WARN  [org.jgroups.protocols.UDP] (ServerService Thread Pool -- 70) JGRP000015: the send buffer of socket ManagedMulticastSocketBinding was set to 1MB, but the OS only allocated 212.99KB. This might lead to performance problems. Please set your max send buffer in the OS correctly (e.g. net.core.wmem_max on Linux)
        WARN  [org.jgroups.protocols.UDP] (ServerService Thread Pool -- 70) JGRP000015: the receive buffer of socket ManagedMulticastSocketBinding was set to 20MB, but the OS only allocated 212.99KB. This might lead to performance problems. Please set your max receive buffer in the OS correctly (e.g. net.core.rmem_max on Linux)
        WARN  [org.jgroups.protocols.UDP] (ServerService Thread Pool -- 70) JGRP000015: the send buffer of socket ManagedMulticastSocketBinding was set to 1MB, but the OS only allocated 212.99KB. This might lead to performance problems. Please set your max send buffer in the OS correctly (e.g. net.core.wmem_max on Linux)
        WARN  [org.jgroups.protocols.UDP] (ServerService Thread Pool -- 70) JGRP000015: the receive buffer of socket ManagedMulticastSocketBinding was set to 25MB, but the OS only allocated 212.99KB. This might lead to performance problems. Please set your max receive buffer in the OS correctly (e.g. net.core.rmem_max on Linux)

8. The timer started on the server instance will log a message every 10 seconds. if you stop the first server instance, you see messages in the second server console indicating it is now the singleton provider.

        WFLYCLSV0003: localhost elected as the singleton provider of the jboss.quickstart.ha.singleton.timer service
        INFO  [org.wildfly.clustering.server] (DistributedSingletonService - 1) WFLYCLSV0001: This node will now operate as the singleton provider of the jboss.quickstart.ha.singleton.timer service
        INFO  [class org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.HATimerService] (MSC service thread 1-4) Start HASingleton timer service 'org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.HATimerService'
        INFO  [org.infinispan.remoting.transport.jgroups.JGroupsTransport] (thread-4,ee,localhost) ISPN000094: Received new cluster view for channel server: [localhost|4] (1) [localhost]
        INFO  [class org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.SchedulerBean] (EJB default - 1) HASingletonTimer: Info=HASingleton timer @localhost <timestamp>
        INFO  [class org.jboss.as.quickstarts.cluster.hasingleton.service.ejb.SchedulerBean] (EJB default - 2) HASingletonTimer: Info=HASingleton timer @localhost <timestamp>

9. In the example, the first server instance is used as a master, if it is available. If it has failed or shutdown, any other service instance  will be used.

Undeploy the Archive
--------------------

1. Make sure you have started the JBoss WildFly Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. When you are finished testing, type this command to undeploy the archive:

        mvn wildfly:undeploy
        mvn wildfly:undeploy -Dwildfly.port=10090


Run the Quickstart in JBoss Developer Studio or Eclipse
-------------------------------------
You can also start the server and deploy the quickstarts from Eclipse using JBoss tools. For more information, see [Use JBoss Developer Studio or Eclipse to Run the Quickstarts](../README.md#useeclipse) 

Debug the Application
------------------------------------

If you want to debug the source code or look at the Javadocs of any library in the project, run either of the following commands to pull them into your local repository. The IDE should then detect them.

    mvn dependency:sources
    mvn dependency:resolve -Dclassifier=javadoc

------------------------------------
