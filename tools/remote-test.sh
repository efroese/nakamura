#!/bin/sh
# NAKAMURA_BUILD_URL=http://builds.sakaiproject.org:8080/view/OAE/job/oae-nakamura/1402
# VERSION=1.1-SNAPSHOT
# KEYPAIR_NAME=choco-pair
# export EC2_HOME=/var/lib/jenkins/.ec2
# KEYPAIR_PATH=$EC2_HOME
# export JAVA_HOME=/usr/lib/jvm/jre-1.6.0-openjdk
# export EC2_CERT=$EC2_HOME/cert-7OBIC2MORWW4RYYF5V5T43POCZBVWGY4.pem
# export EC2_PRIVATE_KEY=$EC2_HOME/pk-7OBIC2MORWW4RYYF5V5T43POCZBVWGY4.pem
: ${NAKAMURA_BUILD_URL:?"Cannot proceed without NAKAMURA_BUILD_URL."}
: ${VERSION:?"Cannot proceed without VERSION of the nakamura build."}
: ${KEYPAIR_NAME:?"Cannot proceed without KEYPAIR_NAME for Amazon AWS."}
: ${KEYPAIR_PATH:?"Cannot proceed without KEYPAIR_PATH for Amazon AWS."}
: ${EC2_HOME:?"Cannot proceed without EC2_HOME for Amazon AWS."}
: ${JAVA_HOME:?"Cannot proceed without JAVA_HOME for Amazon AWS."}
: ${EC2_CERT:?"Cannot proceed without EC2_CERT for Amazon AWS."}
: ${EC2_PRIVATE_KEY:?"Cannot proceed without EC2_PRIVATE_KEY for Amazon AWS."}
export JAVA_HOME=$JAVA_HOME
export EC2_HOME=$EC2_HOME
export EC2_CERT=$EC2_CERT
export EC2_PRIVATE_KEY=$EC2_PRIVATE_KEY
KEYPAIR=$KEYPAIR_PATH/$KEYPAIR_NAME
DOWNLOAD_ARTIFACT=org.sakaiproject.nakamura.app-$VERSION.jar
DOWNLOAD_URL=$NAKAMURA_BUILD_URL/org.sakaiproject.nakamura'\$'org.sakaiproject.nakamura.app/artifact/org.sakaiproject.nakamura/org.sakaiproject.nakamura.app/$VERSION/$DOWNLOAD_ARTIFACT
STARTUP_OPTIONS='-XX:PermSize=64m -XX:MaxPermSize=128m -Xmx512m -Dfile.encoding=UTF8 -Dorg.apache.sling.launcher.system.packages=,sun.misc'

INSTANCE_ID=`$EC2_HOME/bin/ec2-run-instances ami-8c1fece5 -k $KEYPAIR_NAME | egrep "pending" | cut -f2`
echo Amazon instance $INSTANCE_ID starting up...
# sleep until hostname is available
while [ `$EC2_HOME/bin/ec2din | grep -c running` -lt 1 ]
do
    sleep 5
done
EC2_HOST=`$EC2_HOME/bin/ec2din | egrep "INSTANCE[[:blank:]]$INSTANCE_ID" | cut -f4`

# sleep until ssh is available
while [ `ssh -i $KEYPAIR -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -q -q -o BatchMode=yes ec2-user@$EC2_HOST "echo ready" | grep -c ready` -lt 1 ]
do
        sleep 5
done
echo Provisioning host $EC2_HOST...
ssh -q -q -t -i $KEYPAIR -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$EC2_HOST 'sudo yum -y install gcc make ruby-devel rubygems curl-devel && sudo gem install json curb --no-ri --no-rdoc'
ssh -q -q -t -i $KEYPAIR -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$EC2_HOST 'wget https://github.com/sakaiproject/nakamura/tarball/master && tar xzf master'
echo Downloading $DOWNLOAD_URL
ssh -q -q -t -i $KEYPAIR -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$EC2_HOST 'wget '$DOWNLOAD_URL''

echo Nakamura starting up...
ssh -i $KEYPAIR -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$EC2_HOST 'java '$STARTUP_OPTIONS' -jar '$DOWNLOAD_ARTIFACT' -p 8080 -f - 1> run.log 2>&1 &'
# use curl to see if nakamura is answering requests yet
while [ `curl -sL -w "%{http_code} %{url_effective}\\n" "http://$EC2_HOST:8080/var/ux-version/ux-version.json" -o /dev/null | grep -c 200` -lt 1 ]
do
    sleep 10
done

echo Executing tests...
ssh -q -q -i $KEYPAIR -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@$EC2_HOST 'cd sakaiproject-nakamura*;./tools/runalltests.rb'

echo Decommissioning Amazon instance $INSTANCE_ID...
$EC2_HOME/bin/ec2kill $INSTANCE_ID
