##Changelog
`05.04.2016` STORAGE QUANTITY changed to double  
`05.04.2016` STORAGE created  
`04.04.2016` USERGROUP in EVENT deleted | New table ALERTGROUP added for mapping Alerts to the Usergroups  
`01.04.2016` USERGROUP in EVENT added (because of changerequest)  
`31.03.2016` Further settings added (alert-groups)  
`29.03.2016` DELETED flag in USER added  
`24.03.2016` ZIP is now VARCHAR(5) (formerly INT)  
`22.03.2016` CHATROOM, CHATROOMMAPPING, MESSAGE, MESSAGESUNREAD created  
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
ADMINROLE     |BOOLEAN      |  |  |IF true (1) then the user is an admin
DELETED     |BOOLEAN      |  |  |IF true (1) then the user is deleted

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
ZIP		|VARCHAR(5)	|  |  |
TOWN	|VARCHAR(255)	|  |  |
CREATOR     |VARCHAR(255)    |  |X  |References to USER(LOGIN_ID)

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
ANSWERER     |VARCHAR(255)      |X |X |References to USER(LOGIN_ID)

Using only foreign keys as primary key ensures that one user cannot make multiple answers for one event.

----------------------------------------------

#PHONEGAPID
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
DEVICE_ID|VARCHAR(255)	|X |  |
REGISTERTIME|TIMESTAMP	|  |  |YYYY-MM-DD HH:SS
REGISTEREDUSER|VARCHAR(255)	|X |X |References to USER(LOGIN_ID)

----------------------------------------------

#SETTING
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
SETTING|VARCHAR(255)	|X |  |
SETTINGVALUE|BOOLEAN	|  |  |IF true (1) then setting is turned on
USERACCOUNT|VARCHAR(255)	|X |X |References to USER(LOGIN_ID)

###Name convention for settings:  
* car >> Option if a car is general available (pre-checked box in EVENTANSWER etc.)
* gui_highcontrast >> Option if user wants app displayed with special colors
* gui_showexpirationdate >> Option if user wants to see expiration dates in his calendar
* notification_event >> Option if user wants to be notified when a new event (not alert) is created
* notification_groupchat >> Option if user wants to be notified when a new groupmessage was written
* notification_chat >> Option if user wants to be notified when a new chatmessage (one-to-one) was written
* notification_alert_segv >> Option to assign user into alert-group SEG-V (Verpflegung)
* notification_alert_segs >> Option to assign user into alert-group SEG-S (Sanitäter)
* notification_alert_sbf >> Option to assign user into alert-group SBF (Sanitätsbereitschaft Feuerwehr)
* notification_alert_ov >> Option to assign user into alert-group OV (Ortsverein)

----------------------------------------------

#ALERTGROUP

Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
EVENT_ID|INT	|X |X |
USERGROUP|VARCHAR(255)	|X |  |Use groups described in SETTING (notification_alert_segv etc.)

Used to send push notifications just to specific usergroups.

----------------------------------------------

#CHATROOM
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
CHATROOM_ID|INT	|X |  |Use sequencer CHATROOM_ID.nextval for insert (auto_increment)

Use CHATROOM_ID 1 for group-chat!

----------------------------------------------

#CHATROOMMAPPING
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
CHATROOM|INT	|X |X |References to CHATROOM(CHATROOM_ID)
USERACCOUNT|VARCHAR(255)	|X |X |References to USER(LOGIN_ID)

###Use CHATROOM_ID 1 for group-chat!

----------------------------------------------

#MESSAGE
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
MESSAGE_ID|INT	|X |  |Use sequencer MESSAGE_ID.nextval for insert (auto_increment)
CREATETIME|TIMESTAMP	|  |  |YYYY-MM-DD HH:SS
MESSAGECONTENT |VARCHAR(5000)	|  |  |
USERACCOUNT |VARCHAR(255)	|  |X |References to USER(LOGIN_ID)
CHATROOM |INT	|  |X |References to CHATROOM(CHATROOM_ID)

----------------------------------------------

#MESSAGESUNREAD  
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
MESSAGE |INT	|X  |X |References to MESSAGE(MESSAGE_ID)
USERACCOUNT |VARCHAR(255)	|X |X |References to USER(LOGIN_ID)

----------------------------------------------

#STORAGE  
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
ITEM_ID |INT	|X  |  |Use sequencer ITEM_ID.nextval for insert (auto_increment)
ITEMNAME |VARCHAR(255)	|  |  |
QUANTITY |DOUBLE	|  |  |
QUANTITYUNIT |VARCHAR(255)	|  |  |
EXPIRATIONDATE |TIMESTAMP	|  |  |YYYY-MM-DD HH:SS
EQUIPMENTITEM |BOOLEAN	|  |  |IF true (1) then item is an equipment
