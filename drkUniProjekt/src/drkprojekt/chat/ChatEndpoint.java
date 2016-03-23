package drkprojekt.chat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

@ServerEndpoint("/chat")
public class ChatEndpoint
{
	private static Set<Session> peers	= Collections.synchronizedSet(new HashSet<Session>());
	
	@OnOpen
	public void onOpen(Session peer)
	{
		peers.add(peer);		
	}
	
	@OnMessage
	public void onMessage(String msg)
	{
		
	}
}
