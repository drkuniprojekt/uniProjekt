package drkprojekt.chat;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.websocket.Session;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.rest.PushService;

public class RealClient extends ChatClient 
{	
	private ArrayList<Session>	sessionList	= new ArrayList<Session>();	
	private ArrayList<String> 	phonegap_ids= new ArrayList<String>();
	
	public RealClient(String name, String displayName)
	{
		super(name, displayName);				
	}
	
/**
	 * Sends a message to this client. This class decides,  weather the Message is send through phonegap or websocket
	 * @param msgJSON Message JSON containing the Parameter "from" and "message"
	 * @return True, if the user read the Message, false if unread
	 */
	public boolean sendMessage(JSONObject msgJSON)
	{
		try 
		{
			if(sessionList.size() > 0) //User is online at minimum 1 device
			{
				for (Session s : sessionList) 
				{
					s.getBasicRemote().sendText(msgJSON.toJSONString());
				}
				return true;				
			}else if (phonegap_ids.size() > 0) //User is offline, but at least one Device can get a phonegap push
			{
				for (String s : phonegap_ids) 
				{
					//PushService.sendUnicastMessage("Neue Chatnachricht von " + (String)((JSONObject) msgJSON.get("data")).get("from"), s, PushService.NOTIFICATION_CHAT);
					PushService.sendUnicastMessage("Neue Chatnachricht", (String)((JSONObject) msgJSON.get("data")).get("from") + " hat dir eine Nachricht geschickt", s, PushService.NOTIFICATION_CHAT);
				}
				return false;
			}
			//The user is neigher reachable through phonegap, nor through websocket -> Just save to DB
			return false;
		} catch (Exception e) 
		{
			log.error("Could not send message to " + name + ", because an Error occured:\n ", e);
		}
		return false;
	}


	/**
 * Sends a message to this client. This class decides,  weather the Message is send through phonegap or websocket
 * @param msg Message to send
 * @return True, if the user read the Message, false if unread
 */
public boolean sendMessage(String msg)
{
//	try 
//	{
//		if(sessionList.size() > 0) //User is online at minimum 1 device
//		{
//			for (Session s : sessionList) 
//			{
//				s.getBasicRemote().sendText(msg);
//			}
//			return true;				
//		}else if (phonegap_ids.size() > 0) //User is offline, but at least one Device can get a phonegap push
//		{
//			for (String s : phonegap_ids) 
//			{
//				//PushService.sendUnicastMessage(msg, s, PushService.NOTIFICATION_CHAT);
//				PushService.sendUnicastMessage("Neue Nachricht", msg, s, PushService.NOTIFICATION_CHAT);
//			}
//			return false;
//		}
//		//The user is neigher reachable through phonegap, nor through websocket -> Just save to DB
//		return false;
//	} catch (Exception e) 
//	{
//		log.error("Could not send message to " + name + ", because an Error occured:\n ", e);
//	}
	return false;
}

public void deleteSession(Session s)
{
	sessionList.remove(s);
	s	= null;
	log.debug("User {} logged aout a device", name);
}


	// Getter/ Setter -------------------------------------------------------------------	
	public String getName()
	{
		return name;
	}
	public String getDisplayName() {
	    // TODO Auto-generated method stub
	    return displayName;
	}
	/* Please dont use this, if u want to send messages to the client. Use sendMessage instead
	public ArrayList<Session> getSessions()
	{
		return sessionList;
	}
	*/
	public void setSessionList(ArrayList<Session> session)
	{
		this.sessionList	= session;
	}
	public void addSession(Session session)
	{
		sessionList.add(session);
	}
	
	public ArrayList<String> getPhonegap_ids() 
	{
		return phonegap_ids;
	}

	public void setPhonegap_ids(ArrayList<String> phonegap_ids) 
	{
		this.phonegap_ids = phonegap_ids;
	}
	public void addPhonegap_id(String phonegap_id) 
	{
		phonegap_ids.add(phonegap_id);
	}



}
