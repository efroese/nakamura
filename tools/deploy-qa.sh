#!/bin/sh
# NAKAMURA_BUILD_URL=http://builds.sakaiproject.org:8080/view/OAE/job/oae-nakamura/1398
# VERSION=1.1-SNAPSHOT
: ${NAKAMURA_BUILD_URL:?"Cannot proceed without NAKAMURA_BUILD_URL."}
: ${VERSION:?"Cannot proceed without VERSION of the nakamura build."}
DOWNLOAD_ARTIFACT=org.sakaiproject.nakamura.app-$VERSION.jar
DOWNLOAD_URL=$NAKAMURA_BUILD_URL/org.sakaiproject.nakamura'\$'org.sakaiproject.nakamura.app/artifact/org.sakaiproject.nakamura/org.sakaiproject.nakamura.app/$VERSION/$DOWNLOAD_ARTIFACT
ssh sakai@qa20-us.sakaiproject.org 'cd ready-release && rm '$DOWNLOAD_ARTIFACT' && echo Downloading '$DOWNLOAD_URL'... && wget --quiet '$DOWNLOAD_URL' && ls -lh '$DOWNLOAD_ARTIFACT''
