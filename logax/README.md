# logax

Web interface for logax

##Settings
```
vim src/main/java/com/logax/server/LogaxController.java

change the fluentpath where fluent.conf file located.
change the rulepath where Rule.json file located.
change the pos_file where the fluent.conf file located.
```

##Compile and Run
Before you compile this project, you must first compile the `LogaxSencha` by `make buildA`  
Then the sencha build property move to this project. After this, compile this project by
`make build`  
Then, run by
`make run`  
You can connect logax server `http://127.0.0.1:8080/logax`

##Design
All the request from the sencha will go to `LogaxController` by `localhost:8080/api/{command}`  
All the result will be update the mongoDB by `DBClient`  
We have total 4 collection in `test`, which are `type`, `rule`, 'ruletype`, `rules`.  
Specific detail is describe in each file's comment.
