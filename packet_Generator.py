import sys, time

def check_argv(argv_list):
    if len(argv_list) < 4:
        print "Usage : python packet_Generator.py TIME_INTERVAL NUM_OF_PACKET CONTENT_OF_PACKET"
        return;

def generate(argv_list):
    time_interval = int(argv_list[1])
    num_of_packet = int(argv_list[2])
    content_of_packet = argv_list[3]
    i = 1
    print "You can quit this program by Ctrl-C"
    while True:
        start = time.time()
        f = open("/var/log/apache2/access_log","a")
        for j in range(num_of_packet):
            f.write(content_of_packet+"\n")
        while time.time()-start < time_interval:
            continue
        f.close()
        print "packet generation "+str(i)+" times finished!"
        try:
            i+=1
        except KeyBoardInterrupt:
            break

argv_list = sys.argv
check_argv(argv_list)
generate(argv_list)
