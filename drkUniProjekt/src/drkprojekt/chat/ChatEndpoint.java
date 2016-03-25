package drkprojekt.chat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
//	private static Set<ChatClient> peers	= Collections.synchronizedSet(new HashSet<ChatClient>());
	private static Logger log	= LoggerFactory.getLogger(ChatEndpoint.class);
	@OnOpen
	public String onOpen(Session session, @PathParam("name") String clientID)
	{		
		ChatClient c	= ClientFactory.getClient(clientID);
		
		if(c != null)
		{
			c.addSession(session);	
			log.info("New Chat Client " + clientID);
			return "Registerd";
		}
		else
		{
			log.info("Client tried to connect with illegal Username", clientID);
			try 
			{
				session.getBasicRemote().sendText("This is not a valid user");
				session.close();
			} catch (IOException e) 
			{			
			}
			return "This is not a valid user";
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@OnMessage
	public String onMessage(String data, @PathParam("name") String clientID)
	{
		try
		{
			JSONObject msgJson 	= (JSONObject) new JSONParser().parse(data);
			String recipient	= (String) msgJson.get("to");
			String message		= ((String) msgJson.get("message")).trim();
			
			
			if(message.length() == 0)
			{
				throw new IllegalArgumentException("Please Provide a non-empty Message");
			}
			JSONObject outJSON	= new JSONObject();
			outJSON.put("message", message);
			outJSON.put("from", clientID);
			
			if(recipient == null || recipient.equals("Broadcast"))
			{
				log.debug("Got new Broadcast-Message: " + data);
				boolean	msgRead;
				ArrayList<ChatClient>	clients = ClientFactory.getAllClients();
				
				for (ChatClient c : clients) 
				{
					msgRead	= c.sendMessage(outJSON);
					saveMessageToDB(message, msgRead, clientID, c.getName());
				}								
			}
			else
			{
				log.debug("Got new Message to " + recipient + ": "+ data);
				boolean	msgRead;
				msgRead	= ClientFactory.getClient(recipient).sendMessage(outJSON);
				saveMessageToDB(message, msgRead, clientID, recipient);				
			}	
			return outJSON.toJSONString();
			
		} catch (ParseException | IllegalArgumentException | NullPointerException e)
		{
			log.warn("Bad Request over Websocket", e);
			return "Bad Request";
		} catch (Exception e)
		{
			return "Unknown Error";
		}
		
	}
	
	private void saveMessageToDB(String message, boolean read, String from, String to)
	{
		log.debug("Trying to save message to Database: " + message);
		try
		{
			DatabaseHandler db	= DatabaseHandler.getdb();
			String chatroom		= "" + ((JSONObject)db.executeQuery("SELECT chatroom FROM CHATROOMMAPPING WHERE USERACCOUNT = ? AND chatroom <> '1' " 
												+ "AND chatroom IN (" 
												+ "SELECT chatroom FROM CHATROOMMAPPING WHERE USERACCOUNT = ?)",
												new String[]{from, to}).get(0)).get("chatroom"); 
			
			db.executeUpdate("INSERT INTO MESSAGE VALUES(MESSAGE_ID.NEXTVAL, CURRENT_TIMESTAMP, ?,?,?)", 
								new String[]{message, from, chatroom});
			
			if(!read)
			{
				db.executeUpdate("INSERT INTO MESSAGESUNREAD VALUES(MESSAGE_ID.CURRVAL, ?)", to);
			}
		} catch (SQLException e) 
		{
			log.error("SQL Error while SAving Message to DB:\n ",e);
		}
	}
	
//	private boolean userExists(String login_id) throws SQLException
//	{
//		try
//		{
//			JSONArray array = DatabaseHandler.getdb().executeQuery("SELECT login_id FROM user WHERE login_id = ?", login_id);
//			log.debug("User exists DB-Result: ", array.toJSONString());
//			if(array.get(0) != null)
//			{
//				return true;
//			}
//			return false;
//		}
//		catch(IndexOutOfBoundsException e)
//		{
//			log.debug("Test: User exists DB-Result: IndexoutofBounds");
//			return false;
//		}
//	}
}
