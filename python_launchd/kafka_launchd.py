import sys
import plistlib

root = {}
root['Label'] = 'com.logax.server.kafka'
root['ProgramArguments'] = '/Users/hyunhoha/LocalCEP/kafka/bin/kafka-server-start.sh /Users/hyunhoha/LocalCEP/kafka/config/server.properties'.split()
root['KeepAlive'] = False
root['RunAtLoad'] = False

print plistlib.writePlist(root, sys.stdout)
