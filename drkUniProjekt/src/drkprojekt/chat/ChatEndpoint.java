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

import org.joda.time.DateTime;
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

		ChatClient c = ClientFactory.getClient(clientID);
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
					log.error("Unkown Error occured while Opening Socket:\n {} ", e);
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
	public String onMessage(String data, Session session, @PathParam("name") String clientID)
	{
		try
		{
			JSONObject msgJson 	= (JSONObject) new JSONParser().parse(data);
			String recipient	= (String) msgJson.get("to");
			
			if(msgJson.get("requestType") != null && msgJson.get("requestType").equals("init"))
			{
			   log.debug("init");
			   return getMessagesFromDB(clientID).toJSONString();
			}
			else if(msgJson.get("requestType") != null && msgJson.get("requestType").equals("loadData"))
			{
			    log.debug("loadData");			    
			    int message_id = Integer.parseInt((String)msgJson.get("lastMessage_id"));
			    
			    return getMessagesFromDB(clientID, recipient, message_id).toJSONString();
			    //ClientFactory.getClient(clientID).sendMessage(outJSON);
			}
			else
			{
        			    
        			log.debug("sendMessage");
        			String message		= ((String) msgJson.get("message")).trim();
        			
        			
        			if(message.length() == 0)
        			{
        				throw new IllegalArgumentException("Please Provide a non-empty Message");
        			}
        			JSONObject outJSON = new JSONObject();
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
        		}
			
			
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
			log.error("SQL Error while Saving Message to DB:\n ",e);
		}
	}
	
	/**
	 * @param clientID
	 * @param recipient
	 * @param lastMessageTimeStamp
	 * @return
	 */
	private JSONObject getMessagesFromDB(String forUser, String chatpartner, int message_id) throws SQLException {
	    JSONObject out = new JSONObject();
	    DatabaseHandler db	= DatabaseHandler.getdb();
	    
	    int chatroomID;
	    if(chatpartner == null || chatpartner.equals("Gruppenchat")){
		chatroomID = 1;
	    }
	    else
	    {
		    JSONArray chatroom	= db.executeQuery("SELECT a.chatroom FROM chatroommapping a INNER JOIN chatroommapping b ON a.chatroom=b.chatroom WHERE a.useraccount = ? AND b.useraccount = ? AND a.chatroom != '1'", new String[]{"" + forUser, chatpartner});
		    if(chatroom.size() != 1){
			log.debug("Chatroom: " + chatroom);
			log.error("More or less than one chatroom for selected users!");
			throw new SQLException("More or less than one chatroom for selected users!");
		    }
		    else
		    {
			chatroomID = (int) ((JSONObject)chatroom.get(0)).get("chatroom");
		    }
	    }
	    
	    JSONArray msg = db.executeQuery("SELECT TOP 50 createtime, messagecontent AS message, chatroom, message_id, useraccount AS"
	    	+ " \"from\" FROM MESSAGE WHERE Chatroom = ? AND message_id < ?", new String[]{"" + chatroomID, ""+message_id});
	    out.put("messages", msg);
	    return out;
	}
	
	
	private JSONArray getMessagesFromDB(String forUser) throws SQLException
	{
		JSONArray	res		= new JSONArray();
		DatabaseHandler db	= DatabaseHandler.getdb();
		JSONArray chatroom	= db.executeQuery("SELECT chatroom AS roomnumber FROM CHATROOMMAPPING WHERE USERACCOUNT = ?", forUser);
		
		log.debug("Chatroom size: " +chatroom.size());
		
		for (int i = 0; i < chatroom.size(); i++)
		{
		    	
			JSONObject	room	= (JSONObject) chatroom.get(i);
			int number			= (int) room.get("chatroom"); 	
			JSONArray persons	= db.executeQuery("SELECT useraccount AS login_name FROM CHATROOMMAPPING WHERE Chatroom = ? AND useraccount <> ?", new String[]{"" + number, forUser});			

			if(persons.size() == 1)
			{
				room.put("name", (String)((JSONObject)persons.get(0)).get("useraccount"));
			}
			else if( persons.size() > 1) // Ignore room if theres less than 1 other person
			{
				room.put("name", "Gruppenchat");
			}
			JSONArray msg		= db.executeQuery("SELECT TOP 50 createtime, messagecontent, chatroom, message_id, useraccount FROM MESSAGE WHERE Chatroom = ?", "" + number);
			int unread			= db.executeQuery("SELECT u.message FROM MESSAGESUNREAD AS u INNER JOIN MESSAGE AS m	ON m.message_id    = u.message	WHERE m.chatroom  = ? AND u.useraccount = ?", new String[]{"" + number, forUser}).size();
						
			room.put("persons", persons);
			room.put("messages", msg);
			room.put("unreadMessages", unread);
			res.add(room);			
		}
		db.executeUpdate("DELETE FROM MESSAGESUNREAD WHERE useraccount = ?", forUser);
		return res;
	}
}
