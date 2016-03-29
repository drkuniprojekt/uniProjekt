package drkprojekt.chat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.*;
import javax.websocket.CloseReason.CloseCode;
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
	private static Logger log	= LoggerFactory.getLogger(ChatEndpoint.class);
	
	@OnOpen
	public void onOpen(Session session, @PathParam("name") String clientID)
	{		
		ChatClient c	= ClientFactory.getClient(clientID);
		try
		{
			if(c != null)
			{
				c.addSession(session);	
				log.info("New Chat Client " + clientID);
				try 
				{
					session.getBasicRemote().sendText(getMessagesFromDB(clientID).toJSONString());
				} 
				catch (Exception e) 
				{
					log.error(""+ e);
					session.getBasicRemote().sendText("Unknown Error occured");
				}
			}
			else
			{
				log.info("Client tried to connect with illegal Username", clientID);				
				session.getBasicRemote().sendText("This is not a valid user");
				session.close();
			}
		}catch(IOException e)
		{
			log.error("Exception while opening Chat Connection: \n {}", e);
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
			outJSON.put("messagecontent", message);
			outJSON.put("from", clientID);
			outJSON.put("createtime", DatabaseHandler.getCurrentTimeStamp());
			
			if(recipient == null || recipient.equals("Gruppenchat"))
			{
				log.debug("Got new Broadcast-Message: " + data);
				boolean	msgRead;
				ArrayList<ChatClient>	clients = ClientFactory.getAllClients();
				
				for (ChatClient c : clients) 
				{
					if(!c.getName().equals(clientID))
					{
						msgRead	= c.sendMessage(outJSON);	
						saveMessageToDB(message, msgRead, clientID, c.getName(), true);
					}else
					{
						saveMessageToDB(message, false, clientID, c.getName(), true);
					}
					
				}								
			}
			else
			{
				log.debug("Got new Message to " + recipient + ": "+ data);
				boolean	msgRead;				
				msgRead	= ClientFactory.getClient(recipient).sendMessage(outJSON);
				saveMessageToDB(message, msgRead, clientID, recipient, false);				
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
			log.error("Unknown Error occured while executing Onmessage:\n {}", e);
			return "{\"Error\": \"Unknown Error\"}";
		}
		
	}
	
	@OnClose
	public void onClose(Session session, @PathParam("name") String clientID)
	{
		ClientFactory.getClient(clientID).deleteSession(session);
	}
	
	private void saveMessageToDB(String message, boolean read, String from, String to, boolean broadcast)
	{
		log.debug("Trying to save message to Database: " + message);
		try
		{
			DatabaseHandler db	= DatabaseHandler.getdb();
			String chatroom;
			if(broadcast)
			{
				chatroom = "1";
			}
			else
			{
				chatroom		= "" + ((JSONObject)db.executeQuery("SELECT chatroom FROM CHATROOMMAPPING WHERE USERACCOUNT = ? AND chatroom <> '1' " 
						+ "AND chatroom IN (" 
						+ "SELECT chatroom FROM CHATROOMMAPPING WHERE USERACCOUNT = ?)",
						new String[]{from, to}).get(0)).get("chatroom"); 
			}		
			
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
					
			JSONArray persons	= db.executeQuery("SELECT useraccount AS login_name FROM CHATROOMMAPPING WHERE Chatroom = ? AND useraccount <> ?", new String[]{"" + number, forUser});			
			
			if(persons.size() < 2)
			{
				room.put("name", (String)((JSONObject)persons.get(0)).get("useraccount"));
			}
			else
			{
				room.put("name", "Gruppenchat");
			}
			JSONArray msg		= db.executeQuery("SELECT TOP 50 createtime, messagecontent AS message, chatroom, message_id, useraccount AS \"from\" FROM MESSAGE WHERE Chatroom = ?", "" + number);
			int unread			= db.executeQuery("SELECT u.message FROM MESSAGESUNREAD AS u INNER JOIN MESSAGE AS m	ON m.message_id    = u.message	WHERE m.chatroom  = ? AND u.useraccount = ?", new String[]{"" + number, forUser}).size();
						
			room.put("persons", persons);
			room.put("messages", msg);
			res.add(room);			
		}
		db.executeUpdate("DELETE FROM MESSAGESUNREAD WHERE useraccount = ?", forUser);
		return res;
	}
}
