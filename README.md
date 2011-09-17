This is the master branch of Nakamura maintained by the Sakai Foundation. 

This is the Sling-based Sakai OAE kernel that uses OSGi.


To run:

    mvn clean install
    . tools/version
    java -jar app/target/org.sakaiproject.nakamura.app-${K2VERSION}.jar

This will start Felix configured with Sling and the Sakai nakamura bundles.

You will find the bundles under ./bundles and some libraries under ./libraries.


