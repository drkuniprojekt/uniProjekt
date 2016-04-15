package drkprojekt.chat;

import java.util.ArrayList;

import javax.websocket.Session;

import org.json.simple.JSONObject;

public class NullClient extends ChatClient
{	
	
	public NullClient(String name, String displayName)
	{
		super(name, displayName);
	}
	
	@Override
	public boolean sendMessage(JSONObject msg) {
		throw new IllegalArgumentException("The client " + name + " does not exist");		
	}

//	@Override
//	public boolean sendMessage(String msg) {
//		throw new IllegalArgumentException("The client " + name + " does not exist");
//	}

	@Override
	public void deleteSession(Session session) {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}

	@Override
	public String getName() {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}
	
	@Override
	public String getDisplayName() {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}

	@Override
	public void setSessionList(ArrayList<Session> sessionList) {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}

	@Override
	public void addSession(Session session) {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}

	@Override
	public ArrayList<String> getPhonegap_ids() {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}

	@Override
	public void setPhonegap_ids(ArrayList<String> ids) {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}

	@Override
	public void addPhonegap_id(String id) {
		throw new IllegalArgumentException("The client " + name + " does not exist");
	}

}
