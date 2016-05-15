import sys
import plistlib

root = {}
root['Label'] = 'com.logax.server.zookeeper'
root['ProgramArguments'] = '/Users/hyunhoha/LocalCEP/kafka/bin/zookeeper-server-start.sh /Users/hyunhoha/LocalCEP/kafka/config/zookeeper.properties'.split()
root['KeepAlive'] = False
root['RunAtLoad'] = False

print plistlib.writePlist(root, sys.stdout)
