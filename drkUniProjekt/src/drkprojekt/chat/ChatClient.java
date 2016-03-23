package drkprojekt.chat;

import javax.websocket.MessageHandler;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatClient implements MessageHandler
{
	private Session session;
	private String name;
	private static Logger log;
	
	public ChatClient(Session session, String name)
	{
		this.session	= session;
		this.name		= name;
		
		this.session.addMessageHandler(this);
		log				= LoggerFactory.getLogger(this.getClass());
		log.info("New client object");
	}
	
	
	public void onMessage(String text)
	{
		log.info("New Message by client" + name);
		session.getBasicRemote();
	}
}
