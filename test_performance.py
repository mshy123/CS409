import os
import pymongo
import time
import subprocess

result = []
num = 0
for i in range (10):
	p = subprocess.Popen(["sudo", "python", "packet_Generator2.py", "\'127.0.0.1 - - [28/May/2016:19:52:55 +0900] \"GET /login.php HTTP/1.1\" 404 207\'"],stdout=subprocess.PIPE)
	writeT = p.communicate()[0].strip().split(":")
	connection = pymongo.MongoClient("localhost",27017)
	db = connection.test
	collection = db.Rules
	time.sleep(20)
	finishT = str( collection.find().sort([('savedTime',-1)])[0].get('savedTime').get('date') ).split(":")
	writeT = float(writeT[1])*60+float(writeT[2])
	finishT = float(finishT[1])*60+float(finishT[2])
	result.append (finishT-writeT)
	print "execution time",finishT-writeT
	print "number of handled rule",collection.find().count()

print sum(result)/len(result)
