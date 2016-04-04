package drkprojekt.chat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.database.DatabaseHandler;

public class ClientFactory 
{
	private static ArrayList<ChatClient> clients	= new ArrayList<ChatClient>();
	private static Logger log	= LoggerFactory.getLogger(ClientFactory.class);
	
	private static void init() throws SQLException
	{
		if(clients.size() == 0)
		{
			JSONArray login_idJSON 	= DatabaseHandler.getdb().executeQuery("SELECT login_id, displayname, deleted FROM user");
			addClientsToList(login_idJSON);
		}else
		{
			log.debug("Ignoring init call, as the FActory has already been initialized");
		}
		
	}

	/**
	 * Returns the client with the given login_id.
	 * @param login_id
	 * @return ChatClient
	 * @throws SQLException 
	 */
	public static ChatClient getClient(String login_id) throws SQLException
	{
		if(clients.size() == 0)
		{
			try {
				init();
			} catch (SQLException e) 
			{
				log.error("Error while instantiating clients"+ e);
			}
		}
			
		for (int i = 0; i < clients.size(); i++)
		{
			if(clients.get(i).getName().equals(login_id))
			{
				return clients.get(i);
			}			
		}
		//Check if the client was created since the server has been started
		JSONArray newClient 	= DatabaseHandler.getdb().executeQuery("SELECT login_id, displayname, deleted FROM user WHERE login_id = ?", login_id);
		
		if(newClient.size() > 0)
		{
			addClientsToList(newClient);
			
			return clients.get(clients.size() - 1);
		}
		
		return new NullClient(login_id, "NullClient");
	}
	public static ArrayList<ChatClient> getAllClients()
	{
		return clients;
	}

	private static void addClientsToList(JSONArray login_idJSON) throws SQLException
	{
		String login, displayname;
		boolean deleted;
		String logMsg	= "";
		for (int i = 0; i < login_idJSON.size(); i++) 
		{
			login				= (String)((JSONObject)login_idJSON.get(i)).get("login_id");
			displayname			= (String)((JSONObject)login_idJSON.get(i)).get("displayname");
			deleted 			= (boolean)((JSONObject)login_idJSON.get(i)).get("deleted");
			ChatClient c;
			
			if(deleted)
			{
				c	= new DeletedClient(login, displayname);
			}else
			{
				c	= new RealClient(login, displayname);
			}
			
			logMsg				+= login + ", ";
			JSONArray pgJSON 	= DatabaseHandler.getdb().executeQuery("SELECT device_id FROM PHONEGAPID WHERE REGISTEREDUSER = ?", login);
			
			for (int j = 0; j < pgJSON.size(); j++) 
			{
				c.addPhonegap_id((String)((JSONObject)pgJSON.get(i)).get("device_id"));
			}
			clients.add(c);
		}
		log.debug("Adding new Clients to Client list: {}", logMsg);
	}
}
