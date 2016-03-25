package drkprojekt.chat;

import javax.websocket.Session;


public class ChatClient
{
	private Session session;
	private String name;
	
	public ChatClient(Session session, String name)
	{
		this.session	= session;
		this.name		= name;		
		
	}
	public String getName()
	{
		return name;
	}
	public Session getSession()
	{
		return session;
	}
	
}
