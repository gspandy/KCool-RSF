#!/bin/sh
cd `dirname $0`
cp=.:${CLASSPATH}:./conf/
for loop in `ls *.jar`;do
cp=${cp}:${loop}
done
for filename in `ls ./lib/*.jar`
do
cp=${cp}:${filename}
done
cp=classes:${cp}

java -cp ${cp} -server -Xmx128m -Xms128m -XX:SurvivorRatio=3 -XX:PermSize=128m -Xss256k  -XX:+DisableExplicitGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection  -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseFastAccessorMethods -XX:SurvivorRatio=4 -XX:LargePageSizeInBytes=10m -XX:CompileThreshold=10000 com.drive.cool.DriveRouter &
echo "OK"

exit 0



