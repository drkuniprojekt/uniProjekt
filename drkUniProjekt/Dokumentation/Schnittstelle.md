#/auth
##POST /auth
Use to obtain an authentication token for the API
Request Parameter
----------------|-------------------------------|-----------
name 			|description					|type (body or URL)
login_name 		|Login Name of the User			|body
password		|Password of the User			|body
registration_id	|Registration Id for Phonegap	|body

Response example:
```json
{
	"TOKEN":"123456789"
}
```

---------------------------------------------------------------------
#/test
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
##GET /test/testID
> To be implemented