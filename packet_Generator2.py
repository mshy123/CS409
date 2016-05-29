import sys

arg_list = sys.argv

f = open("/var/log/apache2/access_log","a")
for i in range(100):
	f.write(arg_list[1]+"\n")
f.close()
