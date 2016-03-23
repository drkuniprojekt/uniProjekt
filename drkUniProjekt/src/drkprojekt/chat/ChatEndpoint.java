package drkprojekt.chat;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

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
	public void onMessage(String msg)
	{
		try
		{
			PushService.sendBroadCastMessage(msg);
			log.info("Got new Message: " + msg);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
