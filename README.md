# CS409 LOGAX
This project made of three project : `LogaxSencha`, `logax`, `First-app`

##Requirement
You need to have follow things, if not you have to install it.

	1. JDK (1.8 or later)
	2. Fluentd (0.12.19)
		brew install ruby
		gem install fluentd --no-ri --no-rdoc
	3. Zookeeper(3.4.8) & Kafka (0.9.1)
		brew install zookeeper
		brew install kafka
	4. Spark (1.6.1)
		brew install apache-spark
	5. MongoDB (3.2.6)
		brew install mongodb
	6. Elasticsearch (2.3.2) + Kibana (4.5.0)
		brew install elasticsearch
		brew install kibana
		fluent-gem install fluent-plugin-elasticsearch
	7. Maven (3.3.9)
	8. Ext Js (6.0.1 GPL)
		download in sencha homepage
	9. Sencha Cmd (6.1.2.15)
		download in sencha homepage

###Setting Fluentd
```
in /etc/sysctl.conf

kern.sysv.shmmax=1073741824
kern.sysv.shmmin=1
kern.sysv.shmmni=4096
kern.sysv.shmseg=32
kern.sysv.shmall=1179648
kern.maxfilesperproc=65536
kern.maxfiles=65536
net.ipv4.tcp_tw_recycle = 1
net.ipv4.tcp_tw_reuse = 1
net.ipv4.ip_local_port_range = 10240    65535

in ./.bash_profile
ulimit -n 65536
```

###How to use it
Before start run logax server, you have to type command
```
elasticsearch
kibana
zkserver start
kafka-server-start /usr/local/etc/kafka/server.properties
```
You have to add hightopic and lowtopic in the zookeeper using `kafka-topics` command with replica = 1, partitions = 1
After setting running Logax Server.
Please refer to `logax/README.md`

##Logax Server
`localhost:8080/logax`

##Kibana Server
`localhost:5601`
