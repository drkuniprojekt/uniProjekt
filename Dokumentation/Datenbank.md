##Announcment
Nothing yet.

##Changelog
`20.03.2016` Coming soon!   
`18.03.2016` Initial version created   

----------------------------------------------

#USER
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
LOGIN_ID 		  |VARCHAR(255)	|X |  |Needed for Log-On
DISPLAYNAME		|VARCHAR(255)	|  |  |
USERPASSWORD	|VARCHAR(255)	|  |  |Needed for Log-On
ADMINROLE     |BOOLEAN      |  |  |

----------------------------------------------

#EVENT
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
EVENT_ID 		  |INT	|X |  |
NAME		|VARCHAR(255)	|  |  |
DESCRIPTION	|VARCHAR(5000)	|  |  |
ALERTEVENT     |BOOLEAN      |  |  |IF true (1) then the event is an alert
REQUIREDTHINGS		|VARCHAR(5000)	|  |  |Plain text, describing what is needed for the event
QUANTITYMEMBERS	|INT	|  |  |
STARTTIME     |TIMESTAMP      |  |  |YYYY-MM-DD HH:SS
ENDTIME		|TIMESTAMP	|  |  |YYYY-MM-DD HH:SS
STREET	|VARCHAR(255)	|  |  |
HOUSENUMBER     |VARCHAR(10)      |  |  |
ZIP		|INT	|  |  |
TOWN	|VARCHAR(255)	|  |  |
CREATOR     |VARCHAR(255)    |  |X  |References to USER(USER_ID)

Example SQL statement, to insert a timestamp  
```
INSERT INTO "test" VALUES(  
    TO_TIMESTAMP('2011-05-11 12:59.99','YYYY-MM-DD HH:SS.FF2') 
);
```

----------------------------------------------

#EVENTANSWER
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
EVENTANSWER_ID|INT	|X |  |
ANSWER		|BOOLEAN	|  |  |IF true (1) then user takes part
AVIABLECAR|BOOLEAN	|  |  |IF true (1) then a car is aviable
EVENT	|INT	|  |X|References to EVENT(EVENT_ID)
ANSWERER     |VARCHAR(255)      |  |X |References to USER(USER_ID)

----------------------------------------------

#PHONEGAPID
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
DEVICE_ID|VARCHAR(255)	|X |  |
REGISTERTIME|TIMESTAMP	|  |  |YYYY-MM-DD HH:SS
REGISTREDUSER|VARCHAR(255)	|X |X |References to USER(USER_ID)

