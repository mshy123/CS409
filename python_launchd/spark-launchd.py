import sys
import plistlib

root = {}
root['Label'] = 'com.logax.server.spark'
root['ProgramArguments'] = '/Users/hyunhoha/LocalCEP/spark/bin/spark-submit --class "com.mycompany.app.App" --jars First_app-1.0-SNAPSHOT-jar-with-dependencies.jar First_app-1.0-SNAPSHOT.jar 127.0.0.1:2181 01 apache_access_topic,syslog_topic 1'.split()
root['KeepAlive'] = False
root['RunAtLoad'] = False

print plistlib.writePlist(root, sys.stdout)
