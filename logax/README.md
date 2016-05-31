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
We have total 4 collection in `test`, which are `type`, `rule`, `ruletype`, `rules`.  
Specific detail is describe in each file's comment.

##How to use it
###Add type
Go to `http://localhost:8080/logax/#dashboard`  
`Type name : String` You can't add same type name.  
`Type regex : String` is a regular expression that include in type name. You can add number of regular expression.  
`Type path : String` is a path that fluentd will be read. It must be absolute path.  
`Type priority` is a priority of the type.  
###Add rule
Go to `http://localhost:8080/logax/#rule`  
`Rule name : String`  
`Type name` and `Number : Integer` All the type has same priority. We didn't check it.  
`Duration : Integer` Time interval that check this rule. 
`Ordered` When you click the true ordered, CEP engine check each type by ordered.  
`Attribute : String` Grouping the type by this attribute. We didn't check that this attribute is in the regular expression.  
###Delete rule
Go to `http://localhost:8080/logax/#rulelist`  
Click the specific rule in the rule list.  
When you click the `delete` button, you can delete it.  
If there isn't a rule in the list, click the `refresh` button.
