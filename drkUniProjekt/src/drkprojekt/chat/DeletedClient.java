package drkprojekt.chat;

import java.util.ArrayList;

import javax.websocket.Session;

import org.json.simple.JSONObject;

public class DeletedClient extends ChatClient
{

	public DeletedClient(String login, String displayname)
	{
		this.name			= login;
		this.displayName	= displayname;
	}

	@Override
	public boolean sendMessage(JSONObject msg)
	{
		throw new IllegalArgumentException("The client " + name + " has been deleted");			
	}

//	@Override
//	public boolean sendMessage(String msg)
//	{
//		throw new IllegalArgumentException("The client " + name + " has been deleted");	
//	}

	@Override
	public void deleteSession(Session session)
	{	
		throw new IllegalArgumentException("The client " + name + " has been deleted");	
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDisplayName()
	{
		return displayName;
	}

	@Override
	public void setSessionList(ArrayList<Session> sessionList)
	{
		throw new IllegalArgumentException("The client " + name + " has been deleted");	
	}

	@Override
	public void addSession(Session session)
	{
		throw new IllegalArgumentException("The client " + name + " has been deleted");	
	}

	@Override
	public ArrayList<String> getPhonegap_ids()
	{		
		return null;
	}

	@Override
	public void setPhonegap_ids(ArrayList<String> ids)
	{
		throw new IllegalArgumentException("The client " + name + " has been deleted");	
	}

	@Override
	public void addPhonegap_id(String id)
	{
		throw new IllegalArgumentException("The client " + name + " has been deleted");	
	}

}
