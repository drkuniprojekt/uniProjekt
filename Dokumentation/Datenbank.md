#Announcment
Nothing yet.

----------------------------------------------

#USER
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
LOGIN_ID 		  |VARCHAR(255)	|X |  |Wird zum einloggen benötigt
DISPLAYNAME		|VARCHAR(255)	|  |  |
USERPASSWORD	|VARCHAR(255)	|  |  |Wird zum einloggen benötigt
ADMINROLE     |BOOLEAN      |  |  |

----------------------------------------------

#EVENT
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
EVENT_ID 		  |INT	|X |  |
DISPLAYNAME		|VARCHAR(255)	|  |  |
USERPASSWORD	|VARCHAR(255)	|  |  |
ADMINROLE     |BOOLEAN      |  |  |
DISPLAYNAME		|VARCHAR(255)	|  |  |
USERPASSWORD	|VARCHAR(255)	|  |  |
ADMINROLE     |BOOLEAN      |  |  |
DISPLAYNAME		|VARCHAR(255)	|  |  |
USERPASSWORD	|VARCHAR(255)	|  |  |
ADMINROLE     |BOOLEAN      |  |  |

----------------------------------------------

#EVENTANSWER
Attributname 		|Type    |Key     |Foreign Key  |Notes
------|------|------|------|------|
EVENTANSWER_ID|INT	|X |  |
ANSWER		|BOOLEAN	|  |  |IF true (1) then user takes part
AVIABLECAR|BOOLEAN	|  |  |IF true (1) then a car is aviable
EVENT	|INT	|  |X|References to EVENT(EVENT_ID)
ANSWERER     |VARCHAR(255)      |  |X |References to USER(USER_ID)
