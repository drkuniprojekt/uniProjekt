package drkprojekt.chat;

import java.io.IOException;
import java.util.ArrayList;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import	org.slf4j.Logger;

import drkprojekt.rest.PushService;


public abstract class ChatClient
{
	protected Logger log	= LoggerFactory.getLogger(ChatClient.class);
	protected String name;	
	
	public ChatClient(String name)
	{
		this.name		= name;	
	}
	
	public ChatClient() 
	{
		
	}
	
	public abstract boolean sendMessage(JSONObject msg);
	
	public abstract boolean sendMessage(String msg);
	
	public abstract void deleteSession(Session session);
	
	public abstract String getName();
	
	public abstract void setSessionList(ArrayList<Session> sessionList);
	
	public abstract void addSession(Session session);
	
	public abstract ArrayList<String> getPhonegap_ids();
	
	public abstract void setPhonegap_ids(ArrayList<String> ids);
	
	public abstract void addPhonegap_id(String id);
}
