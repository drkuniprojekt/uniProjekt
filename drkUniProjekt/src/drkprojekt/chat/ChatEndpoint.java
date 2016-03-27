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
			try 
			{
				String tmp	= getMessagesFromDB(clientID).toJSONString();
				log.debug("New Client got the following Rooms:\n {}", tmp);
				return tmp;
			} 
			catch (Exception e) 
			{
				log.error(""+ e);
				return "Unknown Error occured";
			}
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
			
		} catch (ParseException | NullPointerException e)
		{
			log.warn("Bad Request over Websocket", e);
			return "Bad Request";
		} catch( IllegalArgumentException e)
		{
			log.warn("Bad Request over Websocket", e);
			return e.getMessage();
		}
		catch (Exception e)
		{
			return "Unknown Error";
		}
		
	}
	
	@OnClose
	public void onClose(Session session, @PathParam("name") String clientID)
	{
		ClientFactory.getClient(clientID).deleteSession(session);
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
	
	private JSONArray getMessagesFromDB(String forUser) throws SQLException
	{
		JSONArray	res		= new JSONArray();
		DatabaseHandler db	= DatabaseHandler.getdb();
		JSONArray chatroom	= db.executeQuery("SELECT chatroom AS roomnumber FROM CHATROOMMAPPING WHERE USERACCOUNT = ?", forUser);
		
		for (int i = 0; i < chatroom.size(); i++)
		{
			JSONObject	room	= (JSONObject) chatroom.get(i);
			int number			= (int) room.get("chatroom"); 
					
			JSONArray persons	= db.executeQuery("SELECT useraccount AS login_name FROM CHATROOMMAPPING WHERE Chatroom = ?", "" + number);
			JSONArray msg		= db.executeQuery("SELECT * FROM MESSAGE WHERE Chatroom = ?", "" + number);
			
			room.put("persons", persons);
			room.put("messages", msg);
			res.add(room);
			
		}
		return res;
	}
}
