import sys
import plistlib

root = {}
root['Label'] = 'com.logax.server.fluentd'
root['ProgramArguments'] = '/usr/local/bin/fluentd -c /Users/hyunhoha/LocalCEP/fluent/fluent.conf'.split()
root['KeepAlive'] = False
root['RunAtLoad'] = False

print plistlib.writePlist(root, sys.stdout)
