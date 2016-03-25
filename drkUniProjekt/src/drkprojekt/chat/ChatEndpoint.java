package drkprojekt.chat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import drkprojekt.database.DatabaseHandler;
import drkprojekt.rest.PushService;

@ServerEndpoint("/chat/{name}")
public class ChatEndpoint
{
	private static Set<ChatClient> peers	= Collections.synchronizedSet(new HashSet<ChatClient>());
	private static Logger log;
	@OnOpen
	public void onOpen(Session peer, @PathParam("name") String clientID)
	{
		peers.add(new ChatClient(peer, clientID));		
		log	= LoggerFactory.getLogger(this.getClass());
		log.info("New Client " + clientID);
		
	}
	
	@OnMessage
	public String onMessage(String msgBody, @PathParam("name") String clientID)
	{
		try
		{
			JSONObject msgJson 	= (JSONObject) new JSONParser().parse(msgBody);
			String recipient	= (String) msgJson.get("to");
			String msg			= ((String)msgJson.get("message")).trim();
			
			if(msg  == null || msg.length() == 0)
			{
				throw new IllegalArgumentException("PLease specify a message body");
			}
			
			if(recipient == null || recipient.equals("Broadcast"))
			{
				PushService.sendBroadCastMessage(msgBody);
				for (ChatClient c: peers)
				{
					c.sendMessage((String)msgJson.get("message"));
				}
				log.debug("Got new Broadcast-Message: " + msgBody);				
			}else
			{
				sendMessageTo(clientID, recipient, msg);
				
				log.debug("Got new Message to " + recipient + ": "+ msgBody);				
				
			}
			
			return "Message was send to " + recipient;
				
			
			
			
		} catch (SQLException | IOException e)
		{
			log.error("" + e);
		} catch (ParseException | IllegalArgumentException | NullPointerException e)
		{
			log.warn("Bad Request over Websocket", e);
			return "Bad Request";
		}
		return "Unknown Error";
	}
	
	private void sendMessageTo(String sender, String recipient, String msg) throws IOException, SQLException
	{
		if(!userExists(recipient))
		{
			throw new IllegalArgumentException("The given recipient was not found");
		}
		DatabaseHandler db	= DatabaseHandler.getdb();
		for (ChatClient c: peers)
		{
			//Client is online
			if(c.getName().equals(recipient))
			{
				saveMessageToDb(sender, recipient, msg, false, db);
				c.sendMessage(msg);				
				return;
			}
			else //Client is offline
			{
				saveMessageToDb(sender, recipient, msg, true, db);
				String deviceId	= (String) ((JSONObject)db.executeQuery("SELECT deviceId FROM PHONEGAPID WHERE REGISTEREDUSER = ?", recipient).get(0)).get("deviceID");
				PushService.sendUnicastMessage(msg, deviceId);
			}
			
		}		
	}
	
	private void sendBroadcast(String sender, String msg) throws IOException, SQLException
	{		
		DatabaseHandler db	= DatabaseHandler.getdb();
		for (ChatClient c: peers)
		{
			//Client is online
			if(c.getName().equals(recipient))
			{
				saveMessageToDb(sender, recipient, msg, false, db);
				c.sendMessage(msg);				
				return;
			}
			else //Client is offline
			{
				saveBroadcastToDb(sender, msg,  db);
				String deviceId	= (String) ((JSONObject)db.executeQuery("SELECT deviceId FROM PHONEGAPID WHERE REGISTEREDUSER = ?", recipient).get(0)).get("deviceID");
				PushService.sendUnicastMessage(msg, deviceId);
			}
			
		}		
	}

	private void saveBroadcastToDb(String sender, String msg, DatabaseHandler db) 
	{
		//Wie in die DB schreiben?
		
	}

	private void saveMessageToDb(String sender, String recipient, String msg, boolean unread, DatabaseHandler db) throws SQLException 
	{
		String[] args	= {sender, recipient};
		String chat		= (String) ((JSONObject)db.executeQuery("SELECT CHATROOM FROM \"CHATROOMMAPPING\" WHERE  USERACCOUNT = ? AND CHATROOM in(" +
												"SELECT CHATROOM FROM \"CHATROOMMAPPING\" WHERE USERACCOUNT = ?);", args)
									.get(0)).get("chatroom");
		
		args	= new String[]{msg, sender, chat};		
		db.executeUpdate("INSERT INTO TABLE message Values(MESSAGE_ID.nextval, CURRENT_TIMESTAMP, ?,?,?)", args);
		
		if (unread) //Tabelle muss geändert werden???????
		{
			db.executeUpdate("INSERT INTO TABLE MESSAGESUNREAD Values(?,?)", args);
		}
	}

	private boolean userExists(String login_id) throws SQLException
	{
		try
		{
			JSONArray array = DatabaseHandler.getdb().executeQuery("SELECT login_id FROM user WHERE login_id = ?", login_id);
			log.debug("User exists DB-Result: ", array.toJSONString());
			if(array.get(0) != null)
			{
				return true;
			}
			return false;
		}
		catch(IndexOutOfBoundsException e)
		{
			log.debug("Test: User exists DB-Result: IndexoutofBounds");
			return false;
		}
	}
}
