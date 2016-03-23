package drkprojekt.chat;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import drkprojekt.rest.PushService;

@ServerEndpoint("/chat")
public class ChatEndpoint
{
	private static Set<Session> peers	= Collections.synchronizedSet(new HashSet<Session>());
	private static Logger log;
	@OnOpen
	public void onOpen(Session peer)
	{
		peers.add(peer);
		log	= LoggerFactory.getLogger(this.getClass());
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
