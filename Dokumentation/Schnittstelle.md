#/alarm
##GET /alarm
Get the current alarm or Error 204, if there is no current alarm.
###Response
```json
"{
	„description“:“<description>“,
	„required_things“:“<rt>“,
	„quantitymembers“:“<qm>“,
	„street“:“<street>“,
	„housenumber“:“<number>“,
	„zip“:“<plz>“ ,
	„town“:“<town>“ 
}"
```

##POST /alarm
Create a new alarm
###Request
name 		|description			|type (body or URL)
----------------|-------------------------------|-----------
description 	|Short Name			|body
reqired_things	|Stuff required for the alarm	|body
quantitymmbers	|Amount of members needed (min)	|body
street		|operating place street		|body
housenumber	|operating place housenumber	|body
zip		|operating place zip		|body
creator		|Admin created the account	|body

##PUT /alarm
Modify the current alarm
###Request
name 		|description			|type (body or URL)
----------------|-------------------------------|-----------
description 	|Short Name			|body
reqired_things	|Stuff required for the alarm	|body
quantitymmbers	|Amount of members needed (min)	|body
street		|operating place street		|body
housenumber	|operating place housenumber	|body
zip		|operating place zip		|body
creator		|Admin created the account	|body

##DELETE /alarm
Delete the current alarm

---------------------------------------------------------------------
#/response
##GET /response
Returns a list of responses to the current alarm
###Response
```json
"
[
	{
		„answer“:“<true/false>“,
		„availablecar“:“<true/false>“,
		„answerer“:“<user_id>“
	}, ...
] "
```

##POST /response
Create a response for the current alarm
###Request
name 		|description			|type (body or URL)
----------------|-------------------------------|-----------
answer 		|Answer (true/false)		|body
availablecar	|Got a car (true/false)		|body
answerer	|User ID of te answerer		|body


---------------------------------------------------------------------
#/authentication
##POST /authentfication
Use to obtain an authentication token for the API
Request Parameter

name 		|description			|type (body or URL)
----------------|-------------------------------|-----------
login_id 	|Login Name of the User		|body
userpassword	|Password of the User		|body
device_id	|Registration Id for Phonegap	|body

Response example:
```json
{
	"token":"123456789"
}
```

##DELETE /authentication/{token}
Use to delete your authentication token (logout)

---------------------------------------------------------------------
#/test > not productive !
##GET /test
Executes some test code and returns the result
Response example:
```json
[
	{	
		"NAME":"123"
	},
	{	
		"NAME":"456"
	}
]
```
---------------------------------------------------------------------
#/user
##GET /user/{id}
Get user with the given id
###Response:
```json
"
{ 
	display_name“:“<username>“,
 	„login_id“:““,
 	„role“:““, 
} "
```

##GET /user
Get list of all user
###Response:
Array of users

##PUT /user/id
Noch zu klären !

##POST /user
Create a new user
###Request
name 		|description			|type (body or URL)
----------------|-------------------------------|-----------
login_id 	|Login Name of the User		|body
display_name	|Name to be shown in the Chat	|body
role		|Role				|body

##DELETE /user/{id}
Delete the user with the given ID
