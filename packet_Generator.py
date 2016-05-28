import sys, time
from optparse import OptionParser


def generate(argv_list):
	time_interval = int(argv_list.time_interval)
			num_of_packet = int(argv_list.number)
				content_of_packet=None
					if argv_list.content:
						content_of_packet = argv_list.content
									if argv_list.file_path and not content_of_packet:
										f = open(argv_list.file_path)
														content_of_packet = f.read()
																f.close()
																	if not content_of_packet:
																		exit(-1)

																					i = 1
																						print "You can quit this program by Ctrl-C"
																							while True:
																								start = time.time()
																												f = open("PATH_OF_PACKET","a")
																														for j in range(num_of_packet):
																															if argv_list.file_path:
																																f.write(content_of_packet)
																															elif argv_list.content:
																																f.write(content_of_packet+"\n")

																																																	while time.time()-start < time_interval:
																																																		continue
																																																	f.close()
																																																								print "packet generation "+str(i)+" times finished!"
																																																										try:
																																																											i+=1
																																																										except KeyBoardInterrupt:
																																																											break

																																																										parser = OptionParser(usage = "usage: %prog [options] filename", 
																																																												version = "%prog 1.0")
																																																										parser.add_option( "-s", "--string",
																																																												action = "store",
																																																												dest = "content",
																																																												help = "input content will be written at your PATH_OF_PACKET")
																																																										parser.add_option( "-f", "--file",
																																																												action = "store",
																																																												dest = "file_path",
																																																												help = "input file will be written at your PATH_OF_PACKET")
																																																										parser.add_option( "-n", "--number",
																																																												action = "store",
																																																												dest = "number",
																																																												help = "# of repeated content")
																																																										parser.add_option( "-t", "--time",
																																																												action = "store",
																																																												dest = "time_interval",
																																																												help = "each time_interval content will be written")
																																																										(options, args) = parser.parse_args()

																																																																			generate(options)


