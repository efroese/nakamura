
Note: The pom.xml file in this directory was copied from the contrib/nyu directory,
      and then customised for CSU.

How to build the CSU Interact2 (OAE) jar file.
==============================================
Note: This example assumes nakamura is using Oracle 11g release 1 (11.1.0.7) database.
If you are using a different version of Oracle 11g, please refer to the
contrib/oracle-jdbc-6/README.txt file.

1) Obtain the correct Oracle jdbc driver, and install in local maven repository 
-------------------------------------------------------------------------------

Oracle jdbc drivers are available here:
http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html

Interact2 is using Oracle 11.1.0.7 and the Nakamura application server has JRE 1.6 installed.
The following link shows jdbc drivers available for Oracle 11.1.0.7
http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-111060-084321.html

As the application server has JRE1.6 installed, down load the ojdbc6.jar file and
and put the ojdbc6.jar file in a temporary directory. In the temporary directory issue
the following maven command to install the oracle ojdbc6.jar file into the local maven repository:

mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.1.0.7 -Dpackaging=jar -Dfile=ojdbc6.jar

2) create a bundle containing the downloaded ojdbc6.jar file
------------------------------------------------
Change directory into the contrib\oracle-jdbc-6 and issue the "mvn clean install" command.

3) create the nakamura application
----------------------------------
Change directory into the top level nakamura directory, and issue the maven command "mvn clean install"

4) Create the interact2 jar file
--------------------------------
This step will create the Interact2 jar file which includes the Oracle 11.1.0.7 jdbc driver.

Change directory to contrib\csu and issue the command "mvn clean package" which
create the interact2 jar file here: 
contrib\csu\target\au.edu.csu.interact2-1.0-SNAPSHOT.jar
 

