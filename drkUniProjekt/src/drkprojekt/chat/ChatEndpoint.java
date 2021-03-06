package drkprojekt.chat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import drkprojekt.auth.AuthHelper;
import drkprojekt.database.DatabaseHandler;

@ServerEndpoint("/chat/{name}")
public class ChatEndpoint
{
	private static Logger log	= LoggerFactory.getLogger(ChatEndpoint.class);
	
	@OnOpen
	public void onOpen(Session session, @PathParam("name") String clientID) throws SQLException
	{

		ChatClient c = ClientFactory.getClient(clientID);
		try
		{
			if(c != null)
			{
				c.addSession(session);	
				log.info("New Chat Client " + clientID);
//				try 
//				{
//					session.getBasicRemote().sendText(getMessagesFromDB(clientID).toJSONString());
//				} 
//				catch (Exception e) 
//				{
//					log.error("Unkown Error occured while Opening Socket:\n {} ", e);
//					session.getBasicRemote().sendText("Unknown Error occured");
//				}
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
			JSONObject answer	= new JSONObject();
			String recipient	= (String) msgJson.get("to");
			String token 		= (String) msgJson.get("token");
			if(!AuthHelper.userIsRegistered(token,clientID)){
				log.debug("Usertoken for this user not registered or null");
				JSONObject out = new JSONObject();
				out.put("Error", "Nutzer nicht authentifiziert");
				return  out.toJSONString();
			    
			}
			
			if(msgJson.get("requestType") != null && msgJson.get("requestType").equals("init"))
			{
			   log.debug("init");
			   answer.put("data", getMessagesFromDB(clientID));
			   answer.put("requestType", msgJson.get("requestType"));
			}
			else if(msgJson.get("requestType") != null && msgJson.get("requestType").equals("loadData"))
			{
			    log.debug("loadData");			    
			    int message_id = (int)(long)msgJson.get("lastMessage_id");
			    
			    answer.put("data", getMessagesFromDB(clientID, recipient, message_id));
			    answer.put("requestType", msgJson.get("requestType"));
			}
			else
			{        			    
			    log.debug("sendMessage");
			    answer =  handleMessage(data, clientID, msgJson, recipient);
			    log.debug("HandleMessage: " + answer.toJSONString());
			}
			
			
			return answer.toJSONString();
			
		} catch (ParseException | NullPointerException e)
		{
			log.warn("Bad Request over Websocket", e);
			return "{\"Error\": \"Bad Request\"}";
		} catch( IllegalArgumentException e)
		{
			log.warn("Bad Request over Websocket", e);
			return "{\"Error\": \""+e.getMessage()+"\"}";
		}
		catch (Exception e)
		{
			log.error("Unknown Error occured while executing Onmessage:\n {}", e);
			return "{\"Error\": \"Unknown Error\"}";
		}
		
	}

	private JSONObject handleMessage(String data, String clientID,
			JSONObject msgJson, String recipient) throws SQLException {
		String message		= ((String) msgJson.get("message")).trim();
		
		
		if(message.length() == 0)
		{
			throw new IllegalArgumentException("Please Provide a non-empty Message");
		}
		JSONObject outJSON = new JSONObject();
		outJSON.put("messagecontent", message);       			
		outJSON.put("from", ClientFactory.getClient(clientID).getDisplayName());
		outJSON.put("createtime", DatabaseHandler.getCurrentTimeStamp());
		outJSON.put("toroom",msgJson.get("toroom"));
		outJSON.put("useraccount", ClientFactory.getClient(clientID).getName());
		JSONObject returnJSON = new JSONObject();
		returnJSON.put("data", outJSON);
		returnJSON.put("requestType", msgJson.get("requestType"));
		
		if(recipient != null && recipient.equals("Gruppenchat"))
		{
			log.debug("Got new Broadcast-Message: " + data);
			saveBroadcastMessageToDB(message, clientID);
			processBroadcastMessage(returnJSON, clientID);        												
		}
		else
		{
			log.debug("Got new Message to " + recipient + ": "+ data);
			boolean	msgRead;				
			msgRead	= ClientFactory.getClient(recipient).sendMessage(returnJSON);
			saveMessageToDB(message, msgRead, clientID, recipient, false);				
		}
		
		return returnJSON;
	}
	




	/**
	 * @param outJSON
	 * @param clientID
	 */
	private void processBroadcastMessage(JSONObject outJSON, String clientID) {
		boolean	msgRead;
		ArrayList<ChatClient>	clients = ClientFactory.getAllClients();
		for (ChatClient c : clients) 
		{
		    	if (c instanceof RealClient && !c.getName().equals(clientID))
		    	{
		    	    	msgRead	= c.sendMessage(outJSON);
		    	
        			if(!msgRead)
        			{
        			    	try
        			    	{
        			    	    	DatabaseHandler.getdb().executeUpdate("INSERT INTO MESSAGESUNREAD VALUES(MESSAGE_ID.CURRVAL, ?)", c.getName());
            			
        			    	} catch (SQLException e) 
        			    	{
        			    	    	log.error("SQL Error while saving unread message to DB:\n ",e);
        			    	}
        			}
		    	}
    		}
	    
	}

	@OnClose
	public void onClose(Session session, @PathParam("name") String clientID) throws SQLException
	{
		ClientFactory.getClient(clientID).deleteSession(session);
	}
	
	private void saveBroadcastMessageToDB(String message, String clientID) {
	    saveMessageToDB(message, true, clientID, "", true);
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
	    if(chatpartner == null || chatpartner.equals("Gruppenchat"))
	    {
	    	chatroomID = 1;
	    }
	    else
	    {
		    JSONArray chatroom	= db.executeQuery("SELECT a.chatroom FROM chatroommapping a INNER JOIN chatroommapping b ON a.chatroom=b.chatroom WHERE a.useraccount = ? AND b.useraccount = ? AND a.chatroom != '1'", new String[]{"" + forUser, chatpartner});
		    if(chatroom.size() != 1){
			log.debug("Chatroom: " + chatroom.toJSONString());
			log.error("More or less than one chatroom for selected users!");
			throw new SQLException("More or less than one chatroom for selected users!");
		    }
		    else
		    {
			chatroomID = (int) ((JSONObject)chatroom.get(0)).get("chatroom");
		    }
	    }

	    out.put("toroom",chatroomID);

	    JSONArray msg = db.executeQuery("SELECT * FROM (SELECT TOP 50 createtime, messagecontent, chatroom, message_id, useraccount, displayname AS \"from\""
	    	+ " FROM message AS m INNER JOIN user AS u ON useraccount = login_id WHERE Chatroom = ? AND message_id < ? ORDER BY createtime DESC) ORDER BY createtime ASC", new String[]{"" + chatroomID, ""+message_id});
	    out.put("messages", msg);
	    
	    return out;
	}
	
	private JSONArray getMessagesFromDB(String forUser) throws SQLException
	{
		JSONArray	res		= new JSONArray();
		DatabaseHandler db	= DatabaseHandler.getdb();
		JSONArray chatroom	= db.executeQuery("SELECT c.chatroom, max(createtime) AS lasttime FROM chatroommapping AS c LEFT JOIN message AS m ON c.chatroom = m.chatroom WHERE c.useraccount = ? GROUP BY c.chatroom ORDER BY max(createtime) DESC", forUser);
		
		log.debug("Chatroom size: " +chatroom.size());
		log.debug("Chatroom: " + chatroom.toJSONString());
		for (int i = 0; i < chatroom.size(); i++)
		{
		    	
			JSONObject	room	= (JSONObject) chatroom.get(i);
			int number			= (int) room.get("chatroom"); 	
			JSONArray persons	= db.executeQuery("SELECT useraccount , u.login_id, u.displayname FROM chatroommapping INNER JOIN user AS u ON useraccount = u.login_id WHERE Chatroom = ? AND useraccount <> ?", new String[]{"" + number, forUser});			
			if(persons.size() == 0){
			    log.debug("Persons is empty");
			}
			else
			{
        			if(persons.size() == 1)
        			{
        				room.put("loginid", (String)((JSONObject)persons.get(0)).get("login_id"));
        				room.put("displayname", (String)((JSONObject)persons.get(0)).get("displayname"));
        			}
        			else if( persons.size() > 1) // Ignore room if theres less than 1 other person
        			{
        				room.put("loginid", "Gruppenchat");
        				room.put("displayname", "Gruppenchat");
        			}
        			JSONArray msg		= db.executeQuery("SELECT * FROM (SELECT TOP 50 createtime, messagecontent, chatroom, message_id, useraccount, u.displayname AS \"from\" FROM MESSAGE INNER JOIN user AS u ON useraccount = u.login_id WHERE Chatroom = ? ORDER BY createtime DESC) ORDER BY createtime ASC", "" + number);
        			int unread			= db.executeQuery("SELECT u.message FROM MESSAGESUNREAD AS u INNER JOIN MESSAGE AS m	ON m.message_id    = u.message	WHERE m.chatroom  = ? AND u.useraccount = ?", new String[]{"" + number, forUser}).size();
        						
        			//room.put("persons", persons);
        			room.put("messages", msg);
        			room.put("unreadMessages", unread);
        			res.add(room);	
			}
		}
		db.executeUpdate("DELETE FROM MESSAGESUNREAD WHERE useraccount = ?", forUser);
		return res;
	}
}
