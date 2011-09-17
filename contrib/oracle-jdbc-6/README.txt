Overview:
---------
This bundle will wrap a oracle ojdbc6 jar that comes with oracle 11g releases 11.1.0.7, 11.1.0.6, 11.2.0.2.0, 11.2.0.1.0 into a osgi bundle.
The oracle ojdbc6.jar file for a particular oracle 11g release is available here:
http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html


Steps 1)
Download the ojdbc6.jar file for the oracle server you are connecting to, into a temporary directory.

In the temporary directory issue the following maven command to install the oracle ojdbc6.jar file into the local maven repository:
mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=<oracle-version> -Dpackaging=jar -Dfile=ojdbc6.jar

where <oracle-version> is one of the following: 
11.1.0.7, 11.1.0.6, 11.2.0.2.0, 11.2.0.1.0


step 2)
Change directory into the contrib/oracle-jdbc-6 directory.
Modify the pom.xml file, and change version tag to match the <oracle-version> number used in step 1 above. 

<dependency>
    <groupId>com.oracle</groupId>
    <artifactId>ojdbc6</artifactId>
    <version>11.1.0.7</version>    <-- modify version tag, to match the <oracle-version> number used in step 1 above. 
</dependency>


step 3)
In the contrib/oracle-jdbc-6 directory issue the "mvn clean install" command to wrap the ojdbc6.jar file in a osgi bundle.