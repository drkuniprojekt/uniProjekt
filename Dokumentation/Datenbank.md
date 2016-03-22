##Announcment
Nothing yet.

##Changelog
`22.03.2016` Coming soon!  
`21.03.2016` EVENTANSWER key changed | SETTING added (formerly known as option)  
`20.03.2016` AVIABLECAR changed to AVAILABLECAR in EVENTANSWER | Sequencer for EVENT_ID added   
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
EVENT_ID 		  |INT	|X |  |Use sequencer EVENT_ID.nextval for insert (auto_increment)
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
ANSWER		|BOOLEAN	|  |  |IF true (1) then user takes part
AVAILABLECAR|BOOLEAN	|  |  |IF true (1) then a car is available
EVENT	|INT	|X |X|References to EVENT(EVENT_ID)
ANSWERER     |VARCHAR(255)      |X |X |References to USER(USER_ID)

Using only foreign keys as primary key ensures that one user cannot make multiple answers for one event.

----------------------------------------------

#PHONEGAPID
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
DEVICE_ID|VARCHAR(255)	|X |  |
REGISTERTIME|TIMESTAMP	|  |  |YYYY-MM-DD HH:SS
REGISTEREDUSER|VARCHAR(255)	|X |X |References to USER(USER_ID)

----------------------------------------------

#SETTING
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
SETTING|VARCHAR(255)	|X |  |
SETTINGVALUE|BOOLEAN	|  |  |IF true (1) then setting is turned on
USERACCOUNT|VARCHAR(255)	|X |X |References to USER(USER_ID)

###Name convention for settings:  
* car >> Option if a car is general available (pre-checked box in EVENTANSWER etc.)
* gui_highcontrast >> Option if user wants app displayed with special colors
* gui_showexpirationdate >> Option if user wants to see expiration dates in his calendar
* notification_event >> Option if user wants to be notified when a new event (not alert) is created
* notification_groupchat >> Option if user wants to be notified when a new groupmessage was written
* notification_chat >> Option if user wants to be notified when a new chatmessage (one-to-one) was written

----------------------------------------------

#CHATROOM
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
CHATROOM_ID|INT	|X |  |Use sequencer CHATROOM_ID.nextval for insert (auto_increment)

###Use CHATROOM_ID 1 for group-chat!

----------------------------------------------

#CHATROOMMAPPING
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
CHATROOM|INT	|X |X |References to CHATROOM(CHATROOM_ID)
USERACCOUNT|VARCHAR(255)	|X |X |References to USER(USER_ID)

###Use CHATROOM_ID 1 for group-chat!
