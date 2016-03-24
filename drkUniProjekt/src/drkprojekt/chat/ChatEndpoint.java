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
	public String onMessage(String msg)
	{
		try
		{
			JSONObject msgJson 	= (JSONObject) new JSONParser().parse(msg);
			String recipient	= (String) msgJson.get("to");
			RemoteEndpoint.Basic r			= null;
			
		//-----------------------------------------------------------------
			if(msgJson.get("test") != null)
			{
				return "Test result: " + userExists(recipient);
			}
		//-------------------------------------------------------------------	
			if(recipient == null || recipient.equals("Broadcast"))
			{
				PushService.sendBroadCastMessage(msg);
				for (ChatClient c: peers)
				{
					r	= c.getSession().getBasicRemote();
				}
				log.debug("Got new Broadcast-Message: " + msg);				
			}else
			{				
				for (ChatClient c: peers)
				{
					if(c.getName().equals(recipient))
					{
						r	= c.getSession().getBasicRemote();
						break;
					}
				}
				log.debug("Got new Message to " + recipient + ": "+ msg);
			}
			if(r!= null)
			{
				r.sendText((String)msgJson.get("message"));
				return "Message was send to " + recipient;
			}else
			{
				throw new IllegalArgumentException("The given recipient was not found");
			}
			
			
		} catch (SQLException | IOException e)
		{
			e.printStackTrace();
		} catch (ParseException | IllegalArgumentException | NullPointerException e)
		{
			log.warn("Bad Request over Websocket", e);
			return "Bad Request";
		}
		return "Unknown Error";
	}
	private boolean userExists(String login_id) throws SQLException
	{
		try
		{
			JSONArray array = DatabaseHandler.getdb().executeQuery("SELECT login_id FROM user WHERE login_id = ?", login_id);
			log.debug("User exists DB-Result: ", array);
			if(array.get(0).equals(login_id))
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
