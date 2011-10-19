#/bin/sh
set -o nounset
set -o errexit
cversion=$1
nversion=$2

echo "Moving from $cversion-SNAPSHOT to $nversion "
listofpoms=`find . -exec grep -l SNAPSHOT {} \;| egrep -v ".git|do_release.sh|.classpath|target|binary|uxloader/src/main/resources|last-release|cachedir|sling|tools|sandbox/drools/drools-repository/src"`

listofpomswithversion=`grep -l $cversion-SNAPSHOT $listofpoms`
set +o errexit
echo "Incrementing version"
for i in $listofpomswithversion
do
  sed "s/$cversion-SNAPSHOT/$nversion/" $i > $i.new
  mv $i.new $i
done
