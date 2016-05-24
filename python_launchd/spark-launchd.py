import sys
import plistlib

root = {}
root['Label'] = 'com.logax.server.spark'
root['ProgramArguments'] = '/usr/local/Cellar/apache-spark/1.6.1/bin/spark-submit --class "com.mycompany.app.App" --jars /Users/hyunhoha/LocalCEP/spark/First_app-1.0-SNAPSHOT-jar-with-dependencies.jar /Users/hyunhoha/LocalCEP/spark/First_app-1.0-SNAPSHOT.jar 127.0.0.1:2181 01 hightopic,lowtopic 1'.split()
root['KeepAlive'] = False
root['RunAtLoad'] = False

print plistlib.writePlist(root, sys.stdout)
